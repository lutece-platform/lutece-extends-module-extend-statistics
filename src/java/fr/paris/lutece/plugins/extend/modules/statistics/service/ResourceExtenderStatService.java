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
package fr.paris.lutece.plugins.extend.modules.statistics.service;

import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistoryFilter;
import fr.paris.lutece.plugins.extend.modules.statistics.business.IResourceExtenderStatDAO;
import fr.paris.lutece.plugins.extend.modules.statistics.business.ResourceExtenderStat;
import fr.paris.lutece.plugins.extend.service.ExtendPlugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


/**
 *
 * ResourceExtenderStatService
 *
 */
@ApplicationScoped
public class ResourceExtenderStatService implements IResourceExtenderStatService
{
    // public static final String BEAN_SERVICE = "extend-statistics.resourceExtenderStatService";
    @Inject
    private IResourceExtenderStatDAO _statDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceExtenderStat> findStats( ResourceExtenderHistoryFilter filter )
    {
        return _statDAO.loadStats( filter, ExtendPlugin.getPlugin(  ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbStats( ResourceExtenderHistoryFilter filter )
    {
        ResourceExtenderHistoryFilter filterCount = copyCriteria( filter );
        filterCount.setGroupByAttributeName( filter.getGroupByAttributeName(  ) );

        return _statDAO.loadCountStats( filterCount, ExtendPlugin.getPlugin(  ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalNumbers( ResourceExtenderHistoryFilter filter )
    {
        return _statDAO.loadTotalNumbers( copyCriteria( filter ), ExtendPlugin.getPlugin(  ) );
    }

    /**
     * Copies the search criteria of a filter into a new one, without grouping, sorting nor paging.
     *
     * @param filter the filter
     * @return the new filter
     */
    private static ResourceExtenderHistoryFilter copyCriteria( ResourceExtenderHistoryFilter filter )
    {
        ResourceExtenderHistoryFilter filterCriteria = new ResourceExtenderHistoryFilter(  );
        filterCriteria.setIdHistory( filter.getIdHistory(  ) );
        filterCriteria.setExtenderType( filter.getExtenderType(  ) );
        filterCriteria.setIdExtendableResource( filter.getIdExtendableResource(  ) );
        filterCriteria.setExtendableResourceType( filter.getExtendableResourceType(  ) );
        filterCriteria.setUserGuid( filter.getUserGuid(  ) );
        filterCriteria.setIpAddress( filter.getIpAddress(  ) );
        filterCriteria.setDateCreation( filter.getDateCreation(  ) );
        filterCriteria.setOperatorDateCreation( filter.getOperatorDateCreation(  ) );
        filterCriteria.setDateCreationBegin( filter.getDateCreationBegin(  ) );
        filterCriteria.setDateCreationEnd( filter.getDateCreationEnd(  ) );
        filterCriteria.setIsWideSearch( filter.isWideSearch(  ) );

        return filterCriteria;
    }
}
