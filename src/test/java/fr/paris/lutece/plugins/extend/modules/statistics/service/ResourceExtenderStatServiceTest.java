/*
 * Copyright (c) 2002-2026, Mairie de Paris
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
package fr.paris.lutece.plugins.extend.modules.statistics.service;

import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistoryFilter;
import fr.paris.lutece.plugins.extend.modules.statistics.business.ResourceExtenderStat;
import fr.paris.lutece.plugins.extend.service.extender.history.IResourceExtenderHistoryService;
import fr.paris.lutece.test.LuteceTestCase;

import jakarta.inject.Inject;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ResourceExtenderStatServiceTest
 */
public class ResourceExtenderStatServiceTest extends LuteceTestCase
{
    private static final String EXTENDER_TYPE = "statistics";
    private static final String RESOURCE_TYPE = "EXTEND_STATISTICS_TEST";
    private static final String ID_RESOURCE_1 = "1";
    private static final String ID_RESOURCE_2 = "2";
    private static final String USER_GUID = "test";
    private static final String GROUP_BY_ATTRIBUTE = " extender_type, id_resource, resource_type ";

    @Inject
    private IResourceExtenderStatService _statService;
    @Inject
    private IResourceExtenderHistoryService _historyService;

    /**
     * Creates two histories on the first resource and one on the second.
     */
    @BeforeEach
    public void createHistories( )
    {
        _historyService.create( EXTENDER_TYPE, ID_RESOURCE_1, RESOURCE_TYPE, USER_GUID );
        _historyService.create( EXTENDER_TYPE, ID_RESOURCE_1, RESOURCE_TYPE, USER_GUID );
        _historyService.create( EXTENDER_TYPE, ID_RESOURCE_2, RESOURCE_TYPE, USER_GUID );
    }

    /**
     * Removes the histories of the test resources.
     */
    @AfterEach
    public void removeHistories( )
    {
        _historyService.removeByResource( EXTENDER_TYPE, ID_RESOURCE_1, RESOURCE_TYPE );
        _historyService.removeByResource( EXTENDER_TYPE, ID_RESOURCE_2, RESOURCE_TYPE );
    }

    /**
     * Builds a filter on the test resource type, grouped by resource.
     *
     * @return the filter
     */
    private ResourceExtenderHistoryFilter buildFilter( )
    {
        ResourceExtenderHistoryFilter filter = new ResourceExtenderHistoryFilter( );
        filter.setExtendableResourceType( RESOURCE_TYPE );
        filter.setGroupByAttributeName( GROUP_BY_ATTRIBUTE );

        return filter;
    }

    /**
     * Finds one stat per resource, counting its histories.
     */
    @Test
    public void testFindStats( )
    {
        List<ResourceExtenderStat> listStats = _statService.findStats( buildFilter( ) );

        assertEquals( 2, listStats.size( ) );

        for ( ResourceExtenderStat stat : listStats )
        {
            assertEquals( EXTENDER_TYPE, stat.getExtenderType( ) );
            assertEquals( RESOURCE_TYPE, stat.getExtendableResourceType( ) );
            assertEquals( ID_RESOURCE_1.equals( stat.getIdExtendableResource( ) ) ? 2 : 1, stat.getNumber( ) );
        }
    }

    /**
     * Counts the grouped rows, whatever the page of the filter.
     */
    @Test
    public void testGetNbStats( )
    {
        ResourceExtenderHistoryFilter filter = buildFilter( );
        filter.setItemsPerPage( 1 );
        filter.setPageIndex( 1 );

        assertEquals( 2, _statService.getNbStats( filter ) );
        assertEquals( 1, _statService.findStats( filter ).size( ) );
    }

    /**
     * Counts every history of the filter, whatever the grouping and the page, and leaves the filter unchanged.
     */
    @Test
    public void testGetTotalNumbers( )
    {
        ResourceExtenderHistoryFilter filter = buildFilter( );
        filter.setItemsPerPage( 1 );
        filter.setPageIndex( 1 );

        assertEquals( 3L, _statService.getTotalNumbers( filter ) );
        assertEquals( GROUP_BY_ATTRIBUTE, filter.getGroupByAttributeName( ) );
        assertEquals( 1, filter.getItemsPerPage( ) );
        assertEquals( 1, filter.getPageIndex( ) );
    }
}
