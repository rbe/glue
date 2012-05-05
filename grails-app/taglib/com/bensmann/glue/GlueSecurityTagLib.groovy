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

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * ApplicationContext, see:
 * http://jira.codehaus.org/browse/GRAILS-1697
 * http://www.intelligrape.com/blog/?p=305
 * http://blogs.bytecode.com.au/glen/2008/07/23/overcoming-grails-circular-service-dependencies.html
 */
class GlueSecurityTagLib implements ApplicationContextAware {
	
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
	 * Throw an error.
	 */
	private void throwError(msg) {
		throw new IllegalStateException(msg)
	}
	
	/**
	 * Get user from session.
	 */
	def private getUser() {
		applicationContext.glueContextService.user
	}
	
	/**
	 * Render body if user is authenticated.
	 */
	def isAuthenticated = { attr, body ->
		if (log.traceEnabled) log.trace "isAuthenticated(${attr.inspect()})"
		def user = getUser()
		if (log.traceEnabled) log.trace "isAuthenticated(${attr.inspect()}): user=${user}"
		if (user) {
			out << body(user: user)
		}
	}
	
	/**
	 * Render body if user is not authenticated.
	 */
	def isNotAuthenticated = { attr, body ->
		if (log.traceEnabled) log.trace "isNotAuthenticated(${attr.inspect()})"
		if (!getUser()) {
			out << body()
		}
	}
	
	/**
	 * Render body if user has certain role(s).
	 */
	def hasRole = { attr, body ->
		if (log.traceEnabled) log.trace "hasRole(${attr.inspect()})"
		def test = attr.name
		if (!test instanceof List) {
			test = [test]
		}
		def user = getUser()
		if (test && user?.role?.grep { it.name in test }) {
			out << body(user: user)
		}
	}
	
	/**
	 * Render body if username matches.
	 */
	def isUser = { attr, body ->
		if (log.traceEnabled) log.trace "isUser(${attr.inspect()})"
		def user = getUser()
		if (attr.username && user?.name == attr.username) {
			out << body(user: user)
		}
	}
	
}
