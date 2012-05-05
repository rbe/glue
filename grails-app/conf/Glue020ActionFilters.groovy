/**
 * 
 */
class Glue020ActionFilters {
	
	/**
	 * Parse URL parameters.
	 * TODO Maybe there's a Grails utility class for this?
	 */
	private def _parseParams(params) {
		if (params) {
			def p = params.split("\\&")
			def z = [:]
			p.collect {
			    def x = it.split("=")
			    z[x[0]] = x[1]
			}
			z
		}
	}
	
	/**
	 * 
	 */
	def filters = {
		// Check update_success and update_failure parameters
		// TODO Rename to gxnextaction
		nextActionFilter(controller: "glue", action: "*") {
			before = {
				if (log.traceEnabled) log.trace "GlueActionFilters/nextActionFilter: params=${params.inspect()}"
				// nextAction map
				if (params.nextAction_controller || params.nextAction_action) {
					params.nextAction = [
						controller: params.remove("nextAction_controller") ?: "glue",
						action: params.remove("nextAction_action") ?: "",
						params: _parseParams(params.remove("nextAction_params"))
					]
					if (log.traceEnabled) log.trace "GlueActionFilters/nextActionFilter: modified params: params.nextAction=${params.nextAction}"
				}
				// update map for remoteLink/remoteFunction tags
				if (params.update_success || params.update_failure) {
					def u = params.remove("update_success")
					def f = params.remove("update_failure")
					params.update = [
						success: u ?: "",
						failure: f ?: u
					]
					if (log.traceEnabled) log.trace "GlueActionFilters/nextActionFilter: modified params.update=${params.update}"
				}
			}
		}
	}
	
}
