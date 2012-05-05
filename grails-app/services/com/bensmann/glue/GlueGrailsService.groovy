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

/**
 * 
 */
class GlueGrailsService {
	
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
	private def getService(name) {
		def service = grailsApplication.mainContext.getBean(name)
		if (!service) {
			log.error "getService: Cannot find service ${name}"
		}
		println "getService: name=${name} -> ${service}"
		service
	}
	
	/**
	 * 
	 */
	def methodMissing(String name, args) {
		def what = name.split("_")
		println "methodMissing: what=${what}"
		// grailsService.call(service, method, args)
		if (what[0] == "call" && args.length > 2) {
			println "methodMissing: calling getService(${args[0]})?.'${args[1]}'(${args[2]})"
			getService(args[0])?."${args[1]}"(args[2])
		}
		// grailsService.call_aMethod_from_otherService(args)
		else if (what[0] == "call" && what[2] == "from" && what.length == 4) {
			def method = what[1]
			def service = what[3]
			println "methodMissing: calling getService(${service})?.'${method}'(${args})"
			getService(service)?."${method}"(args)
		} else {
			log.error "methodMissing: Don't know what to do: name=${name}, args=${args}"
		}
	}
	
}
