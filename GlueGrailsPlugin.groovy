import com.bensmann.glue.*
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.ServiceArtefactHandler

/**
 * 
 */
class GlueGrailsPlugin {
	
	/**
	 * Author and plugin.
	 */
	def author = "Ralf Bensmann"
	def authorEmail = "grails@bensmann.com"
	def title = "Glue -- GORM and scaffolding on steroids"
	def description = """Several helpers for building Grails applications."""
	
	/**
	 * URL to the plugin's documentation.
	 */
	def documentation = "http://grails.org/glue+Plugin"
	
	/**
	 * The plugin version.
	 */
	def version = "1.3.1"
	
	/**
	 * The version or versions of Grails the plugin is designed for.
	 */
	def grailsVersion = "1.3.1 > *"
	
	/**
	 * Other plugins this plugin depends on.
	 */
	def dependsOn = [
		controllers: GrailsUtil.grailsVersion,
		core: GrailsUtil.grailsVersion,
		hibernate: GrailsUtil.grailsVersion,
		cxf: "0.5.1 > *"
	]
	
	/**
	 * 
	 */
	def loadAfter = [
		"controllers",
		"hibernate"
	]
	
	/**
	 * Other plugins influenced by this plugin.
	 * See http://www.grails.org/Auto+Reloading+Plugins
	 */
	def influences = [
		"controllers",
		"hibernate"
	]
	
	/**
	 * Plugins to observe for changes.
	 * See http://www.grails.org/Auto+Reloading+Plugins
	 */
	def observe = [
		"controllers",
		"hibernate"
	]
	
	/**
	 * Resources to watch.
	 * See http://www.grails.org/Auto+Reloading+Plugins
	 */
	def watchedResources = []
	
	/**
	 * Resources that are excluded from plugin packaging.
	 */
	def pluginExcludes = [
		"grails-app/views/"
	]
	
	/**
	 * Implement runtime spring config (optional).
	 * See http://www.grails.org/Runtime+Configuration+Plugins
	 */
	def doWithSpring = {
		// Constraints
		//ConstrainedProperty.registerNewConstraint(BestFrameworkConstraint.NAME, BestFrameworkConstraint.class);
	}
	
	/**
	 * Implement post initialization spring config (optional).
	 * See http://www.grails.org/Runtime+Configuration+Plugins
	 */
	def doWithApplicationContext = { applicationContext ->
	}
	
	/**
	 * Implement additions to web.xml (optional).
	 * See http://www.grails.org/Runtime+Configuration+Plugins
	 */
	def doWithWebDescriptor = { xml ->
	}
	
	/**
	 * Inject methods into controller.
	 * @param ctx Context.
	 * @param c Grails artefact.
	 */
	private def injectMethodsIntoController(ctx, c) {
		println "${this.class.name}: injecting methods into controller ${c}"
		/**
		 * The before interceptor.
		 * See http://www.grails.org/Controllers+-+Interceptors
		c.metaClass.beforeInterceptor = {
			responseNoCache()
		}
		 */
		/**
		 * Don't cache our response.
		 */
		c.metaClass.responseNoCache = { ->
			response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate,max-age=0")
		}
		/**
		 * Which parameters do we need? If one is missing, throw an exception.
		 */
		c.metaClass.checkParameter = { arg ->
			arg.p.each {
				if (!arg.params."${it}") {
					throw new IllegalArgumentException("Parameter ${it} missing")
				}
			}
		}
		/**
		 * Service hooks. Call a service method or closure.
		 */
		c.metaClass.callService = { name, Object... params = null ->
			def servicePropertyName = ctx.getBean("glueNameService").toServicePropertyName(c)
			def serviceProp = groovyService.getProperty(c, servicePropertyName)
			if (!serviceProp) {
				println "No service property ${servicePropertyName} found!"
			} else {
				println "Calling method/closure ${name} with ${params.inspect()} on property ${servicePropertyName}"
				groovyService.callMethodOrClosure(serviceProp, name, params)
			}
		}
	}
	
	/**
	 * 
	 * @param ctx Context.
	 * @param c Grails artefact.
	 */
	private def injectCrudMethodsIntoController(ctx, c) {
		/**
		 * CRUD - Search.
		c.metaClass.qg_ftSearch = { arg ->
			def result
			// If domain is missing, get corresponding domain class name for controller
			if (!arg.domain) {
				arg.domain = [ctx.getBean("glueNameService").toFullDomainClassName(c)]
			}
			if (arg.query) {
				result = glueDomainClassService.ftSearch(domain: arg.domain, query: arg.query)
			}
			println "Queryied domain(s): ${arg.domain} with query ${arg.query} -> ${result}"
			result
		}
		 */
		/**
		 * CRUD - File upload.
		c.metaClass.qg_fileUpload = { arg ->
			glueFileService.uploadFile(params: arg.params, request: arg.request)
		}
		c.metaClass.qg_streamBlob = { arg ->
			glueFileService.streamBlob(
				domain: arg.domain, id: arg.id, property: arg.property,
				response: response
			)
		}
		 */
	}
	
