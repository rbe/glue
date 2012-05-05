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

import org.springframework.beans.factory.InitializingBean

/**
 * 
 */
class GlueWidgetService implements InitializingBean {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	// TODO Change to singleton?
	def scope = "prototype" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = false
	
	/**
	 * 
	 */
	def grailsApplication
	
	/**
	 * 
	 */
	def glueConfigurationService
	
	/**
	 * 
	 */
	def glueDomainClassService
	
	/**
	 * 
	 */
	def glueLinkService
	
	/**
	 * 
	 */
	def glueCrudService
	
	/**
	 * The GSP tag library lookup.
	 */
	def gspTagLibraryLookup
	
	/**
	 * The g tag library.
	 */
	def g
	
	/**
	 * The Glue tag library.
	 */
	def gx
	
	/**
	 * The Grails UI tag library.
	 */
	def gui
	
	/**
	 * InitializingBean.
	 */
	void afterPropertiesSet() {
		g = gspTagLibraryLookup.lookupNamespaceDispatcher("g")
		gx = gspTagLibraryLookup.lookupNamespaceDispatcher("gx")
		gui = gspTagLibraryLookup.lookupNamespaceDispatcher("gui")
	}
	
	/**
	 * Return a list of associations for a property of a domain instance as HTML <ul>
	 * as links to edit the instance, including a delete link (for dissociating).
	 * @param arg Map: domain, id, property
	 * @return HTML string
	 */
	def associationListAsHTML(arg) {
		if (log.traceEnabled) log.trace "associationListAsHTML: ${arg.inspect()}"
		def inst = glueDomainClassService.getInstance(domain: arg.domain, id: arg.domainId)
		if (!inst) {
			log.error "associationListAsHTML: Got no instance: arg=${arg.inspect()}"
			return
		}
		def propertyType = glueDomainClassService.getPropertyType(domain: arg.domain, property: arg.property)
		// All possible values (were associated before)
		def propValue = inst."${arg.property}"
		if (!propValue instanceof List) {
			propValue = [propValue]
		}
		// Sort values
		try {
			propValue = propValue.sort { it.name }
		} catch (e) {
			// ignore
		}
		// Build HTML
		def buf = new StringBuilder()
		buf << "<div class='crud_assoc_list'>" << "<ul>"
		propValue.each { p ->
			def e = glueLinkService.editLink(
				update: arg.update,
				domain: propertyType,
				domainId: p.id,
				prevDomain: arg.domain, prevDomainId: arg.domainId, prevProperty: arg.property,
				body: { glueDomainClassService.pr(domain: p) }
			)
			buf << "<li>"
			// Add remove link when in edit mode
			if (arg.mode == "create" || arg.mode == "edit") {
				buf << glueLinkService.dissociateLink(
					update: arg.update,
					mode: arg.mode,
					type: arg.type,
					domainA: arg.domain, idA: arg.domainId,
					propertyA: arg.property,
					domainB: propertyType, idB: p.id
				)
			}
			buf << "${e}</li>"
		}
		buf << "</ul>" << "</div>"
		buf.toString()
	}
	
	/**
	 * Return a list of possible associations for a one-to-one property of a domain instance
	 * as HTML <input type=radio>.
	 * @param arg Map: domain, id, property
	 * @return HTML string
	 */
	def associationRadioGroupAsHTML(arg) {
		if (log.traceEnabled) log.trace "associationRadioGroupAsHTML: arg=${arg.inspect()}"
		// TODO Check if is one-to-one relationship
		def inst = glueDomainClassService.getInstance(domain: arg.domain, id: arg.domainId)
		if (!inst) {
			log.error "associationRadioGroupAsHTML: Got no instance: arg=${arg.inspect()}"
			return
		}
		// Value of property
		def propValue = inst."${arg.property}"
		def propertyType = glueDomainClassService.getPropertyType(domain: arg.domain, property: arg.property)
		// Name of domain and radio group; for JavaScript replace dot with underscore
		def domainName = glueDomainClassService.getName(arg.domain)?.replaceAll("\\.", "_")
		def radioGroupName = "${domainName}_${arg.domainId}_${arg.property}_radioGroup"
		// Name of JavaScript function
		def onchangeName = "${domainName}_${arg.domainId}_${arg.property}_radioGroupOnChange"
		// Build JavaScript and HTML
		def buf = new StringBuilder()
		buf << "\n<script type=\"text/javascript\" language=\"JavaScript\">\n"
		buf << "${onchangeName} = function() {\n"
		buf << "var selectedValue = null;\n"
		buf << "var len = ${radioGroupName}.length;\n"
		buf << "if (len != undefined) {\n"
		buf << "	for (var i = 0; i < len; i++) {\n"
		buf << "		if (${radioGroupName}[i].checked) selectedValue = ${radioGroupName}[i].value;\n"
		buf << "	}\n"
		buf << "}\n"
		buf << glueCrudService.asyncAssociate(
					domainA: arg.domain, idA: arg.domainId,
					propertyA: arg.property,
					domainB: propertyType, idB: "selectedValue"
				)
		buf << "\n}\n"
		buf << "</script>\n"
		// All possible values
		def possibleValues = glueDomainClassService.getPossibleValues(domain: arg.domain, domainId: arg.domainId, property: arg.property)
		possibleValues.each { p ->
			def e = glueLinkService.editLink(
				update: arg.update,
				domain: propertyType,
				domainId: p.id,
				prevDomain: arg.domain, prevDomainId: arg.domainId, prevProperty: arg.property,
				body: { glueDomainClassService.pr(domain: p) }
			)
			buf << g.radio(
				name: radioGroupName,
				value: p.id,
				checked: p.id == propValue?.id ? true : false,
				onclick: "javascript:${onchangeName}();return true;",
				class: "crud_assoc_radio"
			) { e }
		}
		buf.toString()
	}
	
