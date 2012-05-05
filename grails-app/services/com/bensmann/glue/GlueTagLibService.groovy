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
class GlueTagLibService {
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "session" // prototype request flash flow conversation session singleton
	
	/**
	 * Transactional?
	 */
	boolean transactional = false
	
	/**
	 * 
	 */
	def glueConfigurationService
	
	/**
	 * 
	 */
	def glueDomainClassService
	
	/**
	 * 
	 */
	def map = [:]
	
	/**
	 * 
	 */
	def g_inst
	
	/**
	 * 
	 */
	def originalMode
	
	/**
	 * Default mode: when attribute mode is missing, use instance variable originalMode or use "show"
	 */
	private def _checkMode(model) {
		if (!model.mode) {
			model.mode = originalMode ?: "show"
			if (log.traceEnabled) log.trace "checkModel: no mode, set to ${model.mode} (originalMode=${originalMode})"
		}
	}
	
	/**
	 * 
	 */
	private def _checkDomain(model) {
		if (log.traceEnabled) log.trace "_checkDomain(${model}): g_inst=${g_inst}, ${g_inst.errors}"
		// Retrieve instance using domain.domainId; check if already existing g_inst == domain.domainId
		if (model.domain && model.domainId && (g_inst?.id != model.domainId)) {
			model.g_inst = glueDomainClassService.getInstance(domain: model.domain, id: model.domainId)
			g_inst = model.g_inst
			if (log.traceEnabled) log.trace "checkModel: model.g_inst=${model.g_inst} using getInstance(${model.domain}, ${model.domainId})"
		}
		// An already loaded domain instance (e.g. by controller)
		else if (/*(!model.domain || !model.domainId) && */g_inst) {
			model.g_inst = g_inst
			if (log.traceEnabled) log.trace "checkModel: Setting model.g_inst to g_inst=${g_inst}"
		}
		assert model.g_inst != null
		// Ensure domain and domain id in attributes
		if (model.g_inst) {
			model.domain = glueDomainClassService.getName(model.g_inst)
			model.domainId = model.g_inst.id
			if (log.traceEnabled) log.trace "checkModel: Adjusted model.domain/domainId in flavor of model.g_inst to ${model.domain}#${model.domainId}"
		} else {
			log.warn "checkModel: Could not ensure domain/domainId, no instance"
		}
		// The domain class
		if (model.domain) {
			model.g_instDc = glueDomainClassService.grailsDomainClass(domain: model.domain)
			if (log.traceEnabled) log.trace "checkModel: Set model.g_instDc to ${model.g_instDc}"
		}
	}
	
	/**
	 * 
	 */
	private def _checkProperty(model) {
		if (model.g_instDc && model.property) {
			model.g_property = glueDomainClassService.getProperty(domain: model.domain, property: model.property)
			// TODO model.g_propertyType = model.g_property.type
			model.g_propertyType = glueDomainClassService.getPropertyType(domain: model.domain, property: model.property)
			model.g_constrainedProperty = model.g_instDc.constrainedProperties?."${model.property}"
		} else {
			model.g_property = null
			model.g_propertyType = null
			model.g_constrainedProperty = null
			model.g_widget = null
			if (log.traceEnabled) log.trace "checkModel: Nulled model.g_*property*"
		}
		// Get count of associated property
		if (model.g_property) {
			def count = 1
			if (model.g_property.oneToMany || model.g_property.manyToMany) {
				// TODO Lookup size of possible values not size of associated values
				count = model.g_inst."${model.property}"?.size()
			}
			model.g_widget = glueConfigurationService.getWidget(domain: model.domain, property: model.property, count: count)
			if (!model.g_widget) {
				model.g_widget = "list" // Default is list
			}
			model.type = model.g_widget // TODO Replace type with g_widget
		}
		if (log.traceEnabled) log.trace "checkModel: Set property objects: g_property/Type=${model.g_property} ${model.g_propertyType}, g_constrainedProperty=${model.g_constrainedProperty}"
	}
	
	/**
	 * Create model to work with: domain instance, class and attributes.
	 */
	def checkModel(arg) {
		def start = System.currentTimeMillis()
		if (log.traceEnabled) log.trace "checkModel: arg=${arg.inspect()}"
		// Initialize model
		def model = arg + map
		// Mode
		_checkMode(model)
		// The domain instance and class
		_checkDomain(model)
		// Property objects
		_checkProperty(model)
		//
		if (log.traceEnabled) log.trace "checkModel: got arg=${arg.inspect()} produced model=${model}"
		if (log.traceEnabled) log.trace "checkModel: ${model.domain}#${model.domainId}.${model.property} took ${System.currentTimeMillis() - start} ms"
		// TODO Cache model
		model
	}
	
	/**
	 * 
	 */
	def inEditMode() {
		originalMode == "edit"
	}
	
}
