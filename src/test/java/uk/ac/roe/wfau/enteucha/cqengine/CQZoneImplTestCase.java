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

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;
import uk.ac.roe.wfau.enteucha.api.Zone;
import uk.ac.roe.wfau.enteucha.cqengine.CQZoneImpl;

/**
 * 
 * 
 */
@Slf4j
public class CQZoneImplTestCase
extends TestCase
    {

    /**
     * 
     */
    public CQZoneImplTestCase()
        {
        }

    /**
     * Initialise our data.
     * 
     */
    public Zone.ZoneSet small()
        {
        return init(
            new CQZoneImpl.ZoneSet(1000),
           -2.0,
            2.0,
            0.125
            );
        }

    /**
     * Initialise our data.
     * 
     */
    public Zone.ZoneSet init(final Zone.ZoneSet zones, double min, double max, double step)
        {
        int count = 0 ;
        for (double i = min ; i < max ; i += step)
            {
            for (double j = min ; j < max ; j += step)
                {
                zones.insert(
                    new PositionImpl(
                        i,
                        j
                        )
                    );
                count++;
                }
            }
        log.debug("inserted [{}]", count);
        return zones;
        }

    /**
     * Test adding things.
     * 
     */
    public void frogAddPositions()
        {
        final Zone.ZoneSet zones = small();
        }
    
    /**
     * Test finding things.
     * 
     */
    public void frogFindMatchesSmallSmall()
        {
        final Zone.ZoneSet zones = small();
        final Iterable<Position> matches = zones.matches(
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
        assertEquals(
            13,
            count
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void frogFindMatchesSmallMedium()
        {
        final Zone.ZoneSet zones = small();
        final Iterable<Position> matches = zones.matches(
            new PositionImpl(
                1.20,
                1.20
                ),
            0.50
            );
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        assertEquals(
            52,
            count
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void frogFindMatchesSmallLarge()
        {
        final Zone.ZoneSet zones = small();
        final Iterable<Position> matches = zones.matches(
            new PositionImpl(
                1.20,
                1.20
                ),
            0.75
            );
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
        assertEquals(
            158,
            count
            );
        }

    /**
     * Test finding things.
     * 
     */
    public void frogFindMatchesLarge()
        {
long a = System.currentTimeMillis();
        final Zone.ZoneSet zones = init(
            new CQZoneImpl.ZoneSet(10000),
            0.0,
            2.0,
            0.001
            );
long b = System.currentTimeMillis();

log.info("--------------------------");

long c = System.currentTimeMillis();
final Iterable<Position> matches = zones.matches(
            new PositionImpl(
                1.20,
                1.20
                ),
            0.002
            );
long d = System.currentTimeMillis();

log.info("--------------------------");
log.info("Total inserted [{}] in [{}]ms avg [{}]ms", zones.total(), (b-a),((float)(b-a)/(float)zones.total()) );
log.info("--------------------------");
        int count = 0 ;
        for (Position match : matches)
            {
            log.info("Found [{}][{}]", match.ra(), match.dec());
            count++;
            }
log.info("--------------------------");
        log.info("Total found [{}] in [{}]ms", count, (d-c));
log.info("--------------------------");
        }
    }

