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

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;
import uk.ac.roe.wfau.enteucha.api.Zone;
import uk.ac.roe.wfau.enteucha.util.GenericIterable;

/**
 * A CQEngine based implementation of {@link Zone}
 * 
 */
@Slf4j
public class CQZoneImpl implements Zone
    {

    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final double epsilon = 10E-6;
    
    /**
     * A CQEngine based implementation of {@link Zone.ZoneSet}
     * 
     */
    @Slf4j
    public static class ZoneSet
    implements Zone.ZoneSet
        {
        private int count = 0 ;
        private double height ;

        public void init()
            {
            }

        public ZoneSet(int count)
            {
            log.debug("CQZoneSetImpl() [{}]", count);
            this.height = 180.0 / (double) count ;
            }
        
        @Override
        public Iterable<Zone> contains(final Position target, final Double radius)
            {
            log.debug("contains() [{}][{}][{}]", target.ra(), target.dec(), radius);

            log.debug("height [{}]",  this.height);
            log.debug("dec [{}][{}]", target.dec(), (target.dec() + 90), ((target.dec() + 90) - radius));
            log.debug("min [{}][{}]", ((target.dec() + 90) - radius), (((target.dec() + 90) - radius) / this.height));
            log.debug("max [{}][{}]", ((target.dec() + 90) + radius), (((target.dec() + 90) + radius) / this.height));

            final Integer min = (int) Math.floor(((target.dec() + 90) - radius) / this.height) ;
            final Integer max = (int) Math.floor(((target.dec() + 90) + radius) / this.height) ;

            return new GenericIterable<Zone, CQZoneImpl>(
                between(
                    min,
                    max
                    )
                );
            }
        
        /**
         * Select a {@link ResultSet} of {@link CQZoneImpl}s between an upper and lower bound.  
         * 
         */
        protected ResultSet<CQZoneImpl> between(final Integer min, final Integer max)
            {
            log.debug("between() [{}][{}]", min, max);
            return zones.retrieve(
                QueryFactory.between(
                    CQZoneImpl.ZONE_ID,
                    min,
                    true,
                    max,
                    true
                    )
                );
            }

        /**
         * Select a {@link CQZoneImpl} based on identifier.
         * Creates a new zone if needed.  
         * 
         */
        protected Zone select(final Integer ident)
            {
            log.debug("select() [{}]", ident);
            final Iterator<CQZoneImpl> iter = zones.retrieve(
                QueryFactory.equal(
                    CQZoneImpl.ZONE_ID,
                    ident
                    )
                ).iterator();
            if (iter.hasNext())
                {
                return iter.next();
                }
            else {
                final CQZoneImpl temp = new CQZoneImpl(
                    ident
                    ) ;
                zones.add(temp);
                log.debug("New zone [{}][{}]", temp.ident(), zones.size());
                return temp ;
                }
            }

        /**
         * Our collection of {@link Zone}s, indexed by {@link CQZoneImpl.ZONE_ID}. 
         * 
         */
        private final IndexedCollection<CQZoneImpl> zones = new ConcurrentIndexedCollection<CQZoneImpl>(
            OnHeapPersistence.onPrimaryKey(
                CQZoneImpl.ZONE_ID
                )
            );
        
        @Override
        public Iterable<Position> matches(Position target, Double radius)
            {
            log.debug("matches() [{}][{}][{}]", target.ra(), target.dec(), radius);
            final List<Position> list = new ArrayList<Position>(100);  
            for (Zone zone : contains(target, radius))
                {
                log.debug("Checking zone [{}][{}]", zone.ident(), zone.count());
                for (Position match : zone.matches(target, radius))
                    {
                    //log.debug("Found match [{}][{}]", match.ra(), match.dec());
                    list.add(match);
                    }
                }
            return list ;
            }

        private long total = 0 ;
        
        @Override
        public void insert(final Position position)
            {
            log.debug("insert() [{}][{}][{}]", count++, position.ra(), position.dec());
            final Zone zone = select(
                    (int) Math.floor((position.dec() + 90) / this.height)
                    );
            log.debug("Zone [{}]", zone.ident());
            zone.insert(
                position
                );
            total++;
            log.debug("Added [{}][{}]", zone.count(), total);
            }

        public long total()
            {
            return this.total;
            }

        @Override
        public Iterable<Position> verify()
            {
            // TODO Auto-generated method stub
            return null;
            }
        }
    
    /**
     * Protected constructor.
     * 
     */
    protected CQZoneImpl(int ident)
        {
        this.ident = ident;
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
        log.debug("matches() [{}][{}][{}]", target.ra(), target.dec(), radius);
        return filter(
            target,
            radius,
            query(
                target,
                radius
                )
            );
        }

    protected boolean match(final Position target, final Double radius, final Position pos)
        {
        log.debug("match() [{}][{}]:[{}][{}] [{}]", target.ra(), target.dec(), pos.ra(), pos.dec(), radius);
        double squares =
                Math.pow(
                    pos.cx() - target.cx(),
                    2
                    ) 
              + Math.pow(
                    pos.cy() - target.cy(),
                    2
                    ) 
              + Math.pow(
                  pos.cz() - target.cz(),
                  2
                  ) 
                ;
        double range = 4 * (
                Math.pow(
                    Math.sin(
                        Math.toRadians(
                            radius
                            )/2
                        ),
                    2)
                );

        boolean result = (range > squares) ;
        log.debug("compare [{}]>[{}]=[{}]", range, squares, result);
        return result;
        }
    
    protected Iterable<Position> filter(final Position target, final Double radius, final Iterable<PositionImpl> results)
        {
        return new Iterable<Position>()
            {
            @Override
            public Iterator<Position> iterator()
                {
                return new Iterator<Position>()
                    {
                    final Iterator<PositionImpl> iter = results.iterator(); 
                    Position next = step();

                    protected Position step()
                        {
                        for (int count = 0 ; iter.hasNext(); count++)
                            {
                            log.debug("loop [{}]", count);
                            final Position temp = iter.next();
                            if (match(target, radius, temp))
                                {
                                log.debug("found   [{}][{}]", temp.ra(), temp.dec());
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
    
    protected ResultSet<PositionImpl> query(final Position target, final Double radius)
        {
        log.debug("query() [{}][{}][{}]", target.ra(), target.dec(), radius);

        double minra = (target.ra() - radius) / (Math.cos(Math.toRadians(Math.abs(target.dec()))) + epsilon);
        double maxra = (target.ra() + radius) / (Math.cos(Math.toRadians(Math.abs(target.dec()))) + epsilon);

        double mindec = (target.dec() - radius) ; 
        double maxdec = (target.dec() + radius) ; 

        log.debug("min max ra  [{}][{}]", minra,  maxra) ;
        log.debug("min max dec [{}][{}]", mindec, maxdec);
/*
 *
        return positions.retrieve(
            QueryFactory.between(
                CQZoneImpl.POS_RA,
                minra,
                true,
                maxra,
                true
                )
            );
 *
 */        
        return positions.retrieve(
            QueryFactory.and(
                QueryFactory.between(
                    CQZoneImpl.POS_DEC,
                    mindec,
                    true,
                    maxdec,
                    true
                    ),
                QueryFactory.between(
                    CQZoneImpl.POS_RA,
                    minra,
                    true,
                    maxra,
                    true
                    )
                )
            );
        }

    @Override
    public void insert(final Position position)
        {
        log.debug("insert() [{}][{}]", position.ra(), position.dec());
        if (position instanceof PositionImpl)
            {
            positions.add(
                (PositionImpl) position
                );
            }
        else {
            positions.add(
                new PositionImpl(
                    position
                    )
                );
            }
        }

    /**
     * Our collection of {@link Position}s, indexed on {@link PositionImpl.POS_RA} and {@link PositionImpl.POS_DEC}. 
     * 
     */
    private final IndexedCollection<PositionImpl> positions = new ConcurrentIndexedCollection<PositionImpl>();
        {
/*
        positions.addIndex(
            CompoundIndex.onAttributes(
                CQZoneImpl.POS_RA,
                CQZoneImpl.POS_DEC
                )
            );
 */

        positions.addIndex(
            NavigableIndex.onAttribute(
                CQZoneImpl.POS_RA
                )
            );
        positions.addIndex(
            NavigableIndex.onAttribute(
                CQZoneImpl.POS_DEC
                )
            );
        }

    @Override
    public int count()
        {
        return positions.size();
        }

    public void init()
        {
        }
    
    /**
     * CQEngine {@link Attribute} for a zone identifier.
     * 
     */
    public static final SimpleAttribute<CQZoneImpl, Integer> ZONE_ID = new SimpleAttribute<CQZoneImpl, Integer>("zone.id")
        {
        @Override
        public Integer getValue(final CQZoneImpl zone, final QueryOptions options)
            {
            return zone.ident();
            }
        };
    
    /**
     * CQEngine {@link Attribute} for a {@link PositionImpl} right ascension.
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
     * CQEngine {@link Attribute} for a {@link PositionImpl} declination.
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
    @Override
    public Iterable<Position> verify()
        {
        // TODO Auto-generated method stub
        return null;
        }
    }

