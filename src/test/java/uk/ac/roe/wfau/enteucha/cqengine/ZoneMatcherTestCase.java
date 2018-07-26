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
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.AbstractTestCase;
import uk.ac.roe.wfau.enteucha.cqengine.ZoneMatcher;
import uk.ac.roe.wfau.enteucha.cqengine.ZoneMatcher.IndexingShape;
import uk.ac.roe.wfau.enteucha.cqengine.ZoneMatcherImpl;

/**
 * 
 * 
 */
@Slf4j
public class ZoneMatcherTestCase
extends AbstractTestCase
    {

    /**
     * Public constructor.
     * 
     */
    public ZoneMatcherTestCase()
        {
        super();
        }

    /**
     * Test finding things.
     * 
     */
    public void testFind004()
        {
        for (IndexingShape indexshape : IndexingShape.values())
            {
            for (int power = 1 ; power < 7 ; power++ )
                {
                final double zonecount = Math.pow(10.0, power);
                final IndexingShape indexing = indexshape ;
                findtest(
                    new Matcher.Factory()
                        {
                        @Override
                        public Matcher create()
                            {
                            return new ZoneMatcherImpl(
                                indexing,
                                zonecount
                                );
                            }
                        }
                    );
                }
            }
        }
    }
    