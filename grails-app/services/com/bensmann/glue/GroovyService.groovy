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
class GroovyService {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "singleton" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = false
	
	/**
	 * Get a property.
	 */
	def getProperty(obj, name) {
		obj.metaClass.properties.find {
			it.name == name
		}?.getProperty(obj)
	}
	
	/**
	 * Get a closure.
	 */
	def getClosure(obj, name) {
		obj.metaClass.properties.find {
			it.name == name
		}?.getProperty(obj)
	}
	
	/**
	 * Call a closure.
	 */
	def callClosure(obj, name, Object... params = null) {
		def c = getClosure(obj, name)
		if (!c) {
			return
		}
		if (!params) {
			c.call()
		} else {
			// TODO Currying?
			if (c.parameterTypes.length == 1) c.call(params[0])
			else if (c.parameterTypes.length == 2) c.call(params[0], params[1])
			else if (c.parameterTypes.length == 3) c.call(params[0], params[1], params[2])
			else if (c.parameterTypes.length == 4) c.call(params[0], params[1], params[2], params[3])
			else if (c.parameterTypes.length == 5) c.call(params[0], params[1], params[2], params[3], params[4])
			else {
				log.error "Wrong number of parameters!"
			}
		}
	}
	
	/**
	 * Get a method.
	 */
	def getMethod(obj, name) {
		def m = obj.metaClass.methods.find {
			it.name == name
		}
	}
	
	/**
	 * Call a method.
	 */
	def callMethod(obj, name, Object... params = null) {
		def m = getMethod(obj, name)
		if (!m) {
			return
		}
		if (!params) {
			m.invoke(obj)
		} else {
			m.invoke(obj, params)
		}
	}
	
	/**
	 * Call a method or a closure.
	 */
	def callMethodOrClosure(obj, name, Object... params = null) {
		def ret
		def m = getMethod(obj, name)
		if (m) {
			ret = callMethod(obj, name, params)
		} else {
			m = getClosure(obj, name)
			ret = callClosure(obj, name, params)
		}
		ret
	}
	
}
