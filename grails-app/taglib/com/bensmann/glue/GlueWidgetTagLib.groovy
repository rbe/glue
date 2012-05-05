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
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * 
 */
class GlueWidgetTagLib implements ApplicationContextAware {
	
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
	def glueLinkService
	
	/**
	 * Throw an error.
	 */
	private def throwError(msg) {
		throw new IllegalStateException(msg)
	}
	
	/**
	 * Render a link for displaying list.
	 */
	def listLink = { attr, body ->
		if (log.traceEnabled) log.trace "listLink: attr=${attr.inspect()}"
		// Attributes
		def m = attr
		m.url = [
			controller: "glue",
			action: "list",
			params: [domain: attr.domain]
		]
		//
		out << g.remoteLink(m) { body() ?: "List" }
	}
	
	/**
	 * Render a link for creating a new domain instance.
	 */
	def createLink = { attr, body ->
		if (log.traceEnabled) log.trace "createLink: attr=${attr.inspect()}"
		// Attributes
		def m = attr
		m.url = [
			controller: "glue",
			action: "create",
			params: [domain: attr.domain]
		]
		//
		def l = g.remoteLink(m) { body() ?: "Create" }
		out << l
	}
	
	/**
	 * Render a edit link.
	 */
	def editLink = { attr, body ->
		if (log.traceEnabled) log.trace "editLink: attr=${attr.inspect()}"
		out << glueLinkService.editLink(attr + [body: body])
	}
	
	/**
	 * Render a submit button for updating a domain instance.
	 */
	def commitButton = { attr ->
		if (log.traceEnabled) log.trace "commitButton: attr=${attr.inspect()}"
		// Attributes
		def m = attr
		// Create model
		attr = attr + applicationContext.glueTagLibService.checkModel(attr)
		// The next action after commit
		def nextAction
		if (attr.nextAction) {
			nextAction = [
				nextAction_controller: attr.nextAction.controller ?: "glue",
				nextAction_action: attr.nextAction.action ?: "show",
				nextAction_params: attr.nextAction.params?.collect { k, v -> "${k}=${v}" }?.join("&")
			]
		}
		// Update
		def update = [
			update_success: attr.update?.success,
			update_failure: attr.update?.failure
		]
		// Submit to remote
		m.url = [
			controller: "glue",
			action: "update",
			params: (nextAction ?: []) + (update ?: []) + [mode: attr.mode ?: "edit"] // Default mode is 'edit'
		]
		// Value
		m.value = attr.value ?: "Commit"
		// Multipart form needed? TODO Place this in glueDomainClassService.isMultipart
		def multiPart = false
		if (attr.g_inst) {
			multiPart = attr.g_instDc.properties.any { p ->
				p.type in [java.sql.Blob]
			}
		}
		// Render
		if (log.traceEnabled) log.trace "commitButton(${attr.inspect()}): multipart=${multipart}"
		if (multiPart) {
			out << "<input type=\"submit\">"
		} else {
			out << g.submitToRemote(m)
		}
	}
	
	/**
	 * Create a link to delete this association.
	 */
	def dissociate = { attr, body ->
		if (log.traceEnabled) log.trace "dissociate: attr=${attr.inspect()}"
		arg = attr
		arg.body = body
		out << glueLinkService.dissociateLink(arg)
	}
	
}
