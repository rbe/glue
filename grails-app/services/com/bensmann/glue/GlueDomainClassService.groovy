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

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor as Events
import org.hibernate.Hibernate
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * 
 */
class GlueDomainClassService implements ApplicationContextAware {
	
	/**
	 * Spring's application context.
	 */
	ApplicationContext applicationContext
	
	/**
	 * The scope. See http://www.grails.org/Services.
	 */
	def scope = "singleton" // prototype request flash flow conversation session singleton
	
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
	static def excludedProps = [
		'id',
		'version',
		'dateCreated',
		'lastUpdated',
		'dirty',
		Events.ONLOAD_EVENT,
		Events.BEFORE_DELETE_EVENT,
		Events.BEFORE_INSERT_EVENT,
		Events.BEFORE_UPDATE_EVENT
	]
	
	/**
	 * Convert a byte array into a java.sql.Blob.
	 * @return java.sql.Blob
	 */
	def toBlob(byteArray) {
		Hibernate.createBlob(byteArray)
	}
	
	/**
	 * Convert a java.sql.Blob property into a byte array.
	 * @return byte[]
	 */
	def toByteArray(prop) {
		prop?.getBytes(1L, prop.length().toInteger())
	}
	
	/**
	 * Get representation for domain instance: name or id (used with GlueWidgetService/associationXXXAsHTML).
	 */
	def pr(arg) {
		if (arg.domain instanceof String || arg.domain instanceof GString) {
			// ignore
		} else {
			def n = arg.domain.properties.any { k, v -> k == "name" }
			def v = n ? arg.domain.name : arg.domain.id
			if (log.traceEnabled) log.trace "pr(${arg.inspect()}): v=${v}"
			v
		}
	}
	
	/**
	 * Get name of a domain class regardless if arg is a String or a Class.
	 */
	def getName(arg) {
		if (!arg) return
		def r
		if (arg instanceof GString || arg instanceof String) {
			r = arg as String
		} else if (arg instanceof GrailsClass) {
			r = arg.fullName
		} else if (arg instanceof Class) {
			r = arg.name
		} else /*if (arg instanceof Object)*/ {
			r = arg.class.name
		}
		if (log.traceEnabled) log.trace "getName: arg=${arg.inspect()}/${arg.class} -> ${r}"
		r
	}
	
	/**
	 * Get DefaultGrailsDomainClass.
	 * @param arg A name (String), Class or map (key: domain) of domain class.
	 */
	def grailsDomainClass(arg) {
		assert arg
		def dc
		// We got a DefaultGrailsDomainClass...
		if (arg instanceof DefaultGrailsDomainClass) {
			return arg
		}
		if (arg instanceof Class) {
			dc = grailsApplication.getDomainClass(arg.name as String)
		} else if (arg instanceof Map) {
			dc = grailsApplication.getDomainClass(getName(arg.domain) as String)
		} else if (arg instanceof String || arg instanceof GString) {
			dc = grailsApplication.getDomainClass(arg as String)
		} else if (arg instanceof Object) { // A domain instance!?
			dc = grailsApplication.getDomainClass(arg.class.name)
		}
		// Check
		assert dc
		// Check against DefaultGrailsDomainClass
		assert dc.class == DefaultGrailsDomainClass
		if (log.traceEnabled) log.trace "grailsDomainClass(${arg.inspect()}/${arg.class}): return ${dc}/${dc.class}"
		dc
	}
	
	/**
	 * Return all persistent properties of a domain class.
	 */
	def getPersistentProperties(arg) {
		grailsDomainClass(arg.domain)?.persistentProperties
	}
	
	/**
	 * Get a certain property of a domain class.
	 * @param arg Map: name: Name of a domain class,
	 * 		prop Name of property to return.
	 */
	def getProperty(arg) {
		grailsDomainClass(domain: getName(arg.domain))?.getPropertyByName(arg.property)
	}
	
	/**
	 * Get value of a certain property of a domain instance.
	 * @param arg Map: name: Name of a domain class,
	 * 		id ID of instance of domain class to use,
	 * 		prop Name of property to return.
	 */
	def getPropertyValue(arg) {
		grailsApplication.getClassForName(getName(arg.domain))?.get(arg.id)."${arg.property}"
	}
	
	/**
	 * Get type of a certain property of a domain class.
	 * @param arg Map: name: Name of a domain class,
	 * 		prop Name of property to return.
	 */
	// TODO Rename to getPropertyType"Class"?
	def getPropertyType(arg) {
		getProperty(arg)?.referencedPropertyType
	}
	
