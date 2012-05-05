	/**
	 * 
	 */
	private def renderProperty(domainClass, property) {
		else if (property.embedded) {
			buf << renderEmbedded(domainClass, property)
		}
		else if (property.manyToOne || property.oneToOne) {
			buf << renderManyToOne(domainClass, property)
		}
		else if ((property.oneToMany && !property.bidirectional) || (property.manyToMany && property.isOwningSide())) {
			buf << renderManyToMany(domainClass, property)
		}
		else if (property.oneToMany) {
			buf << renderOneToMany(domainClass, property)
		}
		buf.toString()
	}
	
	/**
	 * TODO
	 */
	private def renderEmbedded(domainClass, property) {
		// Set global domain class
		GLOBAL_DC = domainClass
		def pdgdc = property.referencedDomainClass
		def buf = new StringBuffer()
		//out << "<!-- renderEmbedded for property=${property} -->\n"
		buf << "<table>\n"
		def alreadyPrefixed = false
		pdgdc.properties.findAll {
			!(it.name in ["id", "dateCreated", "lastUpdated", "version", "dirty"])
		}.each {
			//out << "<!-- renderEmbedded: it=${it} pdgdc=${pdgdc} -->\n"
			buf << "<tr>\n"
			if (!alreadyPrefixed) {
				domainInstance = "${pn(property)}"
				alreadyPrefixed = true
			}
			buf << "<td>\n"
			buf << "${it.name}:"
			buf << "</td>\n"
			buf << "<td>\n"
			buf << render(domainClass, it)
			buf << "</td>\n"
			buf << "</tr>\n"
		}
		buf << "</table>\n"
		// Reset global domain class
		GLOBAL_DC = null
		// Return
		buf.toString()
	}
	
	/**
	 * 
	 */
	private def renderOneToMany(domainClass, property) {
		def sw = new StringWriter()
		def pw = new PrintWriter(sw)
		pw.println """<!-- renderOneToMany for ${pn(property)} oto=${property.oneToOne} otm=${property.oneToMany} mtm=${property.manyToMany} bi=${property.bidirectional} --><ul>
			<g:each var="${property.name[0]}" in="\${${pn(property)}?}">
				<li>
					<g:link controller="${property.referencedDomainClass.propertyName}" action="show" id="\${${property.name[0]}.id}">\${${property.name[0]}?.encodeAsHTML()}</g:link>
				</li>
			</g:each>
		</ul>
		<g:link controller="${property.referencedDomainClass.propertyName}" params="['${domainClass.propertyName}.id': ${domainInstance}?.id]" action="create">Add ${property.referencedDomainClass.shortName}</g:link>"""
		/*
		pw.println "<ul>"
		pw.println "\t<g:each var=\"${property.name[0]}\" in=\"\${${pn(property)}?}\">"
		pw.println "\t\t<li><g:link controller=\"${property.referencedDomainClass.propertyName}\" action=\"show\" id=\"\${${property.name[0]}.id}\">\${${property.name[0]}?.encodeAsHTML()}</g:link></li>"
		pw.println "\t</g:each>"
		pw.println "</ul>"
		pw.println "<g:link controller=\"${property.referencedDomainClass.propertyName}\" params=\"['${domainClass.propertyName}.id':${domainInstance}?.id]\" action=\"create\">Add ${property.referencedDomainClass.shortName}</g:link>"
		*/
		return sw.toString()
	}
	
	/**
	 * 
	 */
	private def renderManyToOne(domainClass, property) {
		def controller = domainClass.name.toLowerCase()
		/*if ('autocomplete' == cp.glue) {
			return renderAutoComplete(domainClass, property)
		} else {*/
			if (property.association) {
				return """<!-- renderManyToOne for ${pn(property)} oto=${property.oneToOne} otm=${property.oneToMany} mtm=${property.manyToMany} bi=${property.bidirectional} --><g:select optionKey="id" optionValue="id" from="\${${property.type.name}.list()}" name="${pn(property)}.id" value="\${${pn(property)}?.id}" ${renderNoSelection(property)} />"""
			}
		/*}*/
	}
	
	/**
	 * 
	 */
	private def renderManyToMany(domainClass, property) {
		def sw = new StringWriter()
		def pw = new PrintWriter(sw)
		def controller = domainClass.name.toLowerCase()
		/*if ('autocomplete' == cp.glue) {
			return renderAutoComplete(domainClass, property)
		} else {*/
			pw.println """<!-- renderManyToMany for ${pn(property)} oto=${property.oneToOne} otm=${property.oneToMany} mtm=${property.manyToMany} bi=${property.bidirectional} --><g:select name="${pn(property)}" from="\${${property.referencedDomainClass.fullName}.list()}" size="5" multiple="yes" optionKey="id" value="\${${pn(property)}}" />"""
		/*
		pw.println "<g:select name=\"${pn(property)}\""
		pw.println "from=\"\${${property.referencedDomainClass.fullName}.list()}\""
		pw.println "size=\"5\" multiple=\"yes\" optionKey=\"id\""
		pw.println "value=\"\${${pn(property)}}\" />"
		*/
		/*}*/
		return sw.toString()
	}
	