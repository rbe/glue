/*
 * Glue, https://github.com/rbe/glue
 * Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.bensmann.glue

import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.web.taglib.GroovyPageTagBody
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * 
 */
class GlueCrudTagLib implements ApplicationContextAware {
	
	/**
	 * Spring's application context.
	 */
	ApplicationContext applicationContext
	
	/**
	 * Our namespace.
	 */
	static namespace = "gx"
	
	/**
	 * Which tags return objects?
	 */
	static returnObjectForTags = []
	
	/**
	 * 
	 */
	def glueNameService
	
	/**
	 * 
	 */
	def glueConfigurationService
	
	/**
	 * The domain class service.
	 */
	def glueDomainClassService
	
	/**
	 * 
	 */
	def glueWidgetService
	
	/**
	 * The Glue view service.
	 */
	def glueViewService
	
	/**
	 * Throw an error.
	 */
	private def throwError(msg) {
		throw new IllegalStateException(msg)
	}
	
	/**
	 * Render a list, radio group from an association.
	 * <gx:renderAssociation type="checkBox" format="HTML" successDiv="checkbox" domain="Company" domainId="1" property="two" />
	 */
	def renderAssociation = { attr ->
		def start = System.currentTimeMillis()
		if (log.traceEnabled) log.trace "renderAssociation: attr=${attr.inspect()} flash=${flash.inspect()}"
		//
		if (attr.mode == "show") {
			attr.g_inst = glueDomainClassService.getInstance(domain: attr.domain, id: attr.domainId)
			// Without refresh() will throw org.hibernate.LazyInitializationException: could not initialize proxy - no Session
			attr.g_inst.refresh()
			// Get value
			def r
			if (attr.g_property.oneToOne) {
				// Try getting value of name property
				try {
					r = attr.g_inst."${attr.property}"?.name
				} catch (MissingPropertyException e) {
					r = attr.g_inst."${attr.property}"
				}
			} else if (attr.g_property.oneToMany || attr.g_property.manyToOne || attr.g_property.manyToMany) {
				// Try getting values of name property and join them by comma
				try {
					r = attr.g_inst."${attr.property}"?.name?.sort().join(", ")
				} catch (MissingPropertyException e) {
					r = attr.g_inst."${attr.property}"
				}
			} else {
				r = "(renderAssociation: ${attr.domain}#${attr.domainId}.${attr.property} is not an association)"
			}
			if (log.traceEnabled) log.trace "renderAssociation: showing ${r}"
			out << r?.encodeAsHTML() ?: ""
		} else {
			def t = glueNameService.ucFirst(attr.type)
			out << glueWidgetService."association${t}AsHTML"(attr) ?: "(renderAssociation: wrong type attribute: ${t})"
		}
		if (log.traceEnabled) log.trace "renderAssociation: ${attr.domain}#${attr.domainId}.${attr.property} took ${System.currentTimeMillis() - start} ms"
	}
	
	/**
	 * Render a certain property of a domain class.
	 * Examples:
	 * <gx:renderProperty domain="theDomain" domainId="1" property="theProperty"/>
	 * ... uses views/{domain}/{theProperty}.gsp or a default when template does not exist
	 * 
	 * <gx:renderProperty domain="theDomain" property="theProperty">
	 * 		${name} ${type} ${value}
	 * </gx:renderProperty>
	 * ... uses body
	 * 
	 * The attributes domain and domainId are not required when used inside a renderDomain tag.
	 */
	def renderProperty = { attr ->
		def start = System.currentTimeMillis()
		// Create model
		attr = attr + applicationContext.glueTagLibService.checkModel(attr)
		if (log.traceEnabled) log.trace "renderProperty: attr=${attr.inspect()} flash=${flash.inspect()}"
		// Check attributes
		if (!attr.domain || !attr.domainId || !attr.property) {
			out << "(renderProperty: no domain, domain id or property: attributes=${attr.inspect()})"
			return
		}
		// Check if property is 'id' or not editable
		if (/*(attr.mode == "create" || attr.mode == "edit") && */attr.property == "id" || !attr.g_constrainedProperty?.editable) {
			log.warn "renderProperty: Property ${attr.domain}.${attr.property} is not editable, setting mode to show"
			attr.mode = "show"
		}
		// Parameter map for renderers
		try {
			// Enum
			if (attr.g_property.isEnum()) {
				out << gx.renderEnumEditor(attr)
			}
			// Try to render associative property (using its widget constraint)
			else if (attr.g_property.association) {
				if (log.traceEnabled) log.trace "renderProperty: Rendering ${attr.property}: constraint.attributes=${attr.g_constrainedProperty?.attributes}"
				out << renderAssociation(attr) << gx.renderAutoComplete(attr)
			}
			// Render property editor depending on its type
			else {
				switch (attr.g_propertyType) {
					case [Character.class, String.class, URL.class, java.sql.Clob]:
						out << gx.renderStringEditor(attr)
						break
					// Number.class.isAssignableFrom(property.type) || (property.type.isPrimitive() && property.type != boolean.class)
					case [byte.class, Byte.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class]:
						out << gx.renderNumberEditor(attr)
						break
					case [boolean.class, Boolean.class]:
						out << gx.renderBooleanEditor(attr)
						break
					case [java.util.Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class, Calendar.class]:
						out << gx.renderDateEditor(attr)
						break
					case TimeZone.class:
						out << gx.renderSelectTypeEditor(attr + [type: "timeZone"])
						break
					case Locale.class:
						out << gx.renderSelectTypeEditor(attr + [type: "locale"])
						break
					case Currency.class:
						out << gx.renderSelectTypeEditor(attr + [type: "currency"])
						break
					case [java.sql.Blob, ([] as Byte[]).class]:
						out << gx.renderBlobEditor(attr)
						break
					default:
						out << "(renderProperty: don't know how to render property ${attr.domain}#${attr.domainId}.${attr.property}, type ${attr.g_propertyType})"
				}
			}
		} catch (org.codehaus.groovy.grails.exceptions.InvalidPropertyException e) {
			log.error "renderProperty: No such property ${attr.domain}.${attr.property}"
		} catch (e) {
			throw e
		}
		if (log.traceEnabled) log.trace "renderProperty: ${attr.domain}#${attr.domainId}.${attr.property} took ${System.currentTimeMillis() - start} ms"
	}
	
