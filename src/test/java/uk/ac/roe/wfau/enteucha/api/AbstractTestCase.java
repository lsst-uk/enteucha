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
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.api.Position;

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

    int looprepeat = 10;    
   
    int    insertdepth = 9;
    double insertrange = 2;

    int grain = -6 ;
    
    /**
     * Test finding things.
     * 
     */
    public void findtest(final Matcher.Factory factory)
        {
        log.info("---- find start ----");
        outer(
            factory,
            new PositionImpl(
                120.0,
                120.0
                )
            );
        try {
            log.info("---- find finalize ----");
            System.runFinalization();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        try {
            log.info("---- find gc ----");
            System.gc();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        log.info("---- find done ----");
        }

    public void outer(final Matcher.Factory factory, final Position target)
        {
        for (int zexp = 0 ; zexp > (this.grain - 2) ; zexp-- )
            {
            double zoneheight = Math.pow(2.0, zexp);
            log.info("---- Zone height [{}]", zoneheight);
            final Matcher matcher = factory.create(
                zoneheight
                );
            matcher.insert(
                target
                );
            for (double a = 0 ; a < insertdepth ; a++)
                {
                double b = Math.pow(2.0, a);
                log.info("---- Insert depth [{}][{}]", a, b);
                for (double c = -b ; c <= b ; c++)
                    {
                    for (double d = -b ; d <= b ; d++)
                        {
                        if ((c % 2) == 0)
                            {
                            if ((d % 2) == 0)
                                {
                                continue;
                                }
                            }
                        matcher.insert(
                            new PositionImpl(
                                (target.ra()  + (insertrange * (-c/b))),
                                (target.dec() + (insertrange * (+d/b)))
                                )
                            );
                        }
                    }
                inner(
                    matcher,
                    target 
                    );
                }
            }
        }

    public void inner(final Matcher matcher, final Position target)
        {
        for (int rexp = 0 ; rexp > this.grain ; rexp-- )
            {
            double radius = Math.pow(2.0, rexp);
            log.info("---- Search radius[{}]", radius);

            long looptime  = 0 ;
            long loopcount = 0 ;
            for(int loop = 0 ; loop < this.looprepeat ; loop++)
                {
                //long matchcount = 0 ;
                //log.debug("---- ---- ---- ----");
                //log.debug("Starting crossmatch");
                long innerstart = System.nanoTime();
                Iterable<Position> matches = matcher.matches(
                    target,
                    radius
                    );
                //long innermid = System.nanoTime();
                for (Position match : matches)
                    {
                    //log.debug("Found [{}][{}]", match.ra(), match.dec());
                    loopcount++;
                    //matchcount++;
                    }
                long innerend = System.nanoTime();
                //long innerone = innermid - innerstart ;
                //long innertwo = innerend - innermid ;
                long innertime = innerend - innerstart;
                looptime += innertime ;
                //log.debug("---- ---- ---- ----");
                //log.debug("Finished crossmatch");
                //log.debug("Found [{}] in [{}µs {}ns][{}µs {}ns][{}µs {}ns]", matchcount, (innerone/1000), innerone, (innertwo/1000), innertwo, (innertime/1000), innertime);
                //log.debug("Found [{}] in [{}ms][{}µs][{}ns]", matchcount, (innertime/1000000), (innertime/1000), innertime);
                }
            log.info(
                "Matcher [{}]",
                matcher.config()
                );
            log.info(
                "Searched [{}] radius [{}] zone [{}] found [{}] in [{}] loops, total [{}s][{}ms][{}µs][{}ns], average [{}ms][{}µs][{}ns] {}",
                String.format("%,d", matcher.total()),
                radius,
                matcher.height(),
                (loopcount/this.looprepeat),
                this.looprepeat,
                (looptime/1000000000),
                (looptime/1000000),
                (looptime/1000),
                (looptime),
                (looptime/(this.looprepeat * 1000000)),
                (looptime/(this.looprepeat * 1000)),
                (looptime/this.looprepeat),
                (((looptime/this.looprepeat) < 1000000) ? "PASS" : "FAIL")
                );
            }
        }
    }


    
