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
class GlueCrudEditorTagLib implements ApplicationContextAware {
	
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
	def glueConfigurationService
	
	/**
	 * 
	 */
	def glueWidgetService
	
	/**
	 * Throw an error.
	 */
	private def throwError(msg) {
		throw new IllegalStateException(msg)
	}
	
	/**
	 * 
	 */
	private def _renderNoSelection(property) {
		if (log.traceEnabled) log.trace "_renderNoSelection(${property}): ${property.inspect()}"
		if (property.optional) {
			if (property.oneToMany || property.manyToOne || property.manyToMany) {
				return ['null': '']
			} else {
				return ['': '']
			}
		}
		return null
	}
	
	/**
	 * Render a string property as input type=text, textarea or select tag.
	 */
	def renderStringEditor = { attr ->
		if (log.traceEnabled) log.trace "renderStringEditor: attr=${attr.inspect()}"
		//
		if (attr.mode == "show") {
			out << attr.g_inst."${attr.property}"
		} else {
			def buf = new StringBuilder()
			if (!attr.g_constrainedProperty) {
				buf << "<input type=\"text\""
				buf << " id=\"${attr.formPrefix}_${attr.property}\""
				buf << " name=\"${attr.formPrefix}_${attr.property}\""
				buf << " value=\"${g.fieldValue(bean: attr.g_inst, field: '${attr.property}')}\""
				buf << "/>"
				out << buf.toString()
			} else {
				if (attr.g_widget == "textarea" || (attr.g_constrainedProperty.maxSize > 250 && !attr.g_constrainedProperty.password && !attr.g_constrainedProperty.inList)) {
					buf << "<textarea rows=\"5\" cols=\"40\""
					buf << " id=\"${attr.formPrefix}_${attr.property}\""
					buf << " name=\"${attr.formPrefix}_${attr.property}\">"
					buf << g.fieldValue(bean: attr.g_inst, field: attr.property)
					buf << "</textarea>"
					out << buf.toString()
				}
				else if (attr.g_widget == "richtext") {
					out << gui.richEditor(
						id: "${attr.formPrefix}_${attr.property}",
						name: "${attr.formPrefix}_${attr.property}",
						value: g.fieldValue(bean: attr.g_inst, field: attr.property)
					)
				} else if (attr.g_constrainedProperty?.inList) {
					out << g.select(
						id: "${attr.formPrefix}_${attr.property}",
						name: "${attr.formPrefix}_${attr.property}",
						from: attr.g_constrainedProperty.inList,
						value: attr.g_inst."${attr.property}",
						noSelection: _renderNoSelection(attr.g_property)
					)
				} else {
					buf << "<input"
					attr.g_constrainedProperty.password ? buf << ' type="password" ' : buf << ' type="text"'
					buf << " id=\"${attr.formPrefix}_${attr.property}\""
					buf << " name=\"${attr.formPrefix}_${attr.property}\""
					buf << " value=\"${g.fieldValue(bean: attr.g_inst, field: attr.property)}\""
					if (!attr.g_constrainedProperty.editable) {
						buf << ' readonly="readonly"'
					}
					if (attr.g_constrainedProperty.maxSize) {
						buf << " size=\"${cp.maxSize}\" maxlength=\"${cp.maxSize}\""
					}
					buf << " class=\"crud_input_text\""
					buf << "/>"
					out << buf.toString()
				}
			}
		}
	}
	
	/**
	 * Render a number property as input type=text, select tag.
	 */
	def renderNumberEditor = { attr ->
		if (log.traceEnabled) log.trace "renderNumberEditor: attr=${attr.inspect()}"
		//
		if (attr.mode == "show") {
			// g.formatNumber(number: model.inst."${attr.property}", type: "currency", currencyCode: "EUR")
			out << attr.g_inst."${attr.property}"
		} else {
			def value = attr.g_inst."${attr.property}"
			if (!attr.g_constrainedProperty) {
				if (attr.g_propertyType == Byte.class) {
					out << g.select(
						id: "${attr.formPrefix}_${attr.property}",
						name: "${attr.formPrefix}_${attr.property}",
						from: "-128..127",
						value: value
					)
				} else {
					out << "<input type=\"text\" id=\"${attr.formPrefix}_${attr.property}\" name=\"${attr.formPrefix}_${attr.property}\" value=\"${value}\" />"
				}
			} else {
				if (attr.g_constrainedProperty.range) {
					out << g.select(
						id: "${attr.formPrefix}_${attr.property}",
						name: "${attr.formPrefix}_${attr.property}",
						from: attr.g_constrainedProperty.range.from..attr.g_constrainedProperty.range.to,
						value: value,
						noSelection: _renderNoSelection(attr.g_property)
					)
				} else if (attr.g_constrainedProperty.inList) {
					out << g.select(
						id: "${attr.formPrefix}_${attr.property}",
						name: "${attr.formPrefix}_${attr.property}",
						from: attr.g_constrainedProperty.inList,
						value: value,
						noSelection: _renderNoSelection(attr.g_property)
					)
				} else {
					// g.fieldValue(bean: attr.g_inst, field: attr.property)
					out << "<input type=\"text\" id=\"${attr.formPrefix}_${attr.property}\" name=\"${attr.formPrefix}_${attr.property}\" value=\"${value}\" />"
				}
			}
		}
	}
	