	/**
	 * Return a list of associations for a one-to-one/many property of a domain instance
	 * as HTML <input type=checkbox>
	 * as links to edit the instance, including a delete link (for dissociating).
	 * @param arg Map: domain, id, property
	 * @return HTML string
	 */
	def associationCheckboxAsHTML(arg) {
		if (log.traceEnabled) log.trace "associationCheckboxAsHTML: ${arg.inspect()}"
		def inst = glueDomainClassService.getInstance(domain: arg.domain, id: arg.domainId)
		if (!inst) {
			log.error "associationCheckboxAsHTML: Got no instance: arg=${arg.inspect()}"
			return
		}
		// Value(s) of property: as list
		def propValue = inst."${arg.property}"
		if (!propValue instanceof List) {
			propValue = [propValue]
		}
		def propType = glueDomainClassService.getPropertyType(domain: arg.domain, property: arg.property)
		// Name of domain; replace dot with underscore
		def domainName = glueDomainClassService.getName(arg.domain)?.replaceAll("\\.", "_")
		// Prefix for JavaScript function name
		def prefix = "${domainName}_${arg.domainId}_${arg.property}"
		// Build JavaScript and HTML for all possible values
		def buf = new StringBuilder()
		// All possible values
		def possibleValues = glueDomainClassService.getPossibleValues(domain: arg.domain, domainId: arg.domainId, property: arg.property)
		possibleValues.each { p ->
			// Name of JavaScript function
			def onchangeName = "${prefix}_${p.id}_checkBoxOnChange"
			// JavaScript
			// TODO Generate only 1 function and call with domainA/idA and domainB/idB
			buf << "\n<script type=\"text/javascript\" language=\"JavaScript\">\n"
			buf << "${onchangeName} = function() {\n"
			buf << glueCrudService.asyncSwitchAssociation(
						domainA: arg.domain, idA: arg.domainId,
						propertyA: arg.property,
						domainB: propType, idB: p.id
					)
			buf << "\n}\n"
			buf << "</script>\n"
			def e = glueLinkService.editLink(
				update: arg.update,
				domain: glueDomainClassService.getPropertyType(domain: arg.domain, property: arg.property),
				domainId: p.id,
				prevDomain: arg.domain, prevDomainId: arg.domainId, prevProperty: arg.property,
				body: { glueDomainClassService.pr(domain: p) }
			)
			buf << g.checkBox(
				name: "${prefix}_Checkbox",
				value: p.id,
				checked: p.id in propValue?.id ? true : false,
				onclick: "javascript:${onchangeName}();return true;",
				class: "crud_assoc_checkbox"
			) { e }
		}
		//
		buf.toString()
	}
	
	/**
	 * Autocomplete.
	 * Depends on: Grails UI plugin.
	 */
	def renderAutoComplete(arg) {
		def start = System.currentTimeMillis()
		if (log.traceEnabled) log.trace "renderAutoComplete: arg=${arg.inspect()}"
		//
		if (!arg.domain || !arg.domainId || !arg.property) {
			return "(renderAutoComplete: no domain, domain id or property: arg=${arg.inspect()})"
		}
		// Check configuration, mode and TODO rights
		def glueTagLibService = grailsApplication.mainContext.getBean("glueTagLibService")
		if (glueTagLibService.inEditMode() && glueConfigurationService.isAutoComplete(domain: arg.domain, property: arg.property)) {
			// Name of domain and associated property; replace dot with underscore
			def domainName = glueDomainClassService.getName(arg.domain)?.replaceAll("\\.", "_")
			def prefix = "${domainName}_${arg.domainId}_${arg.property}"
			def acName = "${prefix}_autoComplete"
			// Render autocomplete
			def buf = new StringBuilder()
			buf << "<div class=\"yui-skin-sam crud_assoc_autocomplete\">\n"
			buf << gui.autoComplete(
					id: acName,
					controller: "glue",
					action: "queryAsJSON",
					params: [domain: arg.domain, domainId: arg.domainId, property: arg.property],
					resultName: "results",
					queryDelay: 0.5,
					minQueryLength: 3,
					forceSelection: false,
					useShadow: true,
				)
			buf << "</div>\n"
			// Links
			buf << "<div class=\"crud_assoc_links\">\n"
			def propertyB = glueDomainClassService.getPropertyType(domain: arg.domain, property: arg.property).name
			buf << g.remoteLink(
					controller: "glue",
					action: "associate",
					params: "{update_success: '${arg.update?.success}', update_failure: '${arg.update?.failure}', type: '${arg.type}', mode: '${arg.mode}', domainA: '${arg.domain}', idA: ${arg.domainId}, propertyA: '${arg.property}', domainB: '${propertyB}', propertyB: \$('${acName}').value}",
					update: arg.update,
					class: arg.editLinkClass ?: "crud_assoc_links_associate"
				) { "<img class='crud_assoc_links_associate_button' src='${g.resource(dir: 'images', file: 'picture_add.png')}' alt='Add association' />" }
			buf << "</div>\n"
			def b = buf.toString()
			if (log.traceEnabled) log.trace "renderAutoComplete for ${arg.domain}#${arg.domainId}.${arg.property} took ${System.currentTimeMillis() - start} ms"
			// Return buffer
			b
		}
	}
	
}