	/**
	 * Inject services into controller.
	 * @param ctx Context.
	 * @param c Grails artefact.
	 */
	private def injectServicesIntoController(ctx, c) {
		//ctx.beanDefinitionNames.sort { it }.each { println it }
		//println "${this.class.name}: injecting services into controller ${c}"
		// Inject corresponding service object
		def correspondingServiceProperty = ctx.getBean("glueNameService").toServicePropertyName(c)
		try {
			def correspondingServiceBean = ctx.getBean(correspondingServiceProperty)
			if (correspondingServiceProperty) {
				println "${this.class.name}: injecting service ${correspondingServiceBean} in property ${correspondingServiceProperty} of controller ${c}"
				c.metaClass."${correspondingServiceProperty}" = correspondingServiceBean
			}
		} catch (e) {
			println "${this.class.name}: ${c} has no corresponding service object ${correspondingServiceProperty}"
		}
		// Inject standard services
		["groovyService", "glueGrailsService", "glueNameService", "glueDomainClassService", "glueMimeTypeService", "glueViewService"].each {
			def bean = ctx.getBean(it)
			c.metaClass."${it}" = bean
			println "${this.class.name}: injecting service ${bean} in controller ${c}"
		}
	}
	
	/**
	 * Put some glue in a controller...
	 */
	private def controllerGlue(ctx, c) {
		def useGlue = ctx.getBean("groovyService").getProperty(c.newInstance(), "useGlue")
		//println "controllerGlue: ctx=${ctx} c=${c} useGlue=${useGlue}"
		if (useGlue) {
			injectServicesIntoController(ctx, c)
			injectMethodsIntoController(ctx, c)
		}
	}
	
	/**
	 * Put some glue in all controllers...
	 */
	private def allControllersGlue(application, ctx) {
		application.controllerClasses.each { c ->
			controllerGlue(ctx, c)
		}
	}
	
	/**
	 * Put some glue in a domain class...
	 */
	private def domainClassGlue(ctx, d) {
		def groovyService = ctx.getBean("groovyService")
		def inst = d.newInstance()
		def useGlue = groovyService.getProperty(inst, "useGlue")
		println "${this.class.name}: Analyzing domain class ${d}, useGlue=${useGlue}"
		if (useGlue) {
			// TODO beforeUpdate = no update; just create new version, maybe archive old one
		}
		// Read property "glue"
		try {
			def v = groovyService.getProperty(inst, "glueConstraints")
			if (v) {
				def glueConfigurationService = ctx.getBean("glueConfigurationService")
				def xml = new GlueBuilder().bind { mkp.yield v }
				glueConfigurationService.setDomainClass(d, xml.toString())
			}
		} catch (e) {
			println "${this.class.name}: Could not process glueConstraints of ${d}: ${e}"
		}
		/*/ Odisee
		try {
			def v = groovyService.getProperty(inst, "odisee")
			if (v) {
				def glueConfigurationService = ctx.getBean("glueConfigurationService")
				def xml = new GlueBuilder().bind { mkp.yield v }
				println ""
				println xml.toString()
			}
		} catch (e) {
			println "${this.class.name}: Could not process odisee configuration of ${d}: ${e}"
		}
		*/
	}
	
	/**
	 * Put some glue in all domain classes...
	 */
	private def allDomainClassesGlue(application, ctx) {
		application.domainClasses.each { d ->
			domainClassGlue(ctx, d)
		}
	}
	
	/**
	 * TODO
	private def registerConstraint(constraintClass, usingHibernate, applicationContext) {
		println "registerConstraint: ${constraintClass}.name"
		def constraintName = constraintClass.name
		if (usingHibernate) {
			ConstrainedProperty.registerNewConstraint(constraintName, new CustomConstraintFactory(applicationContext, constraintClass))
		} else {
			// Don't allow persistent constraints if hibernate is not being used
			ConstrainedProperty.registerNewConstraint(constraintName, new CustomConstraintFactory(constraintClass))
		}
	}
	*/
	
	/**
	 * Implement registering dynamic methods to classes (optional).
	 * See http://www.grails.org/Plugin+Dynamic+Methods
	 */
	def doWithDynamicMethods = { ctx ->
		// Initialize mime types
		println "${this.class.name}: Initializing mime types"
		GlueMimeType.mimeTypes.each { mt ->
			if (GlueMimeType.countByExtension(mt.extension) == 0) {
				GlueMimeType.withTransaction { // No Hibernate session bound to thread
					new GlueMimeType(name: mt.name, extension: mt.extension, browser: mt.browser).save()
				}
			}
		}
		// Controllers
		allControllersGlue(application, ctx)
		/*// Constraints
		def usingHibernate = manager.hasGrailsPlugin("hibernate")
		for (GrailsConstraintClass c in application.constraintClasses) {
			registerConstraint(c, usingHibernate, applicationContext)
		}*/
		// Domain Classes
		allDomainClassesGlue(application, ctx)
		// Tasks
		//application.taskClasses.each { j -> }
	}
	
	/**
	 * Implement code that is executed when any artefact that this plugin is
	 * watching is modified and reloaded. The event contains: event.source,
	 * event.application, event.manager, event.ctx, and event.plugin.
	 */
	def onChange = { event ->
		println "${this.class.name}: onChange"
		// Controllers
		if (application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source)) {
			def c = application.getControllerClass(event.source?.name)
			controllerGlue(event.ctx, c)
		} else if (application.isArtefactOfType(DomainClassArtefactHandler.TYPE, event.source)) {
			def d = application.getDomainClass(event.source?.name)
			domainClassGlue(event.ctx, d)
		}
	}
	
	/**
	 * Implement code that is executed when the project configuration changes.
	 * The event is the same as for 'onChange'.
	 */
	def onConfigChange = { event ->
		println "${this.class.name}: onConfigChange"
	}
	
	/**
	 * 
	 */
	def onShutdown = { event ->
		println "${this.class.name}: shutdown"
	}
	
}
