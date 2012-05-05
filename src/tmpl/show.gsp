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
			<h1>Show ${className}</h1>
		</div>
		<div id="${dialogId}" class="entity_dialog ${dialogClass}">
			<div class="clear"></div>
			<gx:renderDomain domain="\${params.domain}" domainId="\${params.domainId}" mode="show">
				<div id="${topMenuId}" class="entity_topmenu">
					<ul>
						<li>
							<span class="crud_button">
								<gx:createLink nextAction="[controller: 'glue', action: 'show']" domain="${domainClass.fullName}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<li>
							<span class="crud_button">
								<gx:editLink domain="${domainClass.fullName}" class="crud_button_edit" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<g:if test="\${updateDiv == 'crud_panel_left'}">
						<li>
							<span class="crud_button">
								<gx:listLink domain="${domainClass.fullName}" class="crud_button_list" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						</g:if>
					</ul>
				</div>
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
					<div id="${labelDivId}" class="crud_row_${rownumType} ${lowerCaseName}_crud_row_${rownumType}">
						<div id="${lowerCaseName}_crud_col${colnum}" class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
							<label for="${p.name}" class="entity_crud_label ${lowerCaseName}_crud_label">${p.naturalName}:</label>
						</div>
				<%
						colnum++
						colnumType = colnum % 2 == 0 ? "even" : "odd"
						divSuffix = "row${rownum}_col${colnum}"
						renderPropertyDivId = "${lowerCaseName}_crud_${divSuffix}"
				%>
						<div id="${renderPropertyDivId}" class="crud_column_${colnumType} ${lowerCaseName}_crud_column_${colnumType}">
							<gx:renderProperty property="${p.name}" />
						</div>
					</div>
				</gx:renderRowType>
			<%
				}
			%>
				<div class="line"></div>
				<div id="${bottomMenuId}" class="entity_bottommenu">
					<ul>
						<li>
							<span class="crud_button">
								<gx:createLink nextAction="[controller: 'glue', action: 'show']" domain="${domainClass.fullName}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<li>
							<span class="crud_button">
								<gx:editLink domain="${domainClass.fullName}" class="crud_button_edit" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
							</span>
						</li>
						<g:if test="\${updateDiv == 'crud_panel_left'}">
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
