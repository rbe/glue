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

import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

/**
 * 
 * glueConfigurationService.setDomainClass(d, xml.toString())
 * glueConfigurationService.managerProperty(d)
 * glueConfigurationService.managerWidget(d)
 * glueConfigurationService.managerActions(d)
 * def a = glueConfigurationService.managerAction(d, 'Copy Project')
 * use (groovy.xml.dom.DOMCategory) { a?.'@service' }
 * 
 */
class GlueConfigurationService {
	
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
	 * 
	 */
	def glueDomainClassService
	
	/**
	 * Configurations of domain classes.
	 */
	def domainClass = [:]
	
	/**
	 * @param d DefaultGrailsDomainClass
	 * @param xml XML document as String
	 */
	def setDomainClass(d, xml) {
		if (log.traceEnabled) log.trace "setDomainClass: d=${d}, xml=${xml}"
		domainClass[glueDomainClassService.getName(d)] = DOMBuilder.parse(new StringReader(xml)).documentElement
	}
	
	/**
	 * 
	 */
	private def _getProperty(doc, property) {
		if (log.traceEnabled) log.trace "getProperty: doc=${doc}, property=${property}"
		use(DOMCategory) {
			doc?.property?.find { it."@name" == property }
		}
	}
	
	/**
	 * Which widget to render for a property (of an domain instance)?
	 * @param arg Map: domain, property, inst
	 */
	def getWidget(arg) {
		if (log.traceEnabled) log.trace "getWidget: arg=${arg.inspect()}"
		def widget
		def doc = domainClass[arg.domain]
		def components
		use (DOMCategory) {
			components = _getProperty(doc, arg.property)?.widget?.component
			if (components) {
				// If we got a count, select appropriate widget
				if (arg.count) {
					// Check every component
					components.list().each {
						if (widget) return // Skip rest if we found a widget
						def m = [test: it."@test", value: it."@value"?.toLong(), type: it."@type"]
						if (m.test && m.value) {
							use (GlueWidgetTestCategory) {
								if (arg.count.toLong()."${m.test}"(m.value)) {
									widget = m.type
								}
							}
						} else {
							log.warn "getWidget: No test or value found for ${arg.inspect()}: component=${it}"
						}
					}
				}
				// Else return first component
				else if (components.length > 0) {
					widget = components[0]?."@type"
				}
			}
		}
		// Check widget
		if (!widget) {
			if (log.traceEnabled) log.trace "getWidget: Could not determine widget for arg=${arg.inspect()} components=${components}"
		}
		if (log.traceEnabled) log.trace "getWidget: arg=${arg.inspect()} -> ${widget}"
		widget
	}
	
	/**
	 * Should we render the autocomplete?
	 * @param arg Map: domain, property, inst
	 */
	def isAutoComplete(arg) {
		if (log.traceEnabled) log.trace "isAutoComplete(${arg.inspect()})"
		def srac
		def doc = domainClass[arg.domain]
		use (DOMCategory) {
			srac = _getProperty(doc, arg.property)?.autoComplete?.text()?.toLowerCase()?.toBoolean() ?: false
			if (log.debugEnabled) log.debug "isAutoComplete(${arg.inspect()}): srac=${srac}"
		}
		srac
	}
	
	/**
	 * Is mapping of our special type one-to-my-many?
	 */
	def isOneToMyManyMapping(arg) {
		if (log.traceEnabled) log.trace "isOneToMyManyMapping(${arg.inspect()})"
		def oneToMyMany
		def doc = domainClass[arg.domain]
		use (DOMCategory) {
			oneToMyMany = _getProperty(doc, arg.property)?.mapping[0]?."@type" == "one-to-my-many" ?: false
			if (log.debugEnabled) log.debug "isOneToMyManyMapping(${arg.inspect()}: ${oneToMyMany}"
		}
		oneToMyMany
	}
	
	/**
	 * Get context(s) for a property.
	 */
	def getContext(arg) {
		if (log.traceEnabled) log.trace "getContext(${arg.inspect()})"
		def doc = domainClass[arg.domain]
		use (DOMCategory) {
			// TODO
			_getProperty(doc, arg.property)?.context.findAll {
				it."@type" == arg.type
			}
		}
	}
	
	/**
	 * 
	 */
	def methodMissing(String name, args) {
		if (log.traceEnabled) log.trace "methodMissing: ${name}, ${args}"
		def r
		use (DOMCategory) {
			def doc = domainClass[args[0]]
			// The property: personProperty(d)
			if (name.endsWith("Property")) {
				def n = name.substring(0, name.indexOf("Property"))
				r = _getProperty(doc, n)
			}
			// Widget of a certain property: personWidget(d)
			else if (name.endsWith("Widget")) {
				def n = name.substring(0, name.indexOf("Widget"))
				r = _getProperty(doc, n)?.widget
			}
			// Actions of a property: personActions(d)
			else if (name.endsWith("Actions")) {
				def n = name.substring(0, name.indexOf("Actions"))
				r = _getProperty(doc, n)?.action
			}
			// A certain action of a property: personAction(d, 'action name')
			else if (name.endsWith("Action")) {
				def n = name.substring(0, name.indexOf("Action"))
				r = _getProperty(doc, n)?.action?.find { it."@name" == args[1] }
			}
			// Context information: personContext(d)
			else if (name.endsWith("Context")) {
				def n = name.substring(0, name.indexOf("Context"))
				r = _getProperty(doc, args[1])?.context
			}
		}
		if (log.traceEnabled) log.trace "methodMissing: name=${name}, args=${args} -> ${r?.class}/${r}"
		r
	}
	
}
