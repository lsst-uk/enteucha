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

import org.apache.commons.math3.util.FastMath;

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

    double spread = 2;

    int spreadmin = 1;
    int spreadmax = 7;

    int insertmin = 8;
    int insertmax = 10;

    int zonemin = 6 ;
    int zonemax = 8 ;

    int radiusmin = 6 ;
    int radiusmax = 8 ;
    
    /**
     * Test finding things.
     * 
     */
    public void outerloop(final Matcher.Factory factory)
        {
        final Runtime runtime = Runtime.getRuntime();
        final Position target = new PositionImpl(
            120.0,
            120.0
            );
        log.info("Target [{}][{}]", target.ra(), target.dec());
        for (int exponent = this.zonemin ; exponent <= this.zonemax ; exponent++ )
            {
            double zoneheight = FastMath.pow(2.0, -exponent);
            log.info("Zone height [{}]", zoneheight);
            final Matcher matcher = factory.create(
                zoneheight
                );
            matcher.insert(
                target
                );
            for (double a = 0 ; a < insertmax ; a++)
                {
                double b = FastMath.pow(2.0, a);
                log.info("Insert depth [{}][{}]", a, b);
                log.info("Memory [{}][{}][{}]", humanSize(runtime.totalMemory()), humanSize(runtime.freeMemory()), humanSize(runtime.maxMemory()));
                for (double c = -b ; c <= b ; c++)
                    {
                    long cmantissa = Double.doubleToLongBits(c) & 0x000fffffffffffffL ;
                    if (cmantissa == 0L)
                        {
                        log.debug("--- C [{}][{}]", c, Long.toHexString(cmantissa));
                        }
                    for (double d = -b ; d <= b ; d++)
                        {
                        if ((((long)c) % 2) == 0)
                            {
                            if ((((long) d) % 2) == 0)
                                {
                                continue;
                                }
                            }
                        matcher.insert(
                            new PositionImpl(
                                (target.ra()  + (spread * (-c/b))),
                                (target.dec() + (spread * (+d/b)))
                                )
                            );
                        }
                    }
                if (a >= insertmin)
                    {
                    log.info("Memory [{}][{}][{}]", humanSize(runtime.totalMemory()), humanSize(runtime.freeMemory()), humanSize(runtime.maxMemory()));
                    innerloop(
                        matcher,
                        target 
                        );
                    }
                }
            }
        try {
            log.info("---- Finalize");
            System.runFinalization();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        try {
            log.info("---- Running gc");
            System.gc();
            Thread.sleep(10000);
            }
        catch (final InterruptedException ouch)
            {
            log.debug("InterruptedException [{}]", ouch);
            }
        }


    public void innerloop(final Matcher matcher, final Position target)
        {
        for (int exponent = this.radiusmin ; exponent <= this.radiusmax ; exponent++ )
            {
            double radius = FastMath.pow(2.0, -exponent);
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
                for (Position match : matches)
                    {
                    //log.debug("Found [{}][{}]", match.ra(), match.dec());
                    loopcount++;
                    //matchcount++;
                    }
                long innerend = System.nanoTime();
                long innertime = innerend - innerstart;
                looptime += innertime ;
                //log.debug("---- ---- ---- ----");
                //log.debug("Finished crossmatch");
                //log.debug("Found [{}] in [{}µs {}ns][{}µs {}ns][{}µs {}ns]", matchcount, (innerone/1000), innerone, (innertwo/1000), innertwo, (innertime/1000), innertime);
                //log.debug("Found [{}] in [{}ms][{}µs][{}ns]", matchcount, (innertime/1000000), (innertime/1000), innertime);
                }
            log.info(
                matcher.info()
                );
            log.info(
                "Searched [{}] radius [{}] found [{}] in [{}] loops, total [{}s][{}ms][{}µs][{}ns], average [{}ms][{}µs][{}ns] {}",
                String.format("%,d", matcher.total()),
                radius,
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

    /**
     * Format a data size as a human readable String.
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html 
     * 
     */
    public static String humanSize(long bytes)
        {
        return humanSize(bytes, false);
        }
    
    /**
     * Format a data size as a human readable String.
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html 
     * 
     */
    public static String humanSize(long bytes, boolean si)
        {
        int unit = (si) ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exponent = (int) (Math.log(bytes) / Math.log(unit));
        final String prefix = (si ? "kMGTPE" : "KMGTPE").charAt(exponent-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exponent), prefix);
        }
    }


    
