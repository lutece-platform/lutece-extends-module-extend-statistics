<%@page import="fr.paris.lutece.portal.web.pluginaction.IPluginActionResult"%>

<jsp:useBean id="socialhubStatistics" scope="session" class="fr.paris.lutece.plugins.socialhub.modules.statistics.web.StatisticsJspBean" />

<% 
	socialhubStatistics.init( request, socialhubStatistics.RIGHT_STATS );
	IPluginActionResult result = socialhubStatistics.getViewStats( request, response );
	if ( result.getRedirect(  ) != null )
	{
		response.sendRedirect( result.getRedirect(  ) );
	}
	else if ( result.getHtmlContent(  ) != null )
	{
%>
		<%@ page errorPage="../../../../ErrorPage.jsp" %>
		<jsp:include page="../../../../AdminHeader.jsp" />

		<%= result.getHtmlContent(  ) %>

		<%@ include file="../../../../AdminFooter.jsp" %>
<%
	}
%>
