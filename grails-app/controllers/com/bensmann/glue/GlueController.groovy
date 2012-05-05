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

import grails.converters.JSON
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * 
 */
class GlueController implements ApplicationContextAware {
	
	/**
	 * 
	 */
	def useGlue = true
	
	/**
	 * Spring's application context.
	 */
	ApplicationContext applicationContext
	
	/**
	 * Glue's domain class service.
	 */
	def glueDomainClassService
	
	/**
	 * Glue's view service.
	 */
	def glueViewService
	
	/**
	 * The all-mighty Glue service.
	 */
	def glueCrudService
	
	/**
	 * 
	 */
	def glueOdiseeService
	
	/**
	 * Grails UI taglib service.
	 */
	def grailsUITagLibService
	
	/**
	 * The before interceptor.
	 */
	def beforeInterceptor = {
		responseNoCache()
	}
	
	//
	// RENDERING - TEMPLATES
	//
	
	/**
	 * Render view for domain instance.
	 */
	private def _render(arg) {
		if (log.traceEnabled) log.trace "_render: arg=${arg.inspect()}"
		// Lookup domain and domainId parameters for rendering
		if (!arg.domain && arg.g_inst) {
			arg.domain = arg.g_inst.class.name
			arg.domainId = arg.g_inst.id
		}
		// Render template
		if (arg.domain && arg.mode) {
			// Determine name of view directory: com.bensmann.cdm.Company -> company
			def name = arg.domain.split("\\.").toList().last()
			def d = name[0].toLowerCase() + name.substring(1)
			def tmpl = "${d}${File.separator}${arg.mode}.gsp"
			if (log.traceEnabled) log.trace "_render: params=${params.inspect()} arg=${arg.inspect()}, view-dir=${d} -> tmpl=${tmpl}"
			// We have an instance
			if (arg.g_inst) {
				// Ensure domain, domainID TODO Move into filters
				arg.domain = glueDomainClassService.getName(arg.g_inst)
				arg.domainId = arg.g_inst.id
				// Save instance in taglib service
				if (log.traceEnabled) log.trace "_render: Saved instance in glueTagLibService: ${arg.g_inst}, ${arg.g_inst.errors}"
				applicationContext.glueTagLibService.g_inst = arg.g_inst
			}
			//
			def rt = glueViewService.render(name: tmpl, model: [params: params] + arg)
			render rt ?: "Sorry, template ${tmpl} had errors or was not found!"
		} else {
			render "(_render: No domain or mode, arg=${arg.inspect()})"
		}
	}
	
	//
	// DOMAIN CLASSES - CRUD
	//
	
	/**
	 * 
	 */
	def private throwNoDomainInstanceException() {
		throw new IllegalStateException("No domain instance found: params=${params.inspect()}")
	}
	
	/**
	 * Create a fresh domain instance.
	 */
	def create = {
		if (log.traceEnabled) log.trace "create: params=${params.inspect()} flash=${flash.inspect()}"
		if (params.domain) {
			def inst = glueCrudService.create(domain: params.domain, gxdefaults: params.gxdefaults)
			if (inst) {
				_render(params + [g_inst: inst, mode: "create"])
			} else {
				throwNoDomainInstanceException()
			}
		} else {
			log.error "create: no domain, params=${params.inspect()}"
		}
	}
	
	/**
	 * Show a list of domain instances; just render list template.
	 */
	def list = {
		if (log.traceEnabled) log.trace "list: params=${params.inspect()} flash=${flash.inspect()}"
		_render(params + [mode: "list"])
	}
	
	/**
	 * Query a list of domain instances and return result as JSON.
	 */
	def queryAsJSON = {
		if (log.traceEnabled) log.trace "queryAsJSON: params=${params.inspect()} flash=${flash.inspect()}"
		def result = glueDomainClassService.query(params)
		if (log.traceEnabled) log.trace "queryAsJSON: params=${params.inspect()} -> result=${result}/${result.size()}"
		def json
		if (result) {
			def jsonData = []
			// Which attributes to use for creation of edit link?
			def editAttrs = [
				domain: params.domain
			]
			// Get properties by using first result
			def props = glueDomainClassService.getPersistentProperties(domain: result[0]).findAll {
				!(it.type in [Set.class, java.sql.Blob.class, ([] as Byte[]).class])
			}
			// Try to sort result by name
			try {
				result = result.sort { it.name }
			} catch (e) {
				// ignore
			}
			// Inspect every domain instance up to a maximum of params.max and create JSON data
			def idx = 0
			for (inst in result) {
				// Break at params.max
				if (idx++ == (params.max?.toInteger() ?: 20)) { // TODO Configure max default
					break
				}
				//
				def m = [id: inst.id, domainId: inst.id]
				// Add every persistent property to map
				props.each { p ->
					def k = p.name
					def v = inst."${k}"
					// No value?
					if (!v) {
						m.put(k, "")
					} else {
						if (log.traceEnabled) log.trace "queryAsJSON: result.each inst=${inst} p.type=${p.type}"
						try {
							switch (p.type) {
								case [java.util.Date.class, java.sql.Timestamp.class]:
									m.put(k, grailsUITagLibService.dateToJs(v))
									break
								// Number.class.isAssignableFrom(property.type)
								case [java.lang.Character, java.lang.String,
										boolean.class, Boolean.class,
										int.class, Integer.class,
										float.class, Float.class,
										double.class, Double.class,
										long.class, Long.class]:
									m.put(k, v as String)
									break
								default:
									// TODO Association!? Make property name configurable
									m.put(k, v.name as String)
							}
						} catch (e) {
							log.error "queryAsJSON: Cannot add property ${p.name} to JSON map: ${e}"
						}
					}
				}
				// Define URL for editing this instance
				m.dataUrl = g.createLink([
						controller: "glue",
						action: "edit",
						params: editAttrs + [domainId: inst.id]
					])
				m.dataUrlDiv = "crud_panel_left" // TODO Use param/attribute from GSP
				// Add map to JSON
				jsonData << m
			}
			json = [
				// Count all instances
				totalRecords: glueDomainClassService.countInstances(domain: params.domain),
				results: jsonData
			]
		} else {
			json = [
				totalRecords: 0,
				results: []
			]
		}
		if (log.traceEnabled) log.trace "queryAsJSON: params=${params.inspect()} json=${json as JSON}"
		render json as JSON
	}
	