	/**
	 * Render a boolean property as a checkbox.
	 */
	def renderBooleanEditor = { attr ->
		if (log.traceEnabled) log.trace "renderBooleanEditor: attr=${attr.inspect()}"
		//
		if (attr.mode == "show") {
			out << attr.g_inst."${attr.property}"
		} else {
			if (!attr.g_constrainedProperty) {
				out << g.checkBox(
					id: "${attr.formPrefix}_${attr.property}",
					name: "${attr.formPrefix}_${attr.property}",
					value: attr.g_inst."${attr.property}"
				)
			} else {
				def m = [id: "${attr.formPrefix}_${attr.property}", name: "${attr.formPrefix}_${attr.property}", value: attr.g_inst."${attr.property}"]
				m.putAll(attr.g_constrainedProperty?.attributes)
				out << g.checkBox(m)
			}
		}
	}
	
	/**
	 * Render a BLOB property as a file upload.
	 */
	def renderBlobEditor = { attr ->
		if (log.traceEnabled) log.trace "renderBlobEditor: attr=${attr.inspect()}"
		// TODO Render one-to-many
		def show = { buf ->
			// The attachment
			def att = attr.g_inst."${attr.property}"
			if (att) {
				// The name
				def name
				try {
					name = attr.g_inst."${attr.property}OriginalFilename"
				} catch (e) {
					log.warn "_renderBlobEditor: Cannot render property ${attr.property}: ${e}"
					name = "Unknown"
				}
				buf << "<p>\n"
				buf << "Previously uploaded file: "
				// TODO One-to-many
				buf << g.link(
					controller: "glue",
					action: "streamBlob",
					params: [domain: attr.domain, domainId: attr.domainId, property: attr.property]
				) { name }
				buf << "\n</p>\n"
			}
		}
		def buf = new StringBuilder()
		show buf
		if (attr.mode in ["create", "edit"]) {
			def n = "${attr.domain}_${attr.domainId}_${attr.property}".replaceAll("\\.", "#")
			buf << "<input type=\"file\" id=\"gxupload_${n}\" name=\"gxupload_${n}\" />\n"
		}
		out << buf.toString()
	}
	
	/**
	 * Render a select tag depending on attr.type.
	 */
	def renderSelectTypeEditor = { attr ->
		if (log.traceEnabled) log.trace "renderSelectTypeEditor: attr=${attr.inspect()}"
		//
		if (attr.mode == "show") {
			switch (attr.type.toLowerCase()) {
				case "timezone":
					out << attr.g_inst."${attr.property}"?.displayName
					break
				case "locale":
					out << attr.g_inst."${attr.property}"?.displayName
					break
				case "currency":
					out << attr.g_inst."${attr.property}"
					break
				default:
					out << "(don't know how to render attr=${attr.inspect()})"
			}
		}
		//
		def m = [id: "${attr.formPrefix}_${attr.property}", name: "${attr.formPrefix}_${attr.property}", noSelection: _renderNoSelection(attr.g_property)]
		m.putAll(attr.g_constrainedProperty?.attributes)
		out << g."${attr.type}Select"(m)
	}
	
	/**
	 * Render a date editor.
	 * Depends on Grails UI.
	 */
	def renderDateEditor = { attr ->
		if (log.traceEnabled) log.trace "renderDateEditor: attr=${attr.inspect()}"
		//
		def precision = attr.precision ?: (attr.g_propertyType == java.sql.Timestamp ? "true" : "false")
		def formatString = (precision == "true" ? "dd.MM.yyyy HH:mm:ss" : "dd.MM.yyyy")
		if (log.traceEnabled) log.trace "renderDateEditor(${attr.inspect()}): attr.propertyType=${attr.propertyType}, precision=${precision} formatString=${formatString}"
		//
		if (attr.mode == "show") {
			def v = attr.g_inst?."${attr.property}"?.format(formatString)
			if (log.traceEnabled) log.trace "renderDateEditor(${attr.inspect()}): v=${v}"
			out << v
		} else {
			//
			def m = [
				id: "${attr.formPrefix}_${attr.property}",
				name: "${attr.formPrefix}_${attr.property}",
				value: attr.g_inst?."${attr.property}",
				includeTime: precision,
				formatString: formatString
			]
			if (attr.g_constrainedProperty) {
				if (attr.g_constrainedProperty.format) {
					m.format = cp.format
				}
				m.putAll(attr.g_constrainedProperty.attributes)
			}
			//
			def buf = new StringBuilder()
			buf << "<div class=\"yui-skin-sam crud_datepicker\">\n"
			buf << gui.datePicker(m)
			buf << "</div>"
			if (log.traceEnabled) log.trace "renderDateEditor(${attr.inspect()}): rendering ${buf.toString()}"
			out << buf.toString()
		}
	}
	
	/**
	 * Render an enum property.
	 */
	def renderEnumEditor = { attr ->
		if (log.traceEnabled) log.trace "renderEnumEditor: attr=${attr.inspect()}"
		//
		if (attr.mode == "show") {
			out << attr.g_inst."${attr.property}"
		} else {
			out << g.select(
				id: "${attr.formPrefix}_${attr.property}",
				name: "${attr.formPrefix}_${attr.property}",
				from: attr.g_propertyType.name.values(),
				value: attr.property, // TODO ???
				noSelection: _renderNoSelection(attr.g_property)
			)
		}
	}
	
	/**
	 * Autocomplete.
	 * Depends on: Grails UI plugin.
	 * <gx:renderAutoComplete domain="Company" domainId="1" property="two" />
	 */
	def renderAutoComplete = { attr ->
		// Create model
		attr = attr + applicationContext.glueTagLibService.checkModel(attr)
		if (log.traceEnabled) log.trace "renderAutoComplete: attr=${attr.inspect()} flash=${flash.inspect()}"
		out << glueWidgetService.renderAutoComplete(attr)
	}
	
}
