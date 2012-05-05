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
import grails.converters.JSON
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import org.apache.log4j.MDC

/**
 * 
 */
class GlueSecurityController {
	
	/**
	 * 
	 */
	def useGlue = true
	
	/**
	 * 
	 */
	def glueContextService
	
	/**
	 * 
	 */
	def glueViewService
	
	/**
	 * The before interceptor.
	 */
	def beforeInterceptor = {
		responseNoCache()
		MDC.put("user", glueContextService.getUser()?.name ?: "no user")
	}
	
	//
	// SECURITY
	//
	
	/**
	 * Login a user.
	 */
	def login = {
		if (log.traceEnabled) log.trace "login: params=${params.inspect()}"
		// Render login template
		def v = glueViewService.render(name: "glue/login.gsp", model: [params: params])
		if (v) {
			render v
		} else {
			// Generate login form
			def out = new StringWriter()
			def builder = new groovy.xml.MarkupBuilder(out)
			v = builder.div(id: "login") {
					builder.form(method: "post") {
						span(id: "loginUser") {
							p("User:")
							input(type: "text", name: "user", value: params.user)
						}
						span(id: "loginPassword") {
							p("Password:")
							input(type: "password", name: "pwd")
						}
						span(id: "loginButton") {
							p(style: "padding-top: 10px")
							// TODO Make crud_panel_left configurable
							mkp.yieldUnescaped g.submitToRemote(url: [controller: "glueSecurity", action: "auth"], update: "crud_panel_left", value: "Login")
						}
					}
				}
			render out
		}
	}
	
	/**
	 * Authenticate a user.
	 */
	def auth = {
		if (params.user && params.pwd && glueContextService.authenticate(user: params.user, password: params.remove("pwd"))) {
			// Get previously accessed URL
			def gxPreAuthUri = session.gxPreAuthUri
			if (gxPreAuthUri) {
				session.removeAttribute("gxPreAuthUri")
			}
			// Get previously submitted parameters
			def gxPreAuthParams = session.gxPreAuthParams
			if (gxPreAuthParams) {
				session.removeAttribute("gxPreAuthParams")
			}
			//def p = gxPreAuthParams.collect { k,v -> "${k}=${v}" }.join("&")
			// Fetch original requested URL and render response
			def http = new HTTPBuilder()
			http.request("http://localhost:8080", GET, TEXT) { req ->
				// java.net.URISyntaxException: Relative path in absolute URI: http://localhost:8080null;jsessionid=38941C59C5BD9DD085C324C2B0DEA3C5
				uri.path = "${gxPreAuthUri ?: "/"};jsessionid=${session.id}"
				uri.query = gxPreAuthParams ?: [:]
				if (log.traceEnabled) log.trace "GlueSecurityController/auth: glueContext=${session.glueContext.inspect()}, redirecting to ${uri.inspect()}"
				headers."User-Agent" = "Glue/0"
				response.success = { resp, stream ->
					assert resp.statusLine.statusCode == 200
					def builder = new StringBuilder()
					stream.readLines().each { builder << "${it}\n" }
					render builder
				}
			}
		} else {
			redirect(action: "login")
		}
		if (log.traceEnabled) log.trace "auth: session=${session.inspect()}, glueContext=${session.glueContext?.inspect()}"
		assert session.glueContext
	}
	
	/**
	 * Logout a user.
	 */
	def logout = {
		if (log.traceEnabled) log.trace "logout: user=${glueContextService.glueContext.user} params=${params.inspect()}"
		session.invalidate()
	}
	
}
