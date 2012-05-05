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

/**
 * 
 */
class GlueFileService implements InitializingBean {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "prototype" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = true
	
	/**
	 * 
	 */
	def grailsApplication
	
	/**
	 * 
	 */
	def glueNameService
	
	/**
	 * 
	 */
	def glueControllerService
	
	/**
	 * 
	 */
	def glueDomainClassService
	
	/**
	 * 
	 */
	def glueMimeTypeService
	
	/**
	 * InitializingBean.
	 */
	void afterPropertiesSet() {
	}
	
	//
	// FILE UPLOAD
	//
	
	/**
	 * Save a BLOB in a property of a domain instance.
	 */
	def saveBlob(arg) {
		def inst = glueDomainClassService.getInstance(domain: arg.domain, id: arg.domainId.toLong())
		if (inst) {
			// Set properties if defined only
			try {
				inst."${arg.property}OriginalFilename" = arg.data.filename
				inst."${arg.property}MimeType" = arg.data.mimeType
			} catch (e) {
				log.warn "saveBlob: Cannot set original filename or mime type for ${arg.property}: ${e}"
			}
			// TODO One-to-many: use glueCrudService.associate!
			inst."${arg.property}" = glueDomainClassService.toBlob(arg.data.content)
			inst.validate()
			inst.save()
			log.info "Saved file ${arg.data.filename} length=${arg.data.content.length} to ${arg.domain}#${arg.domainId}.${arg.property}"
		} else {
			log.warn "Could not find instance of ${arg.domain}#${arg.domainId} for saving file upload ${arg.data.filename}"
		}
	}
	
	/**
	 * Convert controller parameters for file uploads into a map.
	 * Parameter must be named gxupload_<domain with number sign>_<property>_<id>.
	 */
	def upload(arg) {
		// Map with file names and their contents
		def map = [:]
		arg.params.each { k, v ->
			// Get file
			def file = v
			if (!file?.isEmpty()) {
				// Get mime type
				def mimeType = glueMimeTypeService.getMimeType(name: file.originalFilename)
				// Save text or bytes
				saveBlob(
					domain: glueNameService.getDomainClassPart(k),
					domainId: glueNameService.getIdPart(k),
					property: glueNameService.getPropertyPart(k),
					data: [
						content: mimeType?.browser?.startsWith("text") ? file.text : file.bytes,
						filename: file.originalFilename,
						mimeType: mimeType
					]
				)
				//f.transferTo(new File('someotherloc'))
			} else {
				log.warn "upload: File ${k} is empty, skipping"
			}
		}
		map
	}
	
}
