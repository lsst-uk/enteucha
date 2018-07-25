/*
 *  Copyright (C) 2018 Royal Observatory, University of Edinburgh, UK
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.roe.wfau.enteucha.cqengine;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.AbstractTestCase;
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;
import uk.ac.roe.wfau.enteucha.cqengine.CQZoneImpl.IndexingShape;

/**
 * 
 * 
 */
@Slf4j
public class CQZoneImplTestCase
extends AbstractTestCase
    {

    /**
     * 
     */
    public CQZoneImplTestCase()
        {
        super();
        }

    /**
     * The {@link IndexingShape} for this {@link TestCase}.
     * 
     */
    private IndexingShape indexing ;

    /**
     * The zone count for this {@link TestCase}.
     * 
     */
    private double zonecount;
    
    @Override
    public Matcher matcher()
        {
        return new CQZoneImpl.ZoneSetImpl(
            this.indexing,
            this.zonecount
            );
        }
    
    /**
     * Test finding things.
     * 
     */
    public void testFind004()
        {
        for (IndexingShape indexing : IndexingShape.values())
            {
            this.indexing = indexing;
            for (int power = 1 ; power <= 3 ; power++ )
                {
                this.zonecount = Math.pow(10.0, power);
                find004();
                }
            }
        }
    }
    
