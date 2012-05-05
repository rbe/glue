<% import com.bensmann.glue.* %>
<%=packageName%>
<%
	lowerCaseName = className[0].toLowerCase() + className.substring(1)
	def dialogId = "${lowerCaseName}_dialog"
	def dialogClass = "${lowerCaseName}_dialog"
	def panelId = "${lowerCaseName}_panel"
	def panelClass = "${lowerCaseName}_panel"
	def topMenuId = "${lowerCaseName}_topmenu"
	def bottomMenuId = "${lowerCaseName}_bottommenu"
%>
<g:if test="\${params.update?.success}">
	<g:set var="updateDiv" value="\${params.update?.success}" />
</g:if>
<g:else>
	<g:set var="updateDiv" value="crud_panel_left" />
</g:else>
		<div id="content-header">
			<h1>Edit ${className}</h1>
		</div>
		<g:if test="\${flash.message}">
			<div class="message">\${flash.message}</div>
		</g:if>
		<div id="${dialogId}" class="entity_dialog ${dialogClass}">
			<gx:renderDomain domain="\${params.domain}" domainId="\${params.domainId}" mode="edit">
				<div id="${topMenuId}" class="entity_topmenu">
					<ul>
						<li>
							<span class="crud_button">
								<gx:commitButton nextAction="\${params.nextAction}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<g:if test="\${updateDiv == 'content'}">
						<li>
							<span class="crud_button">
								<gx:listLink domain="${domainClass.fullName}" class="crud_button_list" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						</g:if>
					</ul>
				</div>
				<div class="clear"></div>
			<%
				// Define
				def rownum
				def rownumType
				def colnum
				def colnumType
				def divSuffix
				def labelDivId
				def renderPropertyDivId
				// Every property
				GlueScaffolder.getProperties(domainClass: domainClass).eachWithIndex { p, propnum ->
					rownum = propnum + 1
					rownumType = rownum % 2 == 0 ? "even" : "odd"
					colnum = 1
					colnumType = colnum % 2 == 0 ? "even" : "odd"
					divSuffix = "row${rownum}_col${colnum}"
					labelDivId = "${lowerCaseName}_label_${divSuffix}"
			%>
				<div class="line"></div>
				<gx:renderRowType property="${p.name}">
					<div id="${lowerCaseName}_crud_row${rownum}" class="crud_row_${rownumType} ${lowerCaseName}_crud_row_${rownumType}">
						<div id="${labelDivId}" class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
							<label for="${p.name}">${p.naturalName}</label>
						</div>
				<%
						colnum++
						colnumType = colnum % 2 == 0 ? "even" : "odd"
						divSuffix = "row${rownum}_col${colnum}"
						renderPropertyDivId = "${lowerCaseName}_crud_${divSuffix}"
				%>
						<div id="${renderPropertyDivId}" class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
							<gx:renderProperty property="${p.name}" update="[success: '${renderPropertyDivId}', failure: '${renderPropertyDivId}']" />
						</div>
					</div>
					<g:hasErrors bean="\${g_inst}" field="${p.name}">
						<div class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
							Error:
						</div>
						<g:eachError bean="\${g_inst}" field="${p.name}">
							<div class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
								<p>
									<g:message error="\${it}"/>
								</p>
							</div>
						</g:eachError>
					</g:hasErrors>
				</gx:renderRowType>
			<%
				}
			%>
				<div class="line"></div>
				<div id="${bottomMenuId}" class="entity_bottommenu">
					<ul>
						<li>
							<span class="crud_button">
								<gx:commitButton nextAction="\${params.nextAction}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<g:if test="\${updateDiv == 'content'}">
						<li>
							<span class="crud_button">
								<gx:listLink domain="${domainClass.fullName}" class="crud_button_list" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						</g:if>
					</ul>
				</div>
			</gx:renderDomain>
		</div>