	/**
	 * Get class of a certain property of a domain class.
	 * @param arg Map: name: Name of a domain class,
	 * 		prop Name of property to return.
	 */
	// TODO Name of method misleading
	def getPropertyClass(arg) {
		getProperty(arg)?.referencedDomainClass
	}
	
	/**
	 * Get a list of interesting properties from a domain class.
	 * @param arg Map: domain: Name of domain class
	 * 		type: List of types we are interested in
	 */
	def getPropertyByType(arg) {
		def typeList = arg.type instanceof List ? arg.type : [arg.type]
		grailsDomainClass(arg.domain)?.properties.findAll { prop ->
			prop.type in typeList
		}
	}
	
	/**
	 * Find an association.
	 */
	def findAssociation(arg) {
		// Find existing memoized associations
		def assoc = GlueAssoc.withCriteria {
				eq("domainA", arg.domainA)
				eq("domainIdA", arg.domainIdA.toLong())
				eq("propertyA", arg.propertyA)
				if (arg.domainB) eq("domainB", arg.domainB)
				if (arg.domainIdB) eq("domainIdB", arg.domainIdB.toLong())
			}
		// When no memoized associations were found, try to auto-discover
		// TODO Try always to auto-discover (as domain instances can be created out of Glue)?
		if (!assoc/* && autoDiscoverMode*/) {
			if (log.traceEnabled) log.trace "findAssociation(${arg.inspect()}): nothing found in GlueAssoc, trying to auto-discover"
			// Create name for property
			def domainAName = grailsDomainClass(arg.domainA).name
			def domainAPropertyName = domainAName[0].toLowerCase() + domainAName.substring(1)
			// Get referenced property domainA.propertyA -> domainB
			def propertyBClass = getPropertyClass(domain: arg.domainA, property: arg.propertyA)
			try {
				// Get property
				def otherProperty = propertyBClass.getPropertyByName(domainAPropertyName)
				////println "... domainAName=${domainAName}, domainAPropertyName=${arg.domainB}.${domainAPropertyName}, bidirectional=${otherProperty.bidirectional}"
				// Is property bi-directional?
				if (otherProperty.bidirectional) {
					// Lookup all domain instances of property
					def propertyBType = getPropertyType(domain: arg.domainA, property: arg.propertyA)
					if (log.traceEnabled) log.trace "findAssociation(${arg.inspect()}): propertyBType.withCriteria ${domainAPropertyName}.id eq ${arg.domainIdA}"
					assoc = propertyBType.withCriteria {
						"${domainAPropertyName}" {
							eq("id", arg.domainIdA.toLong())
						}
					}?.collect {
						// Create GlueAssoc instances and return them
						// TODO Create as method: GlueDomainClassService.rememberAssoc
						def ga = new GlueAssoc(
								domainA: arg.domainA, domainIdA: arg.domainIdA, propertyA: arg.propertyA,
								domainB: arg.domainB, domainIdB: it.id, active: true
							).save(flush: true)
						ga.refresh()
						ga
					}
				}
			} catch (org.codehaus.groovy.grails.exceptions.InvalidPropertyException e) {
				if (log.traceEnabled) log.trace "findAssociation(${arg.inspect()}): ${e}"
			}
		}
		if (log.traceEnabled) log.trace "findAssociation(${arg.inspect()}): assoc=${assoc}"
		assoc
	}
	
