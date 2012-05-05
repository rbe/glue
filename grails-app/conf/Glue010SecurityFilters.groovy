import com.bensmann.glue.context.*

/**
 * 
 */
class Glue010SecurityFilters {
	
	/**
	 * 
	 */
	def filters = {
		// Watch for an unauthenticated access and redirect to security controller if necessary
		authenticationFilter(controller: "glue", action: "*") {
			before = {
				def user = session.glueContext?.user
				if (!user) {
					// Save original URI and params
					session.gxPreAuthUri = request.forwardURI
					session.gxPreAuthParams = params
					// Redirect to login
					if (log.traceEnabled) log.trace "GlueSecurityFilters/authenticationFilter: no user, redirecting to security controller: session=${session.inspect()} params=${params.inspect()}"
					redirect(controller: "glueSecurity", action: "login")
					return false
				}
			}
		}
	}
	
}
