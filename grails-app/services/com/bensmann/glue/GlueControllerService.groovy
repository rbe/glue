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
class GlueControllerService {
	
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
	def glueMimeTypeService
	
	/**
	 * Stream a file to client. Writes to HTTP resonse.
	 * @param arg Map: response, bytes, name, [mimeType, contentDisposition]
	 */
	def stream(arg) {
		// Content type and length
		arg.response.contentType = arg.mimeType ?: glueMimeTypeService.getMimeType(arg.name)
		arg.response.contentLength = arg.bytes.size()
		// Content disposition
		arg.response.setHeader("Content-disposition", "${arg.contentDisposition ?: "attachment"};filename=${arg.name}")
		// Stream bytes
		arg.response.outputStream << new ByteArrayInputStream(arg.bytes)
		// Flush stream
		arg.response.outputStream.flush()
	}
	
}
