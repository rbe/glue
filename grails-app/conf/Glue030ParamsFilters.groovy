import org.springframework.web.multipart.MultipartHttpServletRequest

/**
 * 
 */
class Glue030ParamsFilters {
	
	/**
	 * 
	 */
	def glueFileService
	
	/**
	 * Map curly-brace syntax
	 */
	def private mapCurlyBraceSyntax(params, paramName) {
		def param = params[paramName]
		if (!param) {
			return
		}
		// Is there a { and a }?
		if (param.indexOf("{") > -1 && param.indexOf("}") > -1) {
			def map = [:]
			// Each map key/value is separated by a comma
			// TODO What happens when a key or value contains a comma???
			param[1 .. param.length() - 2].split(",").eachWithIndex { p, i ->
				def s = p.split("=")
				def b = 0
				if (i > 0) {
					b = 1
				}
				map."${s[0][b .. s[0].length() - 1]}" = s[1]
			}
			map
		}
	}
	
	/**
	 * Parse a date value and return a Date object.
	 */
	def private _parseDate(val) {
		def format
		switch (val.length()) {
			case 10:// DD.MM.YYYY
				format = "dd.MM.yyyy"
				break
			case 13:// DD.MM.YYYY HH
				format = "dd.MM.yyyy HH"
				break
			case 16:// DD.MM.YYYY HH:MI
				format = "dd.MM.yyyy HH:mm"
				break
			case 19:// DD.MM.YYYY HH:MI:SS
				format = "dd.MM.yyyy HH:mm:ss"
				break
		}
		if (format) {
			Date.parse(format, val)
		}
	}
	
	/**
	 * 
	 */
	def filters = {
		// Check 'gxquery' parameter for map syntax
		gxQueryParamsFilter(controller: "glue", action: "list") {
			before = {
				if (log.traceEnabled) log.trace "GlueGxParamsFilters/gxQueryParamsFilter: params=${params.inspect()}"
				if (params.gxquery) {
					params.gxquery = mapCurlyBraceSyntax(params, "gxquery")
				}
			}
		}
		// Check 'gxdefaults' parameter for map syntax
		gxDefaultsParamsFilter(controller: "glue", action: "create") {
			before = {
				if (log.traceEnabled) log.trace "GlueGxParamsFilters/gxDefaultsParamsFilter: params=${params.inspect()}"
				if (params.gxdefaults) {
					params.gxdefaults = mapCurlyBraceSyntax(params, "gxdefaults")
				}
			}
		}
		// File upload: check for 'gxupload' parameters
		gxUploadParamsFilter(controller: "glue", action: "update") {
			before = {
				if (log.traceEnabled) log.trace "gxUploadParamsFilter: request=${request}"
				if (request instanceof MultipartHttpServletRequest) {
					def uploads = [:]
					request.getFileMap().each { k, v ->
						params.remove(k)
						uploads[k.replaceAll("gxupload_", "")] = v
					}
					if (log.traceEnabled) log.trace "gxUploadParamsFilter: uploads=${uploads}"
					glueFileService.upload(params: uploads)
				}
			}
		}
		// Check Glue forms
		gxFormsFilter(controller: "glue", action: "update") {
			before = {
				// Look for gxform(s)
				def gxforms = [:]
				params.grep {
				    it.key ==~ /gxform.*/
				}.each {
				    def (formName, formParameterName) = it.key.split("_")
				    if (!gxforms.containsKey(formName)) {
						gxforms[formName] = [:]
					}
				    gxforms[formName][formParameterName] = it.value
				    params.remove(it.key)
				}
				params.gxforms = gxforms
				if (log.traceEnabled) log.trace "Glue040GxParamsFilters/gxFormsFilter: params=${params.inspect()}"
			}
		}
		// Check date fields
		gxDateFilter(controller: "glue", action: "update") {
			before = {
				// Get Glue's domain class service
				def glueDomainClassService = applicationContext.getBean("glueDomainClassService")
				// Go through each submitted form
				params.gxforms?.each { formName, formParameters ->
					def dateProps = glueDomainClassService.getPropertyByType(domain: formParameters.domain, type: java.util.Date)
					if (log.traceEnabled) log.trace "GlueDataFilters/dateFilter: domain=${formParameters.domain.inspect()}, dateProps=${dateProps.inspect()}"
					// java.util.Date
					dateProps?.each { d ->
						def val = formParameters[d.name]
						if (log.traceEnabled) log.trace "GlueDataFilters/dateFilter: gxform.${d.name}/${d.type}: got ${val}"
						if (val) {
							formParameters[d.name] = _parseDate(val)
							if (log.traceEnabled) log.trace "GlueDataFilters/dateFilter: gxform.${d.name}/${d.type}: now is ${val}"
						}
					}
				}
			}
		}
		// Check timestamp fields
		gxTimestampFilter(controller: "glue", action: "update") {
			before = {
				// Get Glue's domain class service
				def glueDomainClassService = applicationContext.getBean("glueDomainClassService")
				// Go through each submitted form
				params.gxforms?.each { formName, formParameters ->
					def timestampProps = glueDomainClassService.getPropertyByType(domain: formParameters.domain, type: java.sql.Timestamp)
					if (log.traceEnabled) log.trace "GlueDataFilters/timestampFilter: domain=${formParameters.domain.inspect()}, timestampProps=${timestampProps.inspect()}"
					// java.sql.Timestamp
					timestampProps?.each { d ->
						def val = formParameters[d.name]
						if (log.traceEnabled) log.trace "GlueDataFilters/timestampFilter: gxform.${d.name}/${d.type}: got ${val}"
						if (val) {
							// TODO val = Date.parse("dd.MM.yyyy", val).toTimestamp()
							// TODO groovy.lang.MissingMethodException: No signature of method: java.util.Date.toTimestamp()
							formParameters[d.name] = new java.sql.Timestamp(_parseDate(val).time)
							if (log.traceEnabled) log.trace "GlueDataFilters/timestampFilter: gxform.${d.name}/${d.type}: now is ${val}"
						}
					}
				}
			}
		}
	}
	
}
