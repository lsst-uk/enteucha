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

package uk.ac.roe.wfau.enteucha.api;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;
import uk.ac.roe.wfau.enteucha.hsqldb.HsqlMatcherImpl;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;

/**
 * 
 * 
 */
@Slf4j
public abstract class AbstractTestCase
extends TestCase
    {

    /**
     * 
     */
    public AbstractTestCase()
        {
        }

    /**
     * Create our {@link Matcher}.
     * 
     */
    public abstract Matcher matcher();

    /**
     * Initialise our {@link Matcher}.
     * 
     */
    public Matcher init(final Matcher matcher, double min, double max, double step)
        {
        log.debug("---- ---- ---- ----");
        matcher.init();
        log.debug("---- ---- ---- ----");
        log.debug("Starting data insert");
        int count = 0 ;
        long start = System.currentTimeMillis();
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
        long end = System.currentTimeMillis();
        log.debug("---- ---- ---- ----");
        log.debug("Finished data insert");
        log.debug("Inserted [{}] in [{}ms], average [{}]", count, (end - start), ((end - start)/count));
        log.debug("---- ---- ---- ----");
        return matcher;
        }

    /**
     * Test our {@link Matcher}.
     * 
     */
    public Iterable<Position> match(final Matcher matcher, final Position target, Double radius) 
        {
        log.debug("---- ---- ---- ----");
        log.debug("Starting crossmatch");
        long start = System.currentTimeMillis();
        Iterable<Position> matches = matcher.matches(
            target,
            radius
            );
        long end = System.currentTimeMillis();
        log.debug("---- ---- ---- ----");
        log.debug("Finished crossmatch");
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        log.info("Found [{}] in [{}ms]", count, (end - start));
        log.debug("---- ---- ---- ----");
        return matches ;
        }

    /**
     * Test adding things.
     * 
     */
    public void xestInit()
        {
        final Matcher matcher = this.init(
            this.matcher(),
           -2.0,
            2.0,
            0.25
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void xestFind001()
        {
        log.debug("Setting up test");
        final Matcher matcher = this.init(
            this.matcher(),
           -2.0,
            2.0,
            0.25
            );
        log.debug("Running crossmatch");
        this.match(
            matcher,
            new PositionImpl(
                1.20,
                1.20
                ),
            0.25
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void xestFind002()
        {
        log.debug("Setting up test");
        final Matcher matcher = this.init(
            this.matcher(),
           -2.0,
            2.0,
            0.125
            );
        log.debug("Running crossmatch");
        this.match(
            matcher,
            new PositionImpl(
                1.20,
                1.20
                ),
            0.125
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void testFind003()
        {
        log.debug("Setting up test");
        final Matcher matcher = this.init(
            this.matcher(),
           -2.0,
            2.0,
            0.025
            );
        log.debug("Running crossmatch");
        this.match(
            matcher,
            new PositionImpl(
                1.20,
                1.20
                ),
            0.025
            );
        }
    }


