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

import com.bensmann.glue.auth.*
import com.bensmann.glue.context.*
import groovy.xml.DOMBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.dom.DOMCategory
import javax.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder

/**
 * Manages contextual information.
 */
class GlueContextService {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "session" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = true
	
	/**
	 * Domain instances.
	 */
	def inst = [:]
	
	/**
	 * 
	 */
	private HttpSession getSession() {
		return RequestContextHolder.currentRequestAttributes().getSession()
	}
	
	/**
	 * Find context.
	 */
	def findOldContext(arg) {
		if (log.traceEnabled) log.trace "findOldContext(${arg?.inspect()})"
		def glueContext = GlueContext.withCriteria {
			if (arg.user) eq("user", arg.user)
			if (arg.sessionId) ne("sessionId", arg.sessionId)
		}
		if (glueContext) {
			if (log.traceEnabled) log.trace "findOldContext(${arg?.inspect()}): found ${glueContext?.inspect()}"
			assert glueContext.size() == 1
			glueContext[0]
		}
	}
	
	/**
	 * Get context: use context object from session or create a new one.
	 */
	def getContext(arg) {
		if (log.traceEnabled) log.trace "getContext(${arg?.inspect()})"
		def session = getSession()
		if (!session.glueContext) {
			if (log.traceEnabled) log.trace "getContext(${arg?.inspect()}): creating new context"
			session.glueContext = new GlueContext()
			session.glueContext.sessionId = session.id
			// No Hibernate session bound to thread
			GlueContext.withTransaction {
				session.glueContext.save(flush: true)
			}
		}
		if (log.traceEnabled) log.trace "getContext(${arg?.inspect()}): returning ${session?.glueContext}"
		assert session.glueContext
		session.glueContext
	}
	
	/**
	 * 
	 */
	def authenticate(arg) {
		if (log.traceEnabled) log.trace "authenticate(${arg?.inspect()})"
		// Get actual context
		def glueContext = getContext()
		// Already authenticated?
		if (glueContext?.sessionId && glueContext?.user) {
			if (log.traceEnabled) log.trace "authenticate(${arg?.inspect()}): session already authenticated: ${glueContext?.inspect()}"
			true
		}
		// Lookup user in database
		def user = GlueUser.findByNameAndPassword(arg.user, arg.password)
		if (user) {
			// Try to find old context by user
			def oldContext = findOldContext(user: user/*, sessionId: session.id*/)
			if (oldContext) {
				// Delete actual context
				glueContext.delete(flush: true)
				// Use old context
				if (log.traceEnabled) log.trace "authenticate(${arg?.inspect()}): found old context=${oldContext?.inspect()}"
				glueContext = oldContext
			}
			// Set user in context
			glueContext.user = user
			// Update with actual session ID
			glueContext.sessionId = session.id
			// Save Glue Context in database and session
			def session = getSession()
			session.glueContext = glueContext.merge(flush: true)
			if (log.traceEnabled) log.trace "authenticate(${arg?.inspect()}): successfully authenticated" <<
				", glueContext=${session?.glueContext?.inspect()}/${session?.glueContext?.user}"
			true
		} else {
			false
		}
	}
	
	/**
	 * 
	 */
	def getUser() {
		getContext()?.user
	}
	
	/**
	 * Save XML.
	 */
	def private _saveContextXml(document) {
		def gctx = getContext()
		if (document && gctx.user) {
			gctx.contextXml = new StreamingMarkupBuilder().bind { mkp.yieldUnescaped document.documentElement }
			session.glueContext = gctx.merge(flush: true)
			assert session.glueContext
			if (log.traceEnabled) log.trace "_saveContextXml(${document}): contextXml=${session.glueContext.contextXml}"
		} else {
			if (log.traceEnabled) log.trace "_saveContextXml(${document}): nothing saved, no document or no user in session"
		}
	}
	
	/**
	 * Get context XML document as W3C document.
	 */
	def getContextXmlAsDocument() {
		// The document
		def contextXml = getContext().contextXml
		def document
		// Create new document or parse existing one
		if (!contextXml) {
			document = DOMBuilder.newInstance().createDocument()
			document.appendChild(document.createElement("glue"))
		} else {
			document = DOMBuilder.parse(new StringReader(contextXml))
		}
		document
	}
	
	/**
	 * Add a domain instance to this context.
	 */
	def addDomain(arg) {
		if (log.traceEnabled) log.trace "addDomain: arg=${arg.inspect()}"
		use (DOMCategory) {
			// The document
			def document = getContextXmlAsDocument()
			// Add domain
			def domain = document.createElement("domain")
			document.documentElement.appendChild(domain)
			domain.setAttribute("name", arg.domain)
			domain.setAttribute("id", arg.domainId.toString())
			// Save XML
			_saveContextXml(document)
		}
	}
	
	/**
	 * Remove a domain instance to this context.
	 */
	def removeDomain(arg) {
		if (log.traceEnabled) log.trace "removeDomain: arg=${arg.inspect()}"
		use (DOMCategory) {
			def document = getContextXmlAsDocument()
			document.domain?.grep {
				it."@name" == arg.domain && it."@id" == arg.domainId
			}?.each {
				if (log.traceEnabled) log.trace "removing ${it}"
				document.documentElement.removeChild(it)
			}
			// Save XML
			_saveContextXml(document)
		}
	}
	
	/**
	 * Remove a domain instance to this context.
	 */
	def removeDomains() {
		use (DOMCategory) {
			def document = getContextXmlAsDocument()
			document.domain?.each {
				document.documentElement.removeChild(it)
			}
			// Save XML
			_saveContextXml(document)
		}
	}
	
	/**
	 * Get certain domain object from context.
	 */
	def getDomain(arg) {
		use (DOMCategory) {
			getContextXmlAsDocument()?.domain?.grep { it."@name" == arg.domain }
		}
	}
	
	/**
	 * Get all domain objects from context.
	 */
	def getDomains() {
		use (DOMCategory) {
			getContextXmlAsDocument()?.domain
		}
	}
	
}
