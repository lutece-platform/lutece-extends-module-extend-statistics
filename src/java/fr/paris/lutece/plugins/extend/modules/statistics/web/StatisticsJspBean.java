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
import fr.paris.lutece.plugins.extend.business.type.ExtendableResourceType;
import fr.paris.lutece.plugins.extend.modules.statistics.business.ResourceExtenderStat;
import fr.paris.lutece.plugins.extend.modules.statistics.service.IResourceExtenderStatService;
import fr.paris.lutece.plugins.extend.service.extender.IResourceExtenderService;
import fr.paris.lutece.plugins.extend.service.type.IExtendableResourceTypeService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.util.LocalizedDelegatePaginator;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.IPaginator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 *
 * StatisticsJspBean
 *
 */
@Controller(
    controllerJsp = "ViewStats.jsp",
    controllerPath = "jsp/admin/plugins/extend/modules/statistics/",
    right = StatisticsJspBean.RIGHT_STATS,
    securityTokenEnabled = true
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

    // PROPERTIES
    private static final String PROPERTY_VIEW_STATS_PAGE_TITLE = "module.extend.statistics.view_stats.pageTitle";
    private static final String PROPERTY_LABEL_ALL = "extend.labelAll";

    // MARKS
    private static final String MARK_LIST_RESOURCE_TYPES = "listResourceTypes";
    private static final String MARK_LIST_EXTENDER_TYPES = "listExtenderTypes";
    private static final String MARK_FILTER = "filter";
    private static final String MARK_LIST_STATS = "listStats";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_TOTAL_NUMBERS = "totalNumbers";
    private static final String MARK_DATE_CREATION_BEGIN = "dateCreationBegin";
    private static final String MARK_DATE_CREATION_END = "dateCreationEnd";
    private static final String MARK_RESOURCE_TYPE_LABELS = "resourceTypeLabels";

    private static final int ISO_DATE_LENGTH = 10;

    // GROUP BY
    private static final String GROUP_BY_ATTRIBUTE = " extender_type, id_resource, resource_type ";
    private static final List<String> SORTABLE_ATTRIBUTES = List.of( "extender_type", "id_resource", "resource_type", "nb_histories" );

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
    @Inject
    @ConfigProperty( name = "module.extend.statistics.listStats.itemsPerPage", defaultValue = "50" )
    private int _nDefaultItemsPerPage;
    private String _strCurrentPageIndex;
    private ResourceExtenderHistoryFilter _filter;

    /**
     * Gets the view stats page.
     *
     * @param model the model
     * @param request the request
     * @return the view stats page
     */
    @View( value = VIEW_STATS, defaultView = true, securityTokenAction = ACTION_SEARCH )
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
        _filter.setDateCreationBegin( toFilterDate( _filter.getDateCreationBegin(  ) ) );
        _filter.setDateCreationEnd( toFilterDate( _filter.getDateCreationEnd(  ) ) );

        return buildStatsPage( model, request );
    }

    /**
     * Converts a date posted by the date picker (yyyy-MM-dd, optionally followed by a time) into the short date format
     * the history filter parses.
     *
     * @param strDate the posted date
     * @return the date in the short format of the default locale, or an empty string when it is not a date
     */
    private static String toFilterDate( String strDate )
    {
        if ( StringUtils.length( strDate ) < ISO_DATE_LENGTH )
        {
            return StringUtils.EMPTY;
        }

        try
        {
            LocalDate date = LocalDate.parse( strDate.substring( 0, ISO_DATE_LENGTH ) );

            return DateUtil.getDateString( java.sql.Date.valueOf( date ), I18nService.getDefaultLocale(  ) );
        }
        catch ( DateTimeParseException e )
        {
            return StringUtils.EMPTY;
        }
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
        Locale locale = getLocale(  );

        // RESOURCE TYPES
        ReferenceList listResourceTypes = _resourceTypeService.findAllAsRef( locale );
        listResourceTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, locale ) );

        // EXTENDER TYPES
        ReferenceList listExtenderTypes = _resourceExtenderService.getExtenderTypes( locale );
        listExtenderTypes.addItem( StringUtils.EMPTY,
            I18nService.getLocalizedString( PROPERTY_LABEL_ALL, locale ) );

        // FILTER
        _filter.setGroupByAttributeName( GROUP_BY_ATTRIBUTE );
        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        _filter.setSortedAttributeName( ( strSortedAttributeName != null && SORTABLE_ATTRIBUTES.contains( strSortedAttributeName ) )
            ? strSortedAttributeName : StringUtils.EMPTY );
        _filter.setAscSort( request );

        // PAGINATOR
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        if ( _nItemsPerPage <= 0 )
        {
            _nItemsPerPage = _nDefaultItemsPerPage;
        }

        int nNbStats = _statService.getNbStats( _filter );

        int nCurrentPageIndex = NumberUtils.toInt( _strCurrentPageIndex, 1 );

        if ( ( nCurrentPageIndex < 1 ) || ( ( ( nCurrentPageIndex - 1 ) * _nItemsPerPage ) >= nNbStats ) )
        {
            nCurrentPageIndex = 1;
        }

        _strCurrentPageIndex = Integer.toString( nCurrentPageIndex );
        _filter.setItemsPerPage( _nItemsPerPage );
        _filter.setPageIndex( nCurrentPageIndex );

        String strURL = getHomeUrl( request );

        if ( _filter.containsAttributeName(  ) )
        {
            strURL += ( "?" + Parameters.SORTED_ATTRIBUTE_NAME + "=" + _filter.getSortedAttributeName(  ) );
            strURL += ( "&" + Parameters.SORTED_ASC + "=" + _filter.isAscSort(  ) );
        }

        IPaginator<ResourceExtenderStat> paginator = new LocalizedDelegatePaginator<ResourceExtenderStat>( _statService.findStats(
                    _filter ), _nItemsPerPage, strURL, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex,
                nNbStats, locale );

        model.put( MARK_LIST_RESOURCE_TYPES, listResourceTypes );
        model.put( MARK_LIST_EXTENDER_TYPES, listExtenderTypes );
        model.put( MARK_FILTER, _filter );
        model.put( MARK_LIST_STATS, paginator.getPageItems(  ) );
        model.put( MARK_RESOURCE_TYPE_LABELS, getResourceTypeLabels( paginator.getPageItems(  ), locale ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( paginator.getItemsPerPage(  ) ) );
        model.put( MARK_TOTAL_NUMBERS, _statService.getTotalNumbers( _filter ) );
        model.put( MARK_DATE_CREATION_BEGIN, DateUtil.formatDateSql( _filter.getDateCreationBegin(  ), I18nService.getDefaultLocale(  ) ) );
        model.put( MARK_DATE_CREATION_END, DateUtil.formatDateSql( _filter.getDateCreationEnd(  ), I18nService.getDefaultLocale(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_STATS, locale, model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Returns the label of each resource type of the given statistics, as the service of that type describes it. A
     * type no service describes has no entry.
     *
     * @param listStats the statistics
     * @param locale the locale of the labels
     * @return the labels by resource type
     */
    private Map<String, String> getResourceTypeLabels( List<ResourceExtenderStat> listStats, Locale locale )
    {
        Map<String, String> resourceTypeLabels = new HashMap<>(  );

        for ( ResourceExtenderStat stat : listStats )
        {
            String strResourceType = stat.getExtendableResourceType(  );

            if ( StringUtils.isNotEmpty( strResourceType ) && !resourceTypeLabels.containsKey( strResourceType ) )
            {
                ExtendableResourceType resourceType = _resourceTypeService.findByPrimaryKey( strResourceType, locale );

                if ( ( resourceType != null ) && StringUtils.isNotBlank( resourceType.getDescription(  ) ) )
                {
                    resourceTypeLabels.put( strResourceType, resourceType.getDescription(  ) );
                }
            }
        }

        return resourceTypeLabels;
    }
}
