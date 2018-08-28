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

import org.junit.Test;

import uk.ac.roe.wfau.enteucha.api.AbstractTestCase;
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.hsqldb.HsqlMatcherImpl.IndexingShape;

/**
 * 
 * 
 */
public class HsqlMatcherTestCase
extends AbstractTestCase
    {

    /**
     * Public constructor.
     * 
     */
    public HsqlMatcherTestCase()
        {
        super();
        }

    /**
     * Test finding things.
     * 
     */
    @Test
    public void testFind()
        {
        for (IndexingShape indexshape : IndexingShape.values())
            {
            final IndexingShape indexing = indexshape ;
            outerloop(
                new Matcher.Factory()
                    {
                    @Override
                    public Matcher create(double zoneheight)
                        {
                        return new HsqlMatcherImpl(
                            indexing,
                            zoneheight
                            );
                        }
                    }
                );
            }
        }
    }
