<%@page import="fr.paris.lutece.portal.web.pluginaction.IPluginActionResult"%>

<jsp:useBean id="extendStatistics" scope="session" class="fr.paris.lutece.plugins.extend.modules.statistics.web.StatisticsJspBean" />

<% 
	extendStatistics.init( request, extendStatistics.RIGHT_STATS );
	IPluginActionResult result = extendStatistics.getViewStats( request, response );
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
