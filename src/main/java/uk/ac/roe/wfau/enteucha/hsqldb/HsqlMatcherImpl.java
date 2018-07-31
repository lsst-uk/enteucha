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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.math3.util.FastMath;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Matcher;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.PositionImpl;

/**
 * 
 * 
 */
@Slf4j
public class HsqlMatcherImpl implements Matcher
    {
    /**
     * Public constructor.
     * 
     */
    public HsqlMatcherImpl(int count)
        {
        this(
            IndexingShape.SEPARATE,
            count
            ) ;
        }
    
    /**
     * Public constructor.
     * 
     */
    public HsqlMatcherImpl(final IndexingShape indexing, double zoneheight)
        {
        this.indexing = indexing;
        this.zoneheight = zoneheight ;
        this.init();
        }

    /**
     * Indexing shape.
     * 
     */
    public enum IndexingShape
        {
        SEPARATE(),
        COMBINED(),
        COMPLEX();
        };

    /**
     * The {@link IndexingShape} for this {@link Matcher}.
     * 
     */
    private IndexingShape indexing ;

    @Override
    public IndexingShape indexing()
        {
        return this.indexing;
        }

    /**
     * Height of each zone slice.
     * 
     */
    private double zoneheight ;

    @Override
    public double height()
        {
        return this.zoneheight;
        }
    
    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final Double epsilon = 10E-6;
    
    /**
     * The matcher database type.
     * 
     */
    protected String databasetype = "mem";
    protected String databasetype()
        {
        return this.databasetype.trim();
        }

    /**
     * The matcher database host.
     * 
     */
    protected String databasehost = "localhost";
    protected String databasehost()
        {
        return this.databasehost.trim();
        }

    /**
     * The matcher database port.
     * 
     */
    protected String databaseport = "9001" ;
    protected String databaseport()
        {
        return this.databaseport.trim();
        }
    
    /**
     * The matcher database name.
     * 
     */
    protected String databasename = "zonematch";
    protected String databasename()
        {
        return this.databasename.trim();
        }

    /**
     * The matcher database user name.
     * 
    @Value("${databaseuser:}")
    protected String databaseuser;
    protected String databaseuser()
        {
        return this.databaseuser.trim();
        }
     */

    /**
     * The matcher database password.
     * 
    @Value("${databasepass:}")
    protected String databasepass;
    protected String databasepass()
        {
        return this.databasepass.trim();
        }
     */

    /**
     * Generate our database connection url.
     * 
     */
    public String url()
        {
        final StringBuilder builder = new StringBuilder(
            "jdbc:hsqldb"
            ); 

        if ("mem".equals(this.databasetype()))
            {
            builder.append(":mem:");
            builder.append(this.databasename());
            }
        else {
            log.error("Unknown database type [{}]",
                this.databasetype()
                );
            throw new UnsupportedOperationException(
                "Unknown database type [" + this.databasetype() + "]"
                );
            }
        log.trace("url() [{}]", builder.toString());
        return builder.toString();
        }

    /**
     * Our JDBC {@link DataSource}.
     *
     */
    private DataSource source ;

    /**
     * Our JDBC {@link Driver}.
     *
     */
    protected Driver driver()
        {
        return new org.hsqldb.jdbc.JDBCDriver();
        }

    /**
     * Connect our {@link DataSource}.
     * 
     */
    protected DataSource source()
        {
        log.trace("source()");
        log.trace(" databasehost [{}]", databasehost());
        log.trace(" databaseport [{}]", databaseport());
        log.trace(" databasename [{}]", databasename());
        //log.debug(" databaseuser [{}]", databaseuser());
        //log.debug(" databasepass [{}]", databasepass());
        if (null == this.source)
            {
            this.source = new SimpleDriverDataSource(
                this.driver(),
                this.url()//,
                //this.databaseuser(),
                //this.databasepass()
                );            
            }
        return this.source;
        }

    /**
     * Our database connection.
     * 
     */
    private Connection connection ;

    /**
     * Connect our {@link DataSource}.
     * @throws SQLException 
     * 
     */
    protected Connection connect()
    throws SQLException
        {
        if (null == this.connection)
            {
            this.connection = this.source().getConnection();
            }
        return this.connection; 
        }

    /**
     * Initialise our database connection.
     * @throws SQLException 
     * 
     */
    public void init()
        {
        try {
            this.connect();

            this.connection.createStatement().executeUpdate(
                    "DROP TABLE zones IF EXISTS"
                    );
            this.connection.createStatement().executeUpdate(
                "CREATE TABLE zones ("
                + "zone INT NOT NULL, "
                + "ra  DOUBLE NOT NULL, "
                + "dec DOUBLE NOT NULL, "
                + "cx  DOUBLE NOT NULL, "
                + "cy  DOUBLE NOT NULL, "
                + "cz  DOUBLE NOT NULL  "
                + ")"
                );
            switch (this.indexing)
                {
                case SEPARATE:
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX zoneindex "
                        + " ON zones ("
                        + "    zone"
                        + ")"
                        );
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX raindex "
                        + " ON zones ("
                        + "    ra"
                        + ")"
                        );
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX decindex"
                        + " ON zones ("
                        + "    dec"
                        + ")"
                        );
                    break ;

                case COMBINED:
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX zoneindex "
                        + " ON zones ("
                        + "    zone"
                        + ")"
                        );
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX radecindex"
                        + " ON zones ("
                        + "    ra,"
                        + "    dec"
                        + ")"
                        );
                    break ;

                case COMPLEX:
                    this.connection.createStatement().executeUpdate(
                        "CREATE INDEX complexindex"
                        + " ON zones ("
                        + "    zone,"
                        + "    ra,"
                        + "    dec"
                        + ")"
                        );
                break ;
                    
                default :
                    throw new IllegalArgumentException(
                        "Unknown indexing shape [{" + this.indexing.name() + "}]"
                        ); 
                }
            }
        catch (final SQLException ouch)
            {
            log.error("SQLException [{}]", ouch);
            }
        }

    /**
     * Shutdown our database connection.
     * @throws SQLException 
     * 
     */
    public void done()
    throws SQLException
        {
        if (this.connection != null)
            {
            this.connection.close();
            }
        this.connection = null ;
        }

    @Override
    public Iterable<Position> matches(final Position target, final Double radius)
        {
        //log.debug("preparing");

        //log.debug("radius [{}]", radius);
        //log.debug("height [{}]", zoneheight);

        //log.debug("ra  [{}]", target.ra());
        //log.debug("dec [{}]", target.dec());

        //log.debug("cx  [{}]", target.cx());
        //log.debug("cy  [{}]", target.cy());
        //log.debug("cz  [{}]", target.cz());

        final String template = "SELECT "
            + "    zone, "
            + "    ra, "
            + "    dec,"
            + "    cx, "
            + "    cy, "
            + "    cz  "
            + " FROM "
            + "    zones "
            + " WHERE "
            + "    zone BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            + " AND "
            + "    ra BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            + " AND "
            + "    dec BETWEEN "
            + "        ? "
            + "    AND "
            + "        ? "
            + "    AND  "
            + "        ? > (power((cx - ?), 2) + power((cy - ?), 2) + power(cz - ?, 2)) ";

        final int minzone = (int) FastMath.floor(((target.dec() + 90) - radius) / this.zoneheight) ;
        final int maxzone = (int) FastMath.floor(((target.dec() + 90) + radius) / this.zoneheight) ;
        
        double minra = target.ra() - (radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon));
        double maxra = target.ra() + (radius / (FastMath.abs(FastMath.cos(FastMath.toRadians(target.dec()))) + epsilon));

        double mindec = target.dec() - radius ; 
        double maxdec = target.dec() + radius ; 

        double squaresin = 4 * (
                FastMath.pow(
                    FastMath.sin(
                        FastMath.toRadians(
                            radius
                            )/2
                        ),
                    2)
                );

        //log.debug("min/max zone [{}][{}]", minzone, maxzone);
        //log.debug("min/max ra   [{}][{}]", minra, maxra);
        //log.debug("min/max dec  [{}][{}]", mindec, maxdec);
        
        final List<Position> list = new ArrayList<Position>();
        try {
            //log.debug("preparing");
            final PreparedStatement statement = connection.prepareStatement(template);

            //log.debug("setting");
            statement.setInt(1, minzone);
            statement.setInt(2, maxzone);            
            
            statement.setDouble(3, minra);
            statement.setDouble(4, maxra);

            statement.setDouble(5, mindec);
            statement.setDouble(6, maxdec);

            statement.setDouble(7, squaresin);            

            statement.setDouble(8,  target.cx());            
            statement.setDouble(9,  target.cy());            
            statement.setDouble(10, target.cz());            
            
            //log.debug("executing");
            final ResultSet resultset = statement.executeQuery();
            while (resultset.next())
                {
                list.add(
                    new PositionImpl(
                        resultset.getDouble(2),                        
                        resultset.getDouble(3),                        
                        resultset.getDouble(4),                        
                        resultset.getDouble(5),                        
                        resultset.getDouble(6)                        
                        )
                    );
                }
            }
        catch (SQLException ouch)
            {
            log.error("SQLException [{}]", ouch);
            }
        //log.debug("done");
        return list;
        }

    @Override
    public void insert(Position position)
        {
        //log.trace("insert() [{}][{}]", position.ra(), position.dec());
        String template = "INSERT INTO "
            + "    zones ( "
            + "        zone, "
            + "        ra, "
            + "        dec, "
            + "        cx, "
            + "        cy, "
            + "        cz "
            + "        ) "
            + "    VALUES( "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?, "
            + "        ?"
            + "        ) ";

        final Integer zone = (int) FastMath.floor((position.dec() + 90) / this.zoneheight);

        try {
            final PreparedStatement statement = connection.prepareStatement(template);
            statement.setInt(1, zone);
            statement.setDouble(2, position.ra());
            statement.setDouble(3, position.dec());
            statement.setDouble(4, position.cx());
            statement.setDouble(5, position.cy());
            statement.setDouble(6, position.cz());
            statement.execute();
            total++;
            }
        catch (SQLException ouch)
            {
            log.error("SQLException during insert [{}]", ouch);
            }
        }

    private long total = 0;

    @Override
    public long total()
        {
        return total;
        }

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
        builder.append("Total rows [");
        builder.append(String.format("%,d", this.total()));
        builder.append("] ");
        builder.append("Zone height [");
        builder.append(this.zoneheight);
        builder.append("] ");
        builder.append("URL [");
        builder.append(this.url());
        builder.append("]");
        return builder.toString();
        }
    }
