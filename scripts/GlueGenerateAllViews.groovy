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

import grails.spring.WebBeanBuilder
import groovy.text.SimpleTemplateEngine
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.codehaus.groovy.grails.commons.GrailsResourceLoaderFactoryBean
import org.codehaus.groovy.grails.commons.spring.GrailsResourceHolder
import org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator
import org.codehaus.groovy.grails.plugins.DefaultPluginMetaManager
import org.codehaus.groovy.grails.scaffolding.*
import org.springframework.mock.web.MockServletContext
import org.springframework.core.io.Resource
import org.apache.tools.ant.taskdefs.Ant

Ant.property(environment: "env")
grailsHome = Ant.project.properties."environment.GRAILS_HOME"

includeTargets << new File ("${grailsHome}/scripts/Package.groovy")

generateViews = true
generateController = false

target('default': "The description of the script goes here!") {
	depends(checkVersion, packageApp)
	typeName = "Domain Class"
	doStuff()
}

target(doStuff: "The implementation task") {
	rootLoader.addURL(classesDir.toURL())
	def beans = WebBeanBuilder().beans {
		resourceHolder(GrailsResourceHolder) {
			this.resources = "file:${basedir}/grails-app/domain/**/*.groovy"
		}
		grailsResourceLoader(GrailsResourceLoaderFactoryBean) {
			grailsResourceHolder = resourceHolder
		}
		def pluginResources = [] as Resource[]
		if (new File("${basedir}/plugins/*/plugin.xml").exists()) {
			pluginResources = "file:${basedir}/plugins/*/plugin.xml"
		}
		pluginMetaManager(DefaultPluginMetaManager, pluginResources)
		grailsApplication(DefaultGrailsApplication.class, ref("grailsResourceLoader"))
	}
	appCtx = beans.createApplicationContext()
	appCtx.servletContext = new MockServletContext()
	grailsApp = appCtx.grailsApplication
	grailsApp.initialise()
	def domainClasses = grailsApp.domainClasses
	if (!domainClasses) {
		println "Domain classes not found in grails-app/domain, trying hibernate mapped classes..."
		try {
			def config = new GrailsRuntimeConfigurator(grailsApp, appCtx)
			appCtx = config.configure(appCtx.servletContext)
		} catch (Exception e) {
			//println e.message
			e.printStackTrace()
		}
		domainClasses = grailsApp.domainClasses
	}
	if (domainClasses) {
		def generator = new DefaultGrailsTemplateGenerator()
		domainClasses.each { domainClass ->
			if (generateViews) {
				event("StatusUpdate", ["Generating views for domain class ${domainClass.fullName}"])
				generator.generateViews(domainClass,".")
			}
			if (generateController) {
				event("StatusUpdate", ["Generating controller for domain class ${domainClass.fullName}"])
				generator.generateController(domainClass,".")
			}
			event("StatusUpdate", ["Finished generation for domain class ${domainClass.fullName}"])
		}
		event("StatusFinal", ["Finished generation for domain classes"])
	} else {
		event("StatusFinal", ["No domain class found"])
	}
}