	/**
	 * Show domain instance.
	 */
	def show = {
		if (log.traceEnabled) log.trace "show: params=${params.inspect()} flash=${flash.inspect()}"
		def inst = glueCrudService.read(domain: params.domain, domainId: params.domainId)
		if (inst) {
			_render(params + [g_inst: inst, mode: "show"])
		} else {
			throwNoDomainInstanceException()
		}
	}
	
	/**
	 * Edit an existing domain instance: load instance and render edit template.
	 */
	def edit = {
		if (log.traceEnabled) log.trace "edit: params=${params.inspect()} flash=${flash.inspect()}"
		def inst = glueCrudService.read(domain: params.domain, domainId: params.domainId)
		if (inst) {
			_render(params + [g_inst: inst, mode: "edit"])
		} else {
			throwNoDomainInstanceException()
		}
	}
	
	/**
	 * Update a domain instance.
	 */
	def update = {
		if (log.traceEnabled) log.trace "update: params=${params.inspect()} flash=${flash.inspect()}"
		// Process each form
		params.remove("gxforms")?.each { formName, formParameters ->
			if (log.traceEnabled) log.trace "update(${params.inspect()}): processing form ${formName}"
			def inst = glueCrudService.update(domain: formParameters.remove("domain"), domainId: formParameters.remove("domainId"), properties: formParameters)
			if (inst) {
				if (inst.hasErrors()) {
					// Add error message
					flash.message = "Could not update instance: ${inst}"
					log.error "${flash.message}: ${inst.errors}"
					_render(params + [g_inst: inst, mode: "edit"])
				} else {
					// Render response or redirect
					if (log.traceEnabled) log.trace "update: params=${params.inspect()}: checking nextAction"
					if (params.nextAction) {
						if (params.nextAction instanceof Map) {
							params.nextAction.params?.each { nak, nav ->
								params[nak] = nav
							}
							renderAssociation()
						}
					} else {
						_render(params + [g_inst: inst, mode: "show"])
					}
				}
			} else {
				throwNoDomainInstanceException()
			}
		}
	}
	
	/**
	 * Remove a domain instance.
	 */
	def delete = {
		/*if (log.traceEnabled) log.trace "delete: params=${params.inspect()} flash=${flash.inspect()}"
		glueCrudService.delete(domain: params.domain, id: params.domainId)
		_render(params + [mode: "delete"])*/
	}
	
	//
	// DOMAIN CLASSES - ASSOCIATION
	//
	
	/**
	 * Construct an answer after association action.
	 */
	def renderAssociation = {
		def m = [
			domain: params.domainA ?: params.domain,
			domainId: params.idA ?: params.domainId,
			property: params.propertyA ?: params.property,
			mode: params.mode ?: "show",
			update: params.update
		]
		if (log.traceEnabled) log.trace "renderAssociation: m=${m}"
		render gx.renderProperty(m)
	}
	
	/**
	 * Associate two domain instances.
	 */
	def associate = {
		if (log.traceEnabled) log.trace "associate: params=${params.inspect()} flash=${flash.inspect()}"
		glueCrudService.associate(params)
		renderAssociation()
	}
	
	/**
	 * Dissociate two domain instances.
	 */
	def dissociate = {
		if (log.traceEnabled) log.trace "dissociate: params=${params.inspect()} flash=${flash.inspect()}"
		glueCrudService.dissociate(params)
		renderAssociation()
	}
	
	/**
	 * Switch an association: associate if it does not exist otherwise delete it.
	 */
	def switchAssociation = {
		if (log.traceEnabled) log.trace "switchAssociation: params=${params.inspect()} flash=${flash.inspect()}"
		glueCrudService.switchAssociation(params)
		render ""
	}
	
	//
	// FILES
	//
	
	/**
	 * 
	 */
	def streamBlob = {
		try {
			def propInstance = glueDomainClassService.getPropertyValue(domain: params.domain, id: params.domainId, property: params.property)
			if (propInstance) {
				glueControllerService.stream(
					response: response,
					bytes: glueDomainClassService.toByteArray(propInstance),
					name: "${arg.propInstance}_${arg.id}.pdf",
					mimeType: "application/pdf",
					contentDisposition: "inline"
				)
			}
		} catch (e) {
			log.error "streamBlob(${params.inspect()}): Cannot stream BLOB: ${e}"
			throw e
		}
	}
	
	//
	// ODISEE
	//
	
	/**
	 * 
	 */
	def odisee = {
		def result = glueOdiseeService.getOdiseeRequest(domain: params.domain ?: "com.bensmann.cdm.project.Project", domainId: params.domainId ?: 1)
		render result ?: "NO RESULT"
	}
	
}
