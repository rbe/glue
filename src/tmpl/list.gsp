<% import com.bensmann.glue.* %>
<%=packageName%>
<%
	lowerCaseName = className[0].toLowerCase() + className.substring(1)
	def dialogId = "${lowerCaseName}_dialog"
	def dialogClass = "${lowerCaseName}_dialog"
	def topMenuId = "${lowerCaseName}_topmenu"
	def bottomMenuId = "${lowerCaseName}_bottommenu"
	def dataTableCols = GlueScaffolder.getGrailsUiDataTableCols(domainClass: domainClass)
%>
<g:if test="\${params.updateDiv}">
	<g:set var="updateDiv" value="\${params.updateDiv}" />
</g:if>
<g:else>
	<g:set var="updateDiv" value="crud_panel_left" />
</g:else>
		<div id="content-header">
			<h1>List ${className}</h1>
		</div>
		<g:if test="\${flash.message}">
			<div class="message">\${flash.message}</div>
		</g:if>
		<div id="${dialogId}" class="entity_dialog ${dialogClass}">
			<div id="${topMenuId}" class="entity_topmenu">
				<ul>
					<li>
						<span class="crud_button">
							<gx:createLink domain="${domainClass.fullName}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
						</span>
					</li>
				</ul>
			</div>
			<div class="clear"></div>
			<div class="yui-skin-sam">
				<gui:dataTable
					controller="glue"
					action="queryAsJSON"
					params="[domain: '${domainClass.fullName}']"
					columnDefs="${dataTableCols}"
					rowClickNavigation="true"
					draggableColumns="true"
					rowsPerPage="20"
					paginatorConfig="[
						template: '{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
						pageReportTemplate: '{totalRecords} total records'
					]" />
			</div>
		</div>
		<div id="${bottomMenuId}" class="entity_bottommenu">
			<ul>
				<li>
					<span class="crud_button">
						<gx:createLink domain="${domainClass.fullName}" class="crud_button_create" update='[success: "\${updateDiv}", failure: "\${updateDiv}"]'/>
					</span>
				</li>
			</ul>
		</div>
