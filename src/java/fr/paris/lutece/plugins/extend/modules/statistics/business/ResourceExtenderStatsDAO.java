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
package fr.paris.lutece.plugins.extend.modules.statistics.business;

import fr.paris.lutece.plugins.extend.business.extender.history.ResourceExtenderHistoryFilter;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * ResourceExtenderStatsDAO
 *
 */
public class ResourceExtenderStatsDAO implements IResourceExtenderStatDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT COUNT( id_history ) AS nb_histories, extender_type, id_resource, resource_type FROM extend_resource_extender_history ";
    private static final String SQL_QUERY_SELECT_COUNT = " SELECT COUNT( id_history ) AS nb_histories FROM extend_resource_extender_history ";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceExtenderStat> loadStats( ResourceExtenderHistoryFilter filter, Plugin plugin )
    {
        List<ResourceExtenderStat> listStats = new ArrayList<ResourceExtenderStat>(  );
        DAOUtil daoUtil = new DAOUtil( filter.buildSQLQuery( SQL_QUERY_SELECT ), plugin );
        filter.setFilterValues( daoUtil );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            int nIndex = 1;
            ResourceExtenderStat stat = new ResourceExtenderStat(  );
            stat.setNumber( daoUtil.getInt( nIndex++ ) );
            stat.setExtenderType( daoUtil.getString( nIndex++ ) );
            stat.setIdExtendableResource( daoUtil.getString( nIndex++ ) );
            stat.setExtendableResourceType( daoUtil.getString( nIndex ) );

            listStats.add( stat );
        }

        daoUtil.free(  );

        return listStats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int loadCountStats( ResourceExtenderHistoryFilter filter, Plugin plugin )
    {
        int nNbStats = 0;
        DAOUtil daoUtil = new DAOUtil( filter.buildSQLQuery( SQL_QUERY_SELECT ), plugin );
        filter.setFilterValues( daoUtil );
        daoUtil.executeQuery(  );

        ResultSet rs = daoUtil.getResultSet(  );

        if ( rs != null )
        {
            try
            {
                nNbStats = rs.last(  ) ? rs.getRow(  ) : 0;
            }
            catch ( SQLException e )
            {
                AppLogService.error( e.getMessage(  ), e );
            }
        }

        daoUtil.free(  );

        return nNbStats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long loadTotalNumbers( ResourceExtenderHistoryFilter filter, Plugin plugin )
    {
        long lNb = 0;

        DAOUtil daoUtil = new DAOUtil( filter.buildSQLQuery( SQL_QUERY_SELECT_COUNT ), plugin );
        filter.setFilterValues( daoUtil );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            lNb = daoUtil.getLong( 1 );
        }

        daoUtil.free(  );

        return lNb;
    }
}
