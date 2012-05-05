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

import groovyx.net.ws.WSClient
import org.codehaus.groovy.grails.commons.ConfigurationHolder as GCH

/**
 * 
 */
//@Grab(group="org.codehaus.groovy.modules", module="groovyws", version="0.5.1")
class GlueWsClientService {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "singleton" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = true
	
	/**
	 * WSDL to proxy instance cache.
	 */
	def cache = [:]
	
	/**
	 * Return a proxy for a web service.
	 */
	def getProxy(wsdl, reinit = false) {
		if (log.traceEnabled) log.trace "getProxy(${wsdl}, ${reinit})"
		// Lookup already cached proxy
		def proxy = cache[wsdl]
		// Force a re-initialization or don't we have a proxy instance cached before?
		if (reinit || !proxy) {
			// Remove cached proxy when re-initializing proxy
			if (reinit && cache.containsKey(wsdl)) {
				if (log.debugEnabled) log.debug "getProxy(${wsdl}, ${reinit}): removing cached proxy"
				cache.remove(wsdl)
			}
			if (log.debugEnabled) log.debug "getProxy(${wsdl}, ${reinit}): creating new proxy"
			try {
				proxy = new WSClient(wsdl, this.class.classLoader)
				proxy.initialize()
				cache[wsdl] = proxy
			} catch (e) {
				// In case of exception the proxy object remains intact, so we reset it
				// 2010-04-28 15:19:23,555 [http-8080-1] ERROR glue.GlueWsClientService  - getProxy(http://localhost:8080/glue/services/gxMail?WSDL, false): could not create proxy for http://localhost:8080/glue/services/gxMail?WSDL
				// proxy=groovyx.net.ws.WSClient@5632af89
				proxy = null
				log.error "getProxy(${wsdl}, ${reinit}): could not create proxy for ${wsdl}"
			}
		}
		if (log.traceEnabled) log.trace "getProxy(${wsdl}, ${reinit}): returning proxy=${proxy}"
		proxy
	}
	
	/**
	 * Return a reference to a Glue mail service proxy.
	 */
	def mailService() {
		def glueConfig = GCH.config.glue
		if (log.debugEnabled) log.debug "mailService(): glueConfig=${glueConfig}"
		getProxy("${glueConfig.service.api.baseurl}/services/gxmail?WSDL")
	}
	
}
