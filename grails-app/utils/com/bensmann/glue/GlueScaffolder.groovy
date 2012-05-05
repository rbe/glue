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

import org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator

/**
 * Helper for Grails + Glue scaffolding.
 */
class GlueScaffolder {
	
	/**
	 * Return all persistent properties of a domain class used in scaffolding:
	 * properties are persistent and should be display.
	 * @param arg Map: domainClass = DefaultGrailsDomainClass
	 * @return Sorted list of properties (according to domain class constraints)
	 */
	def static getProperties(arg) {
		def props = arg.domainClass?.properties.grep { !GlueConstants.excludedProperties.contains(it.name) }.findAll {
			(it.name == "id") || arg.domainClass.constrainedProperties[it.name]?.display
		} as List
		def comp = new DomainClassPropertyComparator(arg.domainClass) as java.util.Comparator
		Collections.sort(props, comp)
		props
	}
	
	/**
	 * Generate map for Grails UI datatable tag.
	 */
	def static getGrailsUiDataTableCols(arg) {
		def dataTableCols = []
		GlueScaffolder.getProperties(domainClass: arg.domainClass).each { p ->
			// TODO cp = domainClass.constrainedProperties[p.name] ... !cp.widget == "richtext"
			if (p.isAssociation() || (p.type in [java.sql.Blob, ([] as Byte[]).class])) {
				return
			}
			dataTableCols << [
				key: "'" + p.name + "'",
				label: "'" + p.naturalName + "'",
				sortable: true,
				resizeable: true
			]
		}
		dataTableCols
	}
	
}
