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

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;

/**
 * 
 * 
 */
@Slf4j
public class HsqlMatcherImplTestCase extends TestCase
    {

    /**
     * 
     */
    public HsqlMatcherImplTestCase()
        {
        }

    /**
     * Initialise our data.
     * 
     */
    public Matcher init(final Matcher matcher, double min, double max, double step)
        {
        int count = 0 ;
        for (double i = min ; i < max ; i += step)
            {
            for (double j = min ; j < max ; j += step)
                {
                matcher.insert(
                    new PositionImpl(
                        i,
                        j
                        )
                    );
                count++;
                }
            }
        log.debug("inserted [{}]", count);
        return matcher;
        }

    /**
     * Test adding things.
     * 
     */
    public void frogAddPositions()
        {
        final Matcher matcher = new HsqlMatcherImpl(1000);
        matcher.init();
        this.init(
            matcher,
           -2.0,
            2.0,
            0.125
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void frogFindMatchesSmall()
        {
        final Matcher matcher = new HsqlMatcherImpl(1000);
        matcher.init();
        this.init(
            matcher,
           -2.0,
            2.0,
            0.125
            );
        final Iterable<Position> matches = matcher.matches(
            new PositionImpl(
                1.20,
                1.20
                ),
            0.25
            );
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        log.info("Count [{}]",count);
        }

    /**
     * Test finding things.
     * 
     */
    public void testFindMatchesLarge()
        {
        Matcher matcher = new HsqlMatcherImpl(10000);
        matcher.init();
        this.init(
            matcher,
            0.0,
            2.0,
            0.001
            );
        final Iterable<Position> matches = matcher.matches(
            new PositionImpl(
                1.20,
                1.20
                ),
            0.002
            );
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        log.info("Count [{}]",count);
        }
    
    
    
    public void frogSmall()
        {
        final Matcher matcher = new HsqlMatcherImpl(1000);
        matcher.init();
        this.init(
            matcher,
           -2.0,
            2.0,
            0.125
            );
        final Iterable<Position> matches = matcher.verify();
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        log.info("Count [{}]",count);
        }

    }
