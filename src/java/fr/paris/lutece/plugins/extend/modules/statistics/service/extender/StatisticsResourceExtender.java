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
package fr.paris.lutece.plugins.extend.modules.statistics.service.extender;

import fr.paris.lutece.plugins.extend.business.extender.ResourceExtenderDTO;
import fr.paris.lutece.plugins.extend.modules.statistics.web.component.StatisticsResourceExtenderComponent;
import fr.paris.lutece.plugins.extend.service.extender.AbstractResourceExtender;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * StatisticsResourceExtender
 */
@ApplicationScoped
@Named( "extend-statistics.statisticsResourceExtender" )
public class StatisticsResourceExtender extends AbstractResourceExtender
{
    public static final String RESOURCE_EXTENDER = "statistics";

    @Inject
    @ConfigProperty( name = "extend.statistics.titleKey", defaultValue = "module.extend.statistics.extender.statistics.label" )
    private String titleKey;

    @Inject
    private StatisticsResourceExtenderComponent resourceExtenderComponent;

    StatisticsResourceExtender( )
    {

    }

    @PostConstruct
    public void init( )
    {
        setResourceExtenderComponent( resourceExtenderComponent );
        setKey( RESOURCE_EXTENDER );
        setI18nTitleKey( titleKey );
        setConfigRequired( false );
        setHistoryEnable( true );
        setStateEnable( false );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInvoked( String strExtenderType )
    {
        if ( StringUtils.isNotBlank( strExtenderType ) )
        {
            return getKey( ).equals( strExtenderType );
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContent( String strIdExtendableResource, String strExtendableResourceType, String strParameters,
        HttpServletRequest request )
    {
        return StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCreateResourceAddOn( ResourceExtenderDTO extender )
    {
        // No add-on to create
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doDeleteResourceAddOn( ResourceExtenderDTO extender )
    {
        // No add-on to delete
    }
}
