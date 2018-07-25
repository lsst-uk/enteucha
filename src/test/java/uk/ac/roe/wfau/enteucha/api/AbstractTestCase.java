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
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;

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
        long count = 0 ;
        long nanostart = System.nanoTime();
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
        long nanoend  = System.nanoTime();
        long nanodiff = nanoend - nanostart;
        
        log.debug("---- ---- ---- ----");
        log.debug("Finished data insert");
        log.info(
            "Inserted [{}] in [{}s][{}ms][{}µs][{}ns], average [{}ms][{}µs][{}ns]",
            String.format("%,d", count),
            (nanodiff/1000000000),
            (nanodiff/1000000),
            (nanodiff/1000),
            (nanodiff),
            (nanodiff/(count * 1000000)),
            (nanodiff/(count * 1000)),
            (nanodiff/count)
            );
        log.debug("---- ---- ---- ----");
        return matcher;
        }

    /**
     * Test our {@link Matcher}.
     * 
     */
    public long match(final Matcher matcher, final Position target, Double radius) 
        {
        log.debug("---- ---- ---- ----");
        log.debug("Starting crossmatch");
        long nanostart = System.nanoTime();
        Iterable<Position> matches = matcher.matches(
            target,
            radius
            );
        int count = 0 ;
        for (Position match : matches)
            {
            log.debug("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        long nanoend = System.nanoTime();
        long nanodiff = nanoend - nanostart ;
        log.debug("---- ---- ---- ----");
        log.debug("Finished crossmatch");
        log.debug("Found [{}] in [{}ns]", count, nanodiff);
        log.debug("---- ---- ---- ----");
        return nanodiff ;
        }

    
    //double spacing = 0.0025;  
    //double radius  = 0.0025;  

    double spacing = 0.025;  
    double radius  = 0.025;  
    
    /**
     * Test finding things.
     * 
     */
    public void find004()
        {
        log.debug("Setting up test");
        final Matcher matcher = this.init(
            this.matcher(),
           -2.0,
            2.0,
            spacing
            );
        long nanosec = 0 ;
        long loop;
        for(loop = 0 ; loop < 4 ; loop++)
            {
            log.debug("Running crossmatch");
            nanosec += this.match(
                matcher,
                new PositionImpl(
                    1.20,
                    1.20
                    ),
                radius
                );
            }
        log.info(
            "Matcher [{}]",
            matcher.config()
            );
        log.info(
            "Searched [{}] in [{}] loops, total [{}s][{}ms][{}µs][{}ns], average [{}ms][{}µs][{}ns]",
            String.format("%,d", matcher.total()),
            loop,
            (nanosec/1000000000),
            (nanosec/1000000),
            (nanosec/1000),
            (nanosec),
            (nanosec/(loop * 1000000)),
            (nanosec/(loop * 1000)),
            (nanosec/loop)
            );
        }
    }


