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

import org.codehaus.groovy.grails.web.taglib.GroovyPageTagBody
import org.springframework.beans.factory.InitializingBean

/**
 * 
 */
class GlueLinkService implements InitializingBean {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "singleton" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = false
	
	/**
	 * 
	 */
	def grailsApplication
	
	/**
	 * The GSP tag library.
	 */
	def gspTagLibraryLookup
	
	/**
	 * The g tag library.
	 */
	def g
	
	/**
	 * 
	 */
	def glueDomainClassService
	
	/**
	 * InitializingBean.
	 */
	void afterPropertiesSet() {
		g = gspTagLibraryLookup.lookupNamespaceDispatcher("g")
	}
	
	//
	// DOMAIN CLASSES
	//
	
	/**
	 * Create a link to edit a domain instance.
	 */
	def editLink(arg) {
		if (log.traceEnabled) log.trace "editLink: arg=${arg.inspect()}"
		def params = [
			domain: glueDomainClassService.getName(arg.domain),
			domainId: arg.domainId,
			type: arg.type,
			mode: "edit",
			update_success: arg.update?.success,
			update_failure: arg.update?.failure,
		]
		if (arg.prevDomain) {
			params.nextAction_controller = "glue"
			params.nextAction_action = "renderAssociation"
			params.nextAction_params = "domain=${arg.prevDomain}&domainId=${arg.prevDomainId}&property=${arg.prevProperty}"
			
		}
		// TODO Check generation of text when no body is given
		def link = g.remoteLink(
			controller: "glue",
			action: "edit",
			params: params,
			update: arg.update,
			class: arg.editLinkClass ?: "crud_assoc_links_edit"
		) { arg.body/*?.class == GroovyPageTagBody*/ ? arg.body() : "Edit #${arg.domainId}" }
		if (log.traceEnabled) log.trace "editLink(${arg.inspect()}): link=${link}"
		link
	}
	
	/**
	 * Create a link to delete this association.
	 */
	def dissociateLink(arg) {
		if (log.traceEnabled) log.trace "dissociateLink: ${arg.inspect()}"
		g.remoteLink(
			controller: "glue",
			action: "dissociate",
			params: [
				domainA: glueDomainClassService.getName(arg.domainA), idA: arg.idA,
				propertyA: arg.propertyA,
				domainB:  glueDomainClassService.getName(arg.domainB), idB: arg.idB,
				mode: arg.mode,
				type: arg.type,
				update_success: arg.update?.success,
				update_failure: arg.update?.failure
			],
			update: arg.update,
			class: "crud_assoc_links_remove"
		) { arg.body ? arg.body() : "<img class='crud_assoc_links_remove_button' src='${g.resource(dir: 'images', file: 'picture_remove.png')}' alt='Remove association' />" }
	}
	
}
