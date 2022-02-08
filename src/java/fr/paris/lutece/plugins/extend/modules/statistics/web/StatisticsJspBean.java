/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.extend.modules.statistics.web;

import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistoryFilter;
import fr.paris.lutece.plugins.extend.modules.statistics.business.ResourceExtenderStat;
import fr.paris.lutece.plugins.extend.modules.statistics.service.IResourceExtenderStatService;
import fr.paris.lutece.plugins.extend.modules.statistics.service.ResourceExtenderStatService;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.extender.ResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.type.ExtendableResourceTypeService;
import fr.paris.lutece.plugins.extend.service.type.IExtendableResourceTypeService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.pluginaction.DefaultPluginActionResult;
import fr.paris.lutece.portal.web.pluginaction.IPluginActionResult;
import fr.paris.lutece.portal.web.util.LocalizedDelegatePaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.IPaginator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * StatisticsJspBean
 *
 */
public class StatisticsJspBean extends PluginAdminPageJspBean
{
    /** The Constant RIGHT_STATS. */
    public static final String RIGHT_STATS = "EXTEND_STATISTICS";

    // PROPERTIES
    private static final String PROPERTY_VIEW_STATS_PAGE_TITLE = "module.extend.statistics.view_stats.pageTitle";
    private static final String PROPERTY_LABEL_ALL = "extend.labelAll";
    private static final String PROPERTY_DEFAULT_LIST_STATS_PER_PAGE = "module.extend.statistics.listStats.itemsPerPage";

    // MARKS
    private static final String MARK_LIST_RESOURCE_TYPES = "listResourceTypes";
    private static final String MARK_LIST_EXTENDER_TYPES = "listExtenderTypes";
    private static final String MARK_FILTER = "filter";
    private static final String MARK_LIST_STATS = "listStats";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_TOTAL_NUMBERS = "totalNumbers";

    // PARAMETERS
    private static final String PARAMETER_SESSION = "session";
    private static final String PARAMETER_RESET = "reset";

    // GROUP BY
    private static final String GROUP_BY_ATTRIBUTE = " extender_type, id_resource, resource_type ";

    // TEMPLATES
    private static final String TEMPLATE_VIEW_STATS = "admin/plugins/extend/modules/statistics/view_stats.html";

    // JSP
    private static final String JSP_URL_VIEW_STATS = "jsp/admin/plugins/extend/modules/statistics/ViewStats.jsp";

    // SERVICES
    private IExtendableResourceTypeService _resourceTypeService = SpringContextService.getBean( ExtendableResourceTypeService.BEAN_SERVICE );
    private IResourceExtenderService _resourceExtenderService = SpringContextService.getBean( ResourceExtenderService.BEAN_SERVICE );
    private IResourceExtenderStatService _statService = SpringContextService.getBean( ResourceExtenderStatService.BEAN_SERVICE );

    // VARIABLES
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_STATS_PER_PAGE, 50 );
    private String _strCurrentPageIndex;
    private ResourceExtenderHistoryFilter _filter;

    /**
     * Gets the view stats.
     *
     * @param request the request
     * @param response the response
     * @return the view stats
     * @throws AccessDeniedException the access denied exception
     */
    public IPluginActionResult getViewStats( HttpServletRequest request, HttpServletResponse response )
        throws AccessDeniedException
    {
        setPageTitleProperty( PROPERTY_VIEW_STATS_PAGE_TITLE );

        // RESOURCE TYPES
        ReferenceList listResourceTypes = _resourceTypeService.findAllAsRef( AdminUserService.getLocale( request ) );
        listResourceTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, request.getLocale(  ) ) );

        // EXTENDER TYPES
        ReferenceList listExtenderTypes = _resourceExtenderService.getExtenderTypes( request.getLocale(  ) );
        listExtenderTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, request.getLocale(  ) ) );

        // RESOURCE EXTENDER HISTORY FILTER
        initFilter( request );
        _filter.setGroupByAttributeName( GROUP_BY_ATTRIBUTE );
        _filter.setSortedAttributeName( request );
        _filter.setAscSort( request );

        // PAGINATOR
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        int nNbStats = _statService.getNbStats( _filter );

        if ( StringUtils.isNotBlank( _strCurrentPageIndex ) && StringUtils.isNumeric( _strCurrentPageIndex ) )
        {
            // Define the current page index
            // If the current page index is > ( nCurrentPageIndex - 1 ) * _nItemsPerPage ), then display the first page index
            int nCurrentPageIndex = Integer.parseInt( _strCurrentPageIndex );

            if ( ( ( nCurrentPageIndex - 1 ) * _nItemsPerPage ) > nNbStats )
            {
                nCurrentPageIndex = 1;
            }

            _filter.setItemsPerPage( _nItemsPerPage );
            _filter.setPageIndex( nCurrentPageIndex );
        }

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_URL_VIEW_STATS );
        url.addParameter( PARAMETER_SESSION, PARAMETER_SESSION );

        if ( _filter.containsAttributeName(  ) )
        {
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, _filter.getSortedAttributeName(  ) );
            url.addParameter( Parameters.SORTED_ASC, Boolean.toString( _filter.isAscSort(  ) ) );
        }

        IPaginator<ResourceExtenderStat> paginator = new LocalizedDelegatePaginator<ResourceExtenderStat>( _statService.findStats( 
                    _filter ), _nItemsPerPage, url.getUrl(  ), Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex,
                nNbStats, request.getLocale(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_LIST_RESOURCE_TYPES, listResourceTypes );
        model.put( MARK_LIST_EXTENDER_TYPES, listExtenderTypes );
        model.put( MARK_FILTER, _filter );
        model.put( MARK_LIST_STATS, paginator.getPageItems(  ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage(  ) ) );
        model.put( MARK_TOTAL_NUMBERS, _statService.getTotalNumbers( _filter ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_STATS, request.getLocale(  ), model );

        IPluginActionResult result = new DefaultPluginActionResult(  );

        result.setHtmlContent( getAdminPage( template.getHtml(  ) ) );

        return result;
    }

    /**
     * Inits the filter.
     *
     * @param request the request
     */
    private void initFilter( HttpServletRequest request )
    {
        if ( StringUtils.isNotBlank( request.getParameter( PARAMETER_RESET ) ) )
        {
            _filter = new ResourceExtenderHistoryFilter(  );
        }
        else if ( StringUtils.isBlank( request.getParameter( PARAMETER_SESSION ) ) || ( _filter == null ) )
        {
            _filter = new ResourceExtenderHistoryFilter(  );
            populate( _filter, request );
        }
    }
}