	/**
	 * Get all possible values for a certain property.
	 * TODO Include already associated values!? See GlueWidgetService.associationList|Checkbox|RadioGroupAsHTML
	 */
	def getPossibleValues(arg) {
		if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()})"
		def possibleValues
		if (/*arg.inst && */arg.property) {
			def propertyType = getPropertyType(domain: arg.domain, property: arg.property)
			if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()}): propertyType=${propertyType}"
			if (propertyType) {
				// One-to-my-many?
				def glueConfigurationService = applicationContext.getBean("glueConfigurationService")
				def isOneToMyMany = glueConfigurationService.isOneToMyManyMapping(domain: arg.domain, property: arg.property)
				if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()}): isOneToMyManyMapping=${isOneToMyMany}"
				if (isOneToMyMany) {
					if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()}): isOneToMyManyMapping=${isOneToMyMany}"
					// Lookup GlueAssoc
					def propertyClass = getPropertyClass(domain: arg.domain, property: arg.property)
					if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()}): propertyClass=${propertyClass}"
					def assocId = findAssociation(
							domainA: arg.domain, domainIdA: arg.domainId, propertyA: arg.property,
							domainB: propertyClass.fullName
						)?.collect {
							it.domainIdB
						}
					// Get list with possible values
					if (assocId) {
						possibleValues = propertyType.withCriteria {
							"in"("id", assocId)
						}
						if (log.traceEnabled) log.trace "getPossibleValues(${arg.inspect()}): possibleValues=${possibleValues}"
					} else {
						log.warn "getPossibleValues(${arg.inspect()}): found no possibleValues"
					}
				} else {
					possibleValues = propertyType.list()
				}
				// Sort values
				try {
					possibleValues = possibleValues.sort { it.name }
				} catch (e) {
					// ignore
				}
			} else {
				throw new IllegalStateException("getPossibleValues(${arg.inspect()}): no property type found")
			}
		} else {
			throw new IllegalStateException("getPossibleValues(${arg.inspect()}): no property given")
		}
		possibleValues
	}
	
	/**
	 * Generate a HQL WHERE-clause for properties of a domain class.
	 * @param arg Map: domain: domain instance, types: list of type of properties
	 */
	private def propsToQuery(arg) {
		def whereClause = []
		def props = arg.domain?.properties.findAll { p ->
			!(p.name in ["id", "version"]) && (arg.types ? p.type in arg.types : true)
		}
		props?.eachWithIndex { p, i ->
			if (p.isPersistent()) {
				whereClause << (arg.path << "." << p.name << " like :${arg.placeholder ?: p.name}")
			}
		}
		if (log.traceEnabled) log.trace "arg=${arg.inspect()} -> ${whereClause}"
		whereClause
	}
	
	/**
	 * Generate a HQL WHERE-clause for all String properties of a domain class.
	 */
	private def stringPropsToQuery(path, dc, placeholder = null) {
		def x = []
		dc.properties.findAll { p ->
			p.type == String
		}?.eachWithIndex { p, i ->
			if (p.isPersistent()) {
				x << (path << "." << p.name << " like :${placeholder ?: p.name}")
			}
		}
		x
	}
	
	/**
	 * Create a new instance of a domain class.
	 * @param arg Map: domain: Name of domain class.
	 */
	def newInstance(arg) {
		def c = getName(arg.domain)
		def i = grailsApplication.getClassForName(getName(arg.domain))?.newInstance()
		if (log.traceEnabled) log.trace "newInstance: arg=${arg.inspect()} c=${c} -> i=${i}"
		i
	}
	
	/**
	 * Get exactly one instance of a domain class.
	 * @param arg Map: domain: name of domain class, id: ID of instance
	 * 		and/or property: map with property values for domain instance to find.
	 */
	// TODO Change arg.id to arg.domainId
	def getInstance(arg) {
		def start = System.currentTimeMillis()
		if (log.traceEnabled) log.trace "getInstance: arg=${arg.inspect()}"
		def dc
		// It's already an domain instance and we should not find it by id
		if (!(arg.domain instanceof String || arg.domain instanceof GString) && !(arg.id && arg.property)) {
			return arg.domain
		}
		// Lookup an instance by properties
		else if (arg.domain && arg.property instanceof Map) {
			def result = findInstance(arg)
			assert result.size() <= 1
			return result[0] ?: null
		}
		// Get class
		else if (arg.domain instanceof String || arg.domain instanceof GString) {
			dc = grailsApplication.getClassForName(arg.domain as String)
		} else if (arg.domain instanceof Class) {
			dc = arg.domain
		}
		def result
		if (dc && arg.id) {
			result = dc.get(arg.id)
		} else {
			log.warn "getInstance: Can't find instance of domain ${arg.domain} without id"
		}
		if (log.traceEnabled) log.trace "getInstance: ${arg.inspect()} -> ${result} took ${System.currentTimeMillis() - start} ms"
		result
	}
	
	/**
	 * Find one or more instance(s) of a domain class.
	 * @param arg Map: domain: name of domain class
	 * 		property: map with property values for domain instance to find.
	 */
	def findInstance(arg) {
		def dc = grailsDomainClass(arg.domain)
		def query = "select o from ${getName(arg.domain)} as o where " << arg.property.collect { k, v -> "o.${k} like :${k}" }.join(" and ")
		def namedParameter = arg.property
		// Get class
		def c = dc instanceof Class ? dc : grailsApplication.getClassForName(getName(dc))
		// Execute query
		def result = c.executeQuery(query.toString(), namedParameter).flatten()
		if (log.traceEnabled) log.trace "findInstance: HQL: ${query} namedParameter=${namedParameter} result=${result}/${result.size()} (must be <= 1)"
		result
	}
	
	/**
	 * Just return count of all instances.
	 */
	def countInstances(arg) {
		if (log.traceEnabled) log.trace "countInstances(${arg.inspect()})"
		def dc = grailsApplication.getClassForName(getName(arg.domain))
		dc.count()
	}
	
	/**
	 * Return a list of all instances of arg.domain.
	 */
	def listInstances(arg) {
		if (log.traceEnabled) log.trace "listInstances(${arg.inspect()})"
		def dc = grailsApplication.getClassForName(getName(arg.domain))
		// TODO What to do if domain class has no property name?
		dc.list([offset: arg.offset ?: 0/*, max: arg.max*/, sort: "name", order: "asc"])
	}
	
	/**
	 * Find a domain class and return a list of one or more domain instances.
	 */
	def query(arg) {
		def start = System.currentTimeMillis()
		if (log.traceEnabled) log.trace "query(${arg.inspect()})"
		def result = []
		// Lookup domain instance(s) by field
		if (arg.domain && arg.property && arg.query) {
			// Query and show which property?
			if (!arg.showProperty) {
				arg.showProperty = "name" // TODO Submit via controller action queryAsJSON
			}
			// One-to-my-many?
			def glueConfigurationService = applicationContext.getBean("glueConfigurationService")
			def isOneToMyMany = glueConfigurationService.isOneToMyManyMapping(domain: arg.domain, property: arg.property)
			// Find
			def referencedPropertyType = getPropertyType(domain: arg.domain, property: arg.property)
			result = referencedPropertyType.withCriteria {
				// TODO Use context
				ilike arg.showProperty, "%" + arg.query + "%"
				order arg.showProperty, "${arg.order ?: 'desc'}"
				//
				if (arg.offset) {
					firstResult(arg.offset)
				}
				//
				if (arg.max) {
					maxResults(arg.max)
				}
			}
		}
		// Fulltext search in all properties
		else if (arg.domain && arg.query) {
			result = ftSearch(arg/*domain: arg.domain, query: arg.query*/)
		}
		// Just list all instances
		else if (arg.domain) {
			result = listInstances(arg)
		}
		if (log.traceEnabled) log.trace "query: ${arg.inspect()} -> ${result}/${result?.size()} took ${System.currentTimeMillis() - start} ms"
		assert result instanceof List
		result ?: []
	}
	
	/**
	 * Full text search within a domain instance and all its relations.
	 * @param arg Map: domain: List with names of domain classes, query: String to find.
	 */
	def ftSearch(arg) {
		def start = System.currentTimeMillis()
		// Parameter dc must be GrailsClass; not DefaultGrailsDomainClass
		def queryDomainClass = { dc, query ->
			def from = ["o": grailsDomainClass(dc).name]
			def fields = stringPropsToQuery("o", grailsDomainClass(dc), "q")
			def props = grailsDomainClass(dc).properties.findAll { p ->
				p.isAssociation()
			}.eachWithIndex { p, i ->
				from = from + ["o${i}": p.name]
				fields = fields + stringPropsToQuery("o${i}", grailsDomainClass(p.referencedDomainClass), "q")
			}
			def m = "select o from " << from.collect { k, v -> "${v} as ${k}" }.join(" left join o.") << " where " << fields.join(" or ")
			def namedParameters = [q: "%" + query + "%"]
			def paginateParameters = [
				offset: arg.offset ?: 0,
				max: arg.max
			]
			if (log.traceEnabled) log.trace "ftSearch: ${m.toString()} ${namedParameters} ${paginateParameters}"
			// Get class
			def c = dc instanceof String ? grailsApplication.getClassForName(dc) : dc
			// Execute query TODO Remove duplicates through HQL; distinct?
			c.executeQuery(m.toString(), namedParameters, paginateParameters).flatten()
		}
		// We want a list...
		def d = arg.domain instanceof List ? arg.domain : [arg.domain]
		// For every domain class
		def result = d.collect { dc ->
			if (grailsDomainClass(dc)) {
				queryDomainClass(dc, arg.query)
			} else {
				log.error "No domain class found for ${dc}"
			}
		}.flatten().findAll {
			it != null
		}.unique()
		if (log.traceEnabled) log.trace "ftSearch: ${arg.inspect()} -> ${result} took ${System.currentTimeMillis() - start} ms"
		result
	}
	
}
