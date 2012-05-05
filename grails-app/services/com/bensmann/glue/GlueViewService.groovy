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

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

/**
 * 
 */
class GlueViewService implements InitializingBean {
	
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
	 * The all-mighty Groovy template engine.
	 */
	def groovyPagesTemplateEngine
	
	/**
	 * 
	 */
	def viewRoot
	def gsps
	
	/**
	 * InitializingBean.
	 */
	void afterPropertiesSet() {
	}
	
	/**
	 * Find view root.
	 */
	def findViewRoot() {
		// Find real path on server to our root
		def root = SCH.servletContext.getRealPath("/")
		// Development mode?
		def viewRoot = "${root}/../grails-app/views"
		// Production mode: e.g. deployed in an application server
		if (!new File(viewRoot).exists()) {
			viewRoot = "${root}/WEB-INF/grails-app/views"
		}
		if (log.traceEnabled) log.trace "findViewRoot: viewRoot=${viewRoot}"
		viewRoot
	}
	
	/**
	 * Find all GSPs.
	 */
	def findGsps() {
		def gsps = []
		if (grailsApplication.isWarDeployed()) {
			findWarGsps("/WEB-INF/grails-app/views", gsps)
		} else {
			findDevGsps("grails-app/views", gsps)
		}
		if (log.traceEnabled) log.trace "findGsps(): gsps=${gsps}"
		gsps
	}
	
	/**
	 * 
	 */
	def findDevGsps(current, gsps) {
		for (file in new File(current).listFiles()) {
			if (file.path.endsWith(".gsp")) {
				gsps << file //file.path - "grails-app/views/"
			} else {
				findDevGsps file.path, gsps
			}
		}
	}
	
	/**
	 * 
	 */
	def findWarGsps(current, gsps) {
		for (path in SCH.servletContext.getResourcePaths(current)) {
			if (path.endsWith(".gsp")) {
				gsps << path //path - "/WEB-INF/grails-app/views/"
			} else {
				findWarGsps path, gsps
			}
		}
	}
	
	/**
	 * Check if a view exists under grails-app/views.
	 * @param arg Map: name
	 */
	def viewExists(arg) {
		if (log.traceEnabled) log.trace "viewExists(${arg.inspect()})"
		findGsps().any {
			if (log.traceEnabled) log.trace "viewExists(${arg.inspect()}): ${it.absolutePath} == ${arg.name}?"
			it.absolutePath.indexOf(arg.name) > -1 //==~ /.*arg.name/
		}
	}
	
	/**
	 * Check if a view exists under grails-app/views.
	 * @param arg Map: name
	 */
	def getView(arg) {
		if (log.traceEnabled) log.trace "getView(${arg.inspect()})"
		findGsps().find {
			if (log.traceEnabled) log.trace "getView(${arg.inspect()}): ${it} == ${arg.name}?"
			it.absolutePath.indexOf(arg.name) > -1 //==~ /.*${arg.name}/
		}
	}
	
	/**
	 * Get content of a view.
	 * @param arg Map: name
	 */
	def getViewText(arg) {
		if (log.traceEnabled) log.trace "getViewText(${arg.inspect()})"
		def view = getView(name: arg.name)
		if (log.traceEnabled) log.trace "getViewText(${arg.inspect()}): view=${view}"
		if (view) {
			try {
				if (log.traceEnabled) log.trace "getViewText(${arg.inspect()}): viewFile=${view.absolutePath}"
				view.withReader {
					r -> r.text
				}
			} catch (e) {
				// ignore, return nothing
			}
		}
	}
	
	/**
	 * Render a template.
	 * @param arg Map: name, model, out
	 */
	def render(arg) {
		if (log.traceEnabled) log.trace "render: arg=${arg.inspect()}"
		def text = getViewText(arg)
		def tmpFile
		def out
		if (text) {
			out = arg.out ?: new StringWriter()
			try {
				def tmpName = arg.name.replaceAll("/", "_")
				if (log.traceEnabled) log.trace "render(${arg.inspect()}): rendering view ${arg.name} -> ${tmpName} with model: ${arg.model}"
				def tmpl = groovyPagesTemplateEngine.createTemplate(text, tmpName).make(arg.model)
				tmpl.writeTo(out)
				if (log.traceEnabled) {
					try {
						tmpFile = new File("${tmpName}.gsp").write(out.toString())
					} catch (e) {
						log.warn "render(${arg.inspect()}): could not save generated template: ${e}"
					}
				}
			} catch (e) {
				e.printStackTrace()
				log.error "render(${arg.inspect()}): ERROR=${e}"
			}
		}
		if (log.traceEnabled && tmpFile) log.trace "render(${arg.inspect()}): produced template=${tmpFile.absolutePath}"
		out
	}
	
}
