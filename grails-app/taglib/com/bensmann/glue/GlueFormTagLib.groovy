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

class GlueFormTagLib implements ApplicationContextAware {
	
	/**
	 * Spring's application context.
	 */
	ApplicationContext applicationContext
	
	/**
	 * Our namespace.
	 */
	static namespace = "gx"
	
	/**
	 * Glue's name service.
	 */
	def glueNameService
	
	/**
	 * Which tags return objects?
	 */
	static returnObjectForTags = []
	
	/**
	 * Render a glue form.
	 */
	def form = { attr, body ->
		if (log.traceEnabled) log.trace "form: attr=${attr.inspect()}"
		// Map
		def m = [:]
		// Set unique id and name for this form
		def formPrefix = "gxform${glueNameService.uniqueName()}"
		applicationContext.glueTagLibService.map.formPrefix = formPrefix
		if (!m.name) {
			m.id = m.name = formPrefix
		}
		// Hidden values for form submission
		def hiddenValues = new StringBuilder()
		hiddenValues <<
			"<input type=\"hidden\" name=\"${formPrefix}_domain\" value=\"${attr.domain}\"/>" <<
			"<input type=\"hidden\" name=\"${formPrefix}_domainId\" value=\"${attr.domainId}\"/>"
		// Multipart form needed? TODO Place this in glueDomainClassService.isMultipart
		def multiPart = attr.g_instDc.properties.any { p ->
			p.type in [java.sql.Blob]
		}
		if (multiPart) {
			m.enctype = "multipart/form-data"
			m.method = "post"
			m.onsubmit = "return GX.fileupload.submit(this, {'onStart': GX.fileupload.startCallback, 'onComplete': GX.fileupload.completeCallback})"
		}
		// Glue URL
		m.url = [
			controller: "glue",
			action: "update"
		]
		out << g.form(m) {
				body() << hiddenValues.toString()
			}
	}
	
}
