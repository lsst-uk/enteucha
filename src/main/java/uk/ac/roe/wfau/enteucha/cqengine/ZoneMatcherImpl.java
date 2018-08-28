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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.quantizer.DoubleQuantizer;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;
import uk.ac.roe.wfau.enteucha.util.GenericIterable;

/**
 * A CQEngine based implementation of {@link ZoneMatcher}
 * TODO Add validation for out of range values.
 * TODO Add +360 and -360 copies for positions within margins of ra = 0 or 360.
 *  
 */
@Slf4j
public class ZoneMatcherImpl
implements ZoneMatcher
    {

    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final double epsilon = 10E-6;

    /**
     * The height in degrees of the {link Zone}s in this set.
     * 
     */
    private double zoneheight ;

    @Override
    public double height()
        {
        return this.zoneheight;
        }
    
    /**
     * The {@link IndexingShape} for this {@link ZoneMatcher}.
     * 
     */
    protected IndexingShape indexing ;

    /**
     * The {@link IndexingShape} for this {@link ZoneMatcher}.
     * 
     */
    @Override
    public IndexingShape indexing()
        {
        return this.indexing;
        }


    /**
     * Initialise this {@link ZoneMatcher}.
     * 
     */
    public void init()
        {
        }

    /**
     * Public constructor.
     * 
     */
    public ZoneMatcherImpl(final IndexingShape indexing, double zoneheight)
        {
        this.indexing = indexing ;
        this.zoneheight = zoneheight ;
        }

    long zonetotal = 0 ;
    long zonecount = 0 ;

    long mathtotal = 0 ;
    long mathcount = 0 ;

    long radectotal = 0 ;
    long radeccount = 0 ;
    
    @Override
    public Iterable<Position> matches(final Position target, final Double radius)
        {
        //log.trace("matches() [{}][{}][{}]", target.ra(), target.dec(), radius);

        this.zonetotal = 0 ;
        this.zonecount = 0 ;

        this.mathtotal = 0 ;
        this.mathcount = 0 ;

        this.radectotal = 0 ;
        this.radeccount = 0 ;
        
        final List<Position> list = new ArrayList<Position>(100);  
        for (Zone zone : contains(target, radius))
            {
            //log.trace("Checking zone [{}][{}]", zone.ident(), zone.total());
            zonecount++;
            for (Position match : zone.matches(target, radius))
                {
                //log.trace("Found match [{}][{}]", match.ra(), match.dec());
                list.add(match);
                }
            }

        //log.trace("Math cx/cy/zc compare [{}] took [{}µs][{}ns] avg [{}µs][{}ns]", (this.mathcount),  (this.mathtotal/1000),  (this.mathtotal),  (this.mathtotal/(this.mathcount * 1000)),   (this.mathtotal/this.mathcount));
        //log.trace("Zone ra/dec/pos query [{}] took [{}µs][{}ns] avg [{}µs][{}ns]", (this.radeccount), (this.radectotal/1000), (this.radectotal), (this.radectotal/(this.radeccount * 1000)), (this.radectotal/this.radeccount));
        //log.trace("Zone between found [{}] from [{}] took [{}µs][{}ns]", (zonecount), zones.size(), ((zonetotal)/1000), (zonetotal) );
        
        return list ;
        }
        
    @Override
    public Iterable<Zone> contains(final Position target, final Double radius)
        {
        //log.trace("contains() [{}][{}][{}]", target.ra(), target.dec(), radius);
        //log.trace("height [{}]",  this.zoneheight);

        final Integer min = (int) FastMath.floor(((target.dec() + 90) - radius) / this.zoneheight) ;
        final Integer max = (int) FastMath.floor(((target.dec() + 90) + radius) / this.zoneheight) ;

        //log.trace("min [{}]", min);
        //log.trace("max [{}]", max);

        return new GenericIterable<Zone, ZoneImpl>(
            between(
                min,
                max
                )
            );
        }
        
    /**
     * Select a {@link ResultSet} of {@link ZoneImpl}s between an upper and lower bound.  
     * 
     */
    protected ResultSet<ZoneImpl> between(final Integer min, final Integer max)
        {
        //log.trace("between() [{}][{}]", min, max);
        long zonestart = System.nanoTime();
        final ResultSet<ZoneImpl>  results = zones.retrieve(
            QueryFactory.between(
                ZoneMatcherImpl.ZONE_ID,
                min,
                true,
                max,
                true
                )
            );

        long zonedone = System.nanoTime();
        long zonediff = zonedone - zonestart;
        this.zonetotal += zonediff;  
        //log.trace("Zone between took [{}µs][{}ns]", ((zonediff)/1000), (zonediff) );
        return results ; 
        }

    /**
     * Select a {@link ZoneImpl} based on the {@link Zone} identifier.
     * @todo Does creating a new Zone need to be ThreadSafe ?
     * @todo Add a create flag
     *
     */
    protected Zone select(final Integer ident)
        {
        //log.trace("select() [{}]", ident);
        final Iterator<ZoneImpl> iter = zones.retrieve(
            QueryFactory.equal(
                ZoneMatcherImpl.ZONE_ID,
                ident
                )
            ).iterator();
        if (iter.hasNext())
            {
            final ZoneImpl found = iter.next();
            return found ;
            }
        else {
            final ZoneImpl created = new ZoneImpl(
                ident
                ) ;
            zones.add(
                created
                );
            //log.trace("New zone [{}][{}]", created.ident(), zones.size());
            return created ;
            }
        }

    @Override
    public void insert(final Position position)
        {
        //log.debug("insert() [{}][{}]", position.ra(), position.dec());
        final Zone zone = select(
                (int) FastMath.floor((position.dec() + 90) / this.zoneheight)
                );
        //log.debug("Zone [{}]", zone.ident());
        zone.insert(
            position
            );
        this.total++;
        //log.debug("Added [{}][{}]", zone.total(), total());
        }

    /**
     * The total count of {@link Position}s inserted into this {@link ZoneMatcher}.
     *
     */
    private long total = 0 ;

    /**
     * The total count of {@link Position}s inserted into this {@link ZoneMatcher}.
     *
     */
    public long total()
        {
        return this.total;
        }

    /**
     * Our collection of {@link Zone}s, indexed by {@link ZoneImpl.ZONE_ID}. 
     * 
     */
    private final IndexedCollection<ZoneImpl> zones = new ConcurrentIndexedCollection<ZoneImpl>(
        OnHeapPersistence.onPrimaryKey(
            ZoneMatcherImpl.ZONE_ID
            )
        );

    /**
     * The CQEngine {@link Attribute} for a {@link Zone} identifier.
     * 
     */
    public static final SimpleAttribute<ZoneImpl, Integer> ZONE_ID = new SimpleAttribute<ZoneImpl, Integer>("zone.id")
        {
        @Override
        public Integer getValue(final ZoneImpl zone, final QueryOptions options)
            {
            return zone.ident();
            }
        };

    @Override
    public String info()
        {
        final StringBuilder builder = new StringBuilder(); 
        builder.append("Class [");
        builder.append(this.getClass().getSimpleName());
        builder.append("] ");
        builder.append("Indexing [");
        builder.append(this.indexing.name());
        builder.append("] ");
        builder.append("Height [");
        builder.append(this.zoneheight);
        builder.append("] ");

        long subcount = 0 ;
        long subtotal = 0 ;
        long maxtotal = 0 ;
        long mintotal = this.total ;
        for (Zone zone : zones)
            {
            //builder.append("Zone [");
            //builder.append(zone.config());
            //builder.append("] ");
            
            subcount++;
            subtotal += zone.total();
            if (zone.total() > maxtotal)
                {
                maxtotal = zone.total();
                }
            if (zone.total() < mintotal)
                {
                mintotal = zone.total();
                }
            }
        builder.append("Zone count [");
        builder.append(subcount);
        builder.append("] ");
        builder.append("Zone size [");
        builder.append((subtotal/subcount));
        builder.append("]");
        builder.append("[");
        builder.append((maxtotal));
        builder.append("]");
        builder.append("[");
        builder.append((mintotal));
        builder.append("]");
        
        return builder.toString();
        }

    /**
     * A CQEngine based implementation of {@link ZoneMatcher.Zone}
     * 
     */
    public class ZoneImpl
    implements ZoneMatcher.Zone
        {
    
        /**
         * Protected constructor.
         * 
         */
        protected ZoneImpl(int ident)
            {
            this.ident = ident;
            this.init();
            }

        private int ident;
        @Override
        public int ident()
            {
            return this.ident;
            }

        @Override
        public Iterable<Position> matches(final Position target, final Double radius)
            {
            final Iterable<Position> results = filter(
                query(
                    target,
                    radius
                    ),
                target,
                radius
                );
            return results;
            }
        
        /**
         * Filter a list of candidates, checking if they are within the search radius of a target {@link Position}.
         *
         */
        protected Iterable<Position> filter(final Iterable<PositionImpl> candidates, final Position target, final Double radius)
            {
            return new Iterable<Position>()
                {
                @Override
                public Iterator<Position> iterator()
                    {
                    return new Iterator<Position>()
                        {
                        private final Iterator<PositionImpl> iter = candidates.iterator(); 
                        private Position next = step();

                        /**
                         * Get the next candidate and check if it is within range.
                         *
                         */
                        protected Position step()
                            {
                            for (int count = 0 ; iter.hasNext(); count++)
                                {
                                //log.trace("loop [{}]", count);
                                final Position temp = iter.next();
                                if (check(target, radius, temp))
                                    {
                                    return temp ;
                                    }
                                }
                            return null ;
                            }

                        @Override
                        public boolean hasNext()
                            {
                            return (next != null);
                            }

                        @Override
                        public Position next()
                            {
                            final Position temp = next ;
                            next = step();
                            return temp;
                            }
                        };
                    }
                };
            }

        /**
         * Check if a {@link Position} is within the search radius of a target {@link Position}
         * by calculating the distance between the cartesian coordinates. 
         *
         */
        protected boolean check(final Position target, final Double radius, final Position pos)
            {
            long mathstart = System.nanoTime();
            double squares =
                    FastMath.pow(
                        pos.cx() - target.cx(),
                        2
                        ) 
                  + FastMath.pow(
                        pos.cy() - target.cy(),
                        2
                        ) 
                  + FastMath.pow(
                      pos.cz() - target.cz(),
                      2
                      )
                    ;
            double squaresin = 4 * (
                    FastMath.pow(
                        FastMath.sin(
                            FastMath.toRadians(
                                radius
                                )/2
                            ),
                        2)
                    );

            boolean result = (squaresin > squares) ;
            long mathend = System.nanoTime();
            long mathdif = mathend - mathstart ;
            mathtotal += mathdif ;
            mathcount++;
            //log.trace("Math cx/cy/zc compare [{}]>[{}]=[{}] took [{}µs][{}ns]", squaresin, squares, result, (mathdif/1000), (mathdif));
            return result;
            }
        
        /**
         * Query our CQEngine collection for {@link Position}s within a search radius of a target {@link Position}.
         *
         */
        protected ResultSet<PositionImpl> query(final Position target, final Double radius)
            {
            //log.trace("query() [{}][{}][{}]", target.ra(), target.dec(), radius);

            double factor = radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon);
            double minra = target.ra() - factor;
            double maxra = target.ra() + factor;

            double mindec = (target.dec() - radius) ; 
            double maxdec = (target.dec() + radius) ; 

            //log.trace("min/max ra  [{}][{}]", minra,  maxra) ;
            //log.trace("min/max dec [{}][{}]", mindec, maxdec);
/*
 *
 * TODO Make this configurable.
            return positions.retrieve(
                QueryFactory.between(
                    ZoneMatcherImpl.POS_RA,
                    minra,
                    true,
                    maxra,
                    true
                    )
                );
 *
 */        
            long radecstart = System.nanoTime();

            final ResultSet<PositionImpl> results = positions.retrieve(
                QueryFactory.and(
                    QueryFactory.between(
                        ZoneMatcherImpl.POS_DEC,
                        mindec,
                        true,
                        maxdec,
                        true
                        ),
                    QueryFactory.between(
                        ZoneMatcherImpl.POS_RA,
                        minra,
                        true,
                        maxra,
                        true
                        )
                    )
                );

            long radecdone = System.nanoTime();
            long radecdiff = radecdone -radecstart;
            radectotal += radecdiff;
            radeccount++;

            //log.trace("Zone ra/dec query took [{}µs][{}ns]", (radecdiff/1000), (radecdiff) );
            return results ;
            }

        @Override
        public void insert(final Position position)
            {
            //log.trace("insert() [{}][{}]", position.ra(), position.dec());
            if (position instanceof PositionImpl)
                {
                positions.add(
                    (PositionImpl) position
                    );
                }
            else {
                throw new IllegalArgumentException(
                    "PositionImpl expected [" + position.getClass().getName() + "]"
                    );
                }
            }
    
        /**
         * Our collection of {@link Position}s. 
         * 
         */
        private final IndexedCollection<PositionImpl> positions = new ConcurrentIndexedCollection<PositionImpl>();

        @Override
        public long total()
            {
            return positions.size();
            }

        @Override
        @SuppressWarnings("unchecked")
        public void init()
            {
            switch(ZoneMatcherImpl.this.indexing)
                {
                case SEPARATE_SIMPLE:
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            ZoneMatcherImpl.POS_RA
                            )
                        );
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            ZoneMatcherImpl.POS_DEC
                            )
                        );
                    break ;

                case SEPARATE_QUANTIZED:
                    positions.addIndex(
                        NavigableIndex.withQuantizerOnAttribute(
                            DoubleQuantizer.withCompressionFactor(
                                5
                                ),
                            ZoneMatcherImpl.POS_RA
                            )
                        );
                    positions.addIndex(
                        NavigableIndex.onAttribute(
                            ZoneMatcherImpl.POS_DEC
                            )
                        );
                break ;
                
                case COMBINED_SIMPLE:
                    {
                    positions.addIndex(
                        CompoundIndex.onAttributes(
                            ZoneMatcherImpl.POS_RA,
                            ZoneMatcherImpl.POS_DEC
                            )
                        );
                    }
                    break ;
                default:
                    throw new IllegalArgumentException(
                        "Unknown indexing [{" + ZoneMatcherImpl.this.indexing.name() + "}]"
                        ); 
                }
            }

        @Override
        public String info()
            {
            final StringBuilder builder = new StringBuilder(); 
            builder.append("Class [");
            builder.append(this.getClass().getSimpleName());
            builder.append("] ");
            builder.append("Indexing [");
            builder.append(ZoneMatcherImpl.this.indexing.name());
            builder.append("]");
            builder.append("Total rows [");
            builder.append(String.format("%,d", this.total()));
            builder.append("]");
            return builder.toString();
            }

        @Override
        public double height()
            {
            return ZoneMatcherImpl.this.zoneheight;
            }

        @Override
        public Enum<?> indexing()
            {
            return ZoneMatcherImpl.this.indexing;
            }
        }

    /**
     * The CQEngine {@link Attribute} for a {@link PositionImpl} right ascension.
     * 
     */
    public static final SimpleAttribute<PositionImpl, Double> POS_RA = new SimpleAttribute<PositionImpl, Double>("pos.ra")
        {
        @Override
        public Double getValue(final PositionImpl position, final QueryOptions options)
            {
            return position.ra();
            }
        };

    /**
     * The CQEngine {@link Attribute} for a {@link PositionImpl} declination.
     * 
     */
    public static final SimpleAttribute<PositionImpl, Double> POS_DEC = new SimpleAttribute<PositionImpl, Double>("pos.dec")
        {
        @Override
        public Double getValue(final PositionImpl position, final QueryOptions options)
            {
            return position.dec();
            }
        };
    }

