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

package uk.ac.roe.wfau.enteucha.hsqldb;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.AbstractTestCase;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;
import uk.ac.roe.wfau.enteucha.hsqldb.HsqlMatcherImpl.IndexingShape;

/**
 * 
 * 
 */
@Slf4j
public class HsqlMatcherImplTestCase
extends AbstractTestCase
    {

    public HsqlMatcherImplTestCase()
        {
        super();
        }

    /**
     * The {@link IndexingShape} for this {@link TestCase}.
     * 
     */
    private IndexingShape indexing ;
    
    
    @Override
    public Matcher matcher()
        {
        return new HsqlMatcherImpl(
            indexing ,
            1000
            );
        }

    
    /**
     * Test finding things.
     * 
     */
    public void testFind004()
        {
        this.indexing = IndexingShape.SEPARATE;
        find004();
        
        this.indexing = IndexingShape.COMBINED;
        find004();

        this.indexing = IndexingShape.COMPLEX;
        find004();
        }
    }
