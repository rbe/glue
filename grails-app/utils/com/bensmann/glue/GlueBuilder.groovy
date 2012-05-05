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

import groovy.util.BuilderSupport
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder

/**
 * 
 */
class GlueBuilder extends StreamingMarkupBuilder {
	
	/*
	protected void setParent(Object parent, Object child) {
		println "\nsetParent: ${parent?.name()} <- ${child?.name()}"
		if (child && parent) {
			parent.append(child)
		}
	}
	
	protected Object createNode(Object name) {
		createNode name, [:], null
	}
	
	protected Object createNode(Object name, Object value) {
		createNode name, [:], value
	}
	
	protected Object createNode(Object name, Map attributes) {
		createNode name, attributes, null
	}
	
	protected Object createNode(Object name, Map attributes, Object value) {
		println "createNode(name, attributes, value): ${name}, ${attributes}, ${value}"
		def parentName = getCurrentNode()?.parent()?.name()
		println "createNode(name, attributes, value): my parent is actually: ${parentName}"
		return new Node(getCurrentNode(), name, attributes, value)
	}
	
	protected Node getCurrentNode() {
		return (Node) getCurrent()
	}
	*/
	
	/*
	static def showNode(node) {
		def iter = node.iterator()
		while (iter.hasNext()) {
			def o = iter.next()
			println "${o?.getClass()} -> ${o?.name()}  ${o?.attributes()}"
		}
	}
	
	static void main(String[] args) {
		showNode(x)
		println "\n\n"
		def domain0 = x.get("domain")[0]
		showNode(domain0)
		println domain0.get("property")[0].get("render")[0].children()
		g.properties.each { println it }
		g.metaClass.methods.each { println it.name }
	}
	*/
	
}
