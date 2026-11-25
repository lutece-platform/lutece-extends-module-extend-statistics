/*
 * Copyright (c) 2002-2026, City of Paris
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

import fr.paris.lutece.plugins.extend.service.extender.history.IResourceExtenderHistoryService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.security.ISecurityTokenService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.test.AdminUserUtils;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;

import jakarta.inject.Inject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * A new search opens on the first page of its results.
 */
public class StatisticsJspBeanTest extends LuteceTestCase
{
    private static final String EXTENDER_TYPE = "e2estats";
    private static final String RESOURCE_TYPE = "E2E_STATS_PAGINATION";
    private static final String PREFIX = "e2e13res";
    private static final int NB_RESOURCES = 120;

    @Inject
    private StatisticsJspBean _bean;
    @Inject
    private IResourceExtenderHistoryService _historyService;
    @Inject
    private ISecurityTokenService _securityTokenService;

    private MockHttpServletRequest request( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        AdminUserUtils.registerAdminUserWithRight( request, new AdminUser( ), StatisticsJspBean.RIGHT_STATS );

        return request;
    }

    @Test
    public void testNewSearchOpensOnTheFirstPage( ) throws Exception
    {
        for ( int i = 0; i < NB_RESOURCES; i++ )
        {
            _historyService.create( EXTENDER_TYPE, PREFIX + String.format( "%03d", i ), RESOURCE_TYPE, "e2e" );
        }

        try
        {
            MockHttpServletRequest view = request( );
            view.addParameter( "view", "viewStats" );
            view.addParameter( "items_per_page", "50" );
            view.addParameter( "page_index", "3" );
            _bean.processController( view, new MockHttpServletResponse( ) );

            MockHttpServletRequest search = request( );
            search.setMethod( "POST" );
            search.addParameter( "action", "search" );
            search.addParameter( "extenderType", EXTENDER_TYPE );
            search.addParameter( SecurityTokenService.PARAMETER_TOKEN, _securityTokenService.getToken( search, "search" ) );
            String strHtml = _bean.processController( search, new MockHttpServletResponse( ) );

            Set<String> setIds = new HashSet<>( );
            Matcher matcher = Pattern.compile( PREFIX + "\\d{3}" ).matcher( strHtml );

            while ( matcher.find( ) )
            {
                setIds.add( matcher.group( ) );
            }

            assertEquals( 50, setIds.size( ), "rows of the first page expected, got " + setIds.size( ) );
        }
        finally
        {
            for ( int i = 0; i < NB_RESOURCES; i++ )
            {
                _historyService.removeByResource( EXTENDER_TYPE, PREFIX + String.format( "%03d", i ), RESOURCE_TYPE );
            }
        }
    }
}
