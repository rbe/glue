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

import com.bensmann.cdm.project.*

/**
 * This service reads Odisee definitions from domain instances and submits them to Odisee
 * for generating a document.
 */
class GlueOdiseeService {
	
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
	def glueDomainClassService
	
	/**
	 * 
	 */
	def getOdiseeRequest(arg) {
		if (log.traceEnabled) log.trace "getOdiseeRequest: arg=${arg.inspect()}"
		def r
		def inst = glueDomainClassService.getInstance(domain: arg.domain, id: arg.domainId)
		if (inst) {
			try {
				def xml = new GlueBuilder().bind { mkp.yield inst.odiseeRequest }
				println/*if (log.debugEnabled) log.debug*/ "getOdiseeRequest: Odisee request for ${inst}: ${xml.toString()}"
				// TODO Rename to odiseeService
				def odisee = grailsApplication.mainContext.getBean("oooService")
				r = odisee.generateDocument(xml: xml.toString())
			} catch (e) {
				log.error "show: Could not process Odisee configuration of ${inst}: ${e}"
				throw e
			}
		} else {
			log.error "getOdiseeRequest(${arg.inspect()}): No instance found"
		}
		r
	}
	
}
