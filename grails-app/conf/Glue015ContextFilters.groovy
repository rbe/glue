import com.bensmann.glue.context.*

/**
 * 
 */
class Glue015ContextFilters {
	
	/**
	 * 
	 */
	def filters = {
		// Remove all domains from context at login
		contextAuthFilter(controller: "glueSecurity", action: "auth") {
			after = {
				def glueContextService = applicationContext.getBean("glueContextService")
				if (glueContextService) {
					if (log.traceEnabled) log.trace "GlueContextFilters/contextAuthFilter: params=${params.inspect()}"
					glueContextService.removeDomains()
				} else {
					log.warn "contextAuthFilter: could not get reference to GlueContextService"
				}
			}
		}
		// Add domain to context when editing
		contextEditFilter(controller: "glue", action: "edit") {
			before = {
				def glueContextService = applicationContext.getBean("glueContextService")
				if (glueContextService && params.domain && params.domainId) {
					if (log.traceEnabled) log.trace "GlueContextFilters/contextEditFilter: params=${params.inspect()}"
					glueContextService.addDomain(domain: params.domain, domainId: params.domainId)
				} else {
					log.warn "contextEditFilter: could not get reference to GlueContextService"
				}
			}
		}
		// Remove domain from context when data is committed
		contextUpdateFilter(controller: "glue", action: "update") {
			before = {
				def glueContextService = applicationContext.getBean("glueContextService")
				if (glueContextService && params.domain && params.domainId) {
					if (log.traceEnabled) log.trace "GlueContextFilters/contextUpdateFilter: params=${params.inspect()}"
					glueContextService.removeDomain(domain: params.domain, domainId: params.domainId)
				} else {
					log.warn "contextUpdateFilter: could not get reference to GlueContextService"
				}
			}
		}
	}
	
}
