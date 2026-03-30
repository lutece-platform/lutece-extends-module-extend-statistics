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
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.type.IExtendableResourceTypeService;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.util.LocalizedDelegatePaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.IPaginator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


/**
 *
 * StatisticsJspBean
 *
 */
@Controller(
    controllerJsp = "ViewStats.jsp",
    controllerPath = "jsp/admin/plugins/extend/modules/statistics/",
    right = StatisticsJspBean.RIGHT_STATS
)
@SessionScoped
@Named
public class StatisticsJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    /** The Constant RIGHT_STATS. */
    public static final String RIGHT_STATS = "EXTEND_STATISTICS";

    // VIEWS
    private static final String VIEW_STATS = "viewStats";

    // ACTIONS
    private static final String ACTION_SEARCH = "search";
    private static final String ACTION_RESET = "reset";

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

    // GROUP BY
    private static final String GROUP_BY_ATTRIBUTE = " extender_type, id_resource, resource_type ";

    // TEMPLATES
    private static final String TEMPLATE_VIEW_STATS = "admin/plugins/extend/modules/statistics/view_stats.html";

    // SERVICES
    @Inject
    private IExtendableResourceTypeService _resourceTypeService;
    @Inject
    private IResourceExtenderService _resourceExtenderService;
    @Inject
    private IResourceExtenderStatService _statService;

    // VARIABLES
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_STATS_PER_PAGE, 50 );
    private String _strCurrentPageIndex;
    private ResourceExtenderHistoryFilter _filter;

    /**
     * Gets the view stats page.
     *
     * @param model the model
     * @param request the request
     * @return the view stats page
     */
    @View( value = VIEW_STATS, defaultView = true )
    public String getViewStats( Models model, HttpServletRequest request )
    {
        if ( _filter == null )
        {
            _filter = new ResourceExtenderHistoryFilter(  );
        }

        return buildStatsPage( model, request );
    }

    /**
     * Processes the search filter action.
     *
     * @param model the model
     * @param request the request
     * @return the view stats page with filtered results
     */
    @Action( ACTION_SEARCH )
    public String doSearch( Models model, HttpServletRequest request )
    {
        _filter = new ResourceExtenderHistoryFilter(  );
        populate( _filter, request );

        return buildStatsPage( model, request );
    }

    /**
     * Resets the filter.
     *
     * @param request the request
     * @return redirect to the default view
     */
    @Action( ACTION_RESET )
    public String doReset( HttpServletRequest request )
    {
        _filter = new ResourceExtenderHistoryFilter(  );

        return redirectView( request, VIEW_STATS );
    }

    /**
     * Builds the stats page with the current filter.
     *
     * @param model the model
     * @param request the request
     * @return the rendered page
     */
    private String buildStatsPage( Models model, HttpServletRequest request )
    {
        // RESOURCE TYPES
        ReferenceList listResourceTypes = _resourceTypeService.findAllAsRef( AdminUserService.getLocale( request ) );
        listResourceTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, request.getLocale(  ) ) );

        // EXTENDER TYPES
        ReferenceList listExtenderTypes = _resourceExtenderService.getExtenderTypes( request.getLocale(  ) );
        listExtenderTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, request.getLocale(  ) ) );

        // FILTER
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
            int nCurrentPageIndex = Integer.parseInt( _strCurrentPageIndex );

            if ( ( ( nCurrentPageIndex - 1 ) * _nItemsPerPage ) > nNbStats )
            {
                nCurrentPageIndex = 1;
            }

            _filter.setItemsPerPage( _nItemsPerPage );
            _filter.setPageIndex( nCurrentPageIndex );
        }

        String strURL = getHomeUrl( request );

        if ( _filter.containsAttributeName(  ) )
        {
            strURL += ( "?" + Parameters.SORTED_ATTRIBUTE_NAME + "=" + _filter.getSortedAttributeName(  ) );
            strURL += ( "&" + Parameters.SORTED_ASC + "=" + _filter.isAscSort(  ) );
        }

        IPaginator<ResourceExtenderStat> paginator = new LocalizedDelegatePaginator<ResourceExtenderStat>( _statService.findStats(
                    _filter ), _nItemsPerPage, strURL, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex,
                nNbStats, request.getLocale(  ) );

        model.put( MARK_LIST_RESOURCE_TYPES, listResourceTypes );
        model.put( MARK_LIST_EXTENDER_TYPES, listExtenderTypes );
        model.put( MARK_FILTER, _filter );
        model.put( MARK_LIST_STATS, paginator.getPageItems(  ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage(  ) ) );
        model.put( MARK_TOTAL_NUMBERS, _statService.getTotalNumbers( _filter ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_STATS, getLocale(  ), model.asMap(  ) );

        return getAdminPage( template.getHtml(  ) );
    }
}