	/**
	 * Render a whole domain class.
	 * 
	 * <gx:renderDomain domain="theDomain" domainId="1" mode="show|create|edit"/>
	 * ... uses views/{domain}/{mode}.gsp or a default when template does not exist
	 * 
	 * <gx:renderDomain domain="theDomain" domainId="1" template="aTemplate.gsp"/>
	 * ... uses views/{domain}/aTemplate.gsp
	 */
	def renderDomain = { attr, body ->
		def start = System.currentTimeMillis()
		// Save original mode in our taglib service
		applicationContext.glueTagLibService.originalMode = attr.mode
		// Create model
		attr = attr + applicationContext.glueTagLibService.checkModel(attr)
		if (log.traceEnabled) log.trace "renderDomain: attr=${attr.inspect()} flash=${flash.inspect()}"
		//
		if (attr.g_inst) {
			// Evaluate body
			if (body?.class == GroovyPageTagBody) {
				if (log.traceEnabled) log.trace "USING BODY: body=${body?.class}"
				if (attr.template) {
					log.warn "renderDomain: Got a template and tag body, using body for ${attr.domain}#${attr.domainId}"
				}
				out << gx.form(attr, body)
			} else if (attr.template) {
				// Render
				def text = glueViewService.render(name: attr.template, model: attr)
				if (text) {
					if (log.traceEnabled) log.trace "renderDomain: Will render ${attr.domain}#${attr.domainId} using template ${attr.template}"
					out << text
				} else {
					log.error "renderDomain: Can't render domain, no result from template evaluation"
				}
			} else {
				if (log.traceEnabled) log.trace "renderDomain: Will dynamically render all properties of ${attr.domain}#${attr.domainId} as HTML"
				// Render each property
				def buf = new StringBuilder()
				GlueScaffolder.getProperties(domainClass: attr.g_instDc).each { p ->
					buf << "${p.name}: " << renderProperty(attr + [property: p.name, mode: applicationContext.glueTagLibService.originalMode]) << "<br/>"
				}
				// Show or edit?
				if (attr.mode == "show") {
					out << buf.toString()
				} else {
					// The edit form
					out << gx.form(attr) { buf.toString() << commitButton(attr) } // + [mode: mode]
				}
			}
		} else {
			out << "(renderDomain: no domain, domain id or no data found: attributes=${attr.inspect()})"
		}
		if (log.traceEnabled) log.trace "renderDomain: ${attr.domain}#${attr.domainId} took ${System.currentTimeMillis() - start} ms"
	}
	
	/**
	 * 
	 */
	def renderRowType = { attr, body ->
		// Create model
		attr = attr + applicationContext.glueTagLibService.checkModel(attr)
		//
		def rowType
		if (!attr.mode) attr.mode = mode // Take default mode (set by renderDomain)
		if (attr.mode == "show") {
			rowType = 1
		} else /*if (attr.mode in ["create", "edit"])*/ {
			if (attr.g_property.association) {
				rowType = 1
				switch (attr.type.toLowerCase()) {
					case "list":
						rowType = "list"
						break
					/*case ["radiogroup"]:
						break
					case "checkbox":
						break*/
				}
			} else {
				rowType = 1
				switch (attr.g_propertyType) {
					case [String.class, URL.class]:
						if (attr.g_widget == "textarea" || (attr.g_constrainedProperty.maxSize > 250 && !attr.g_constrainedProperty.password && !attr.g_constrainedProperty.inList)) {
							rowType = "textarea"
						}
						break
					// Number.class.isAssignableFrom(property.type) || (property.type.isPrimitive() && property.type != boolean.class
					case [byte.class, Byte.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class]:
						break
					case Boolean.class:
						break
					case [java.util.Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class, Calendar.class]:
						rowType = "date"
						break
					case TimeZone.class:
						break
					case Locale.class:
						break
					case Currency.class:
						break
					case [java.sql.Blob, ([] as Byte[]).class]:
						break
				}
			}
			if (rowType == 1 && glueConfigurationService.isAutoComplete(attr)) {
				rowType += 2
			}
		}
		if (log.traceEnabled) log.trace "renderRowType: attr=${attr.inspect()}, rowType=${rowType}"
		out << "<div id='crud_rowcount_${rowType}'>" << body() << "</div>"
	}
	
}
