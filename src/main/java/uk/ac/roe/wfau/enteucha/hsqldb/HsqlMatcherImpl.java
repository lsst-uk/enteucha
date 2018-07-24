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
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import lombok.extern.slf4j.Slf4j;
import uk.ac.roe.wfau.enteucha.api.Position;
import uk.ac.roe.wfau.enteucha.api.Position.Matcher;
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
        this.height = 180.0 / (double) count ;
        }

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
        //log.debug("url()");
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
        //log.debug("url() [{}]", builder.toString());
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
        //log.debug("source()");
        //log.debug(" databasehost [{}]", databasehost());
        //log.debug(" databaseport [{}]", databaseport());
        //log.debug(" databasename [{}]", databasename());
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
        //log.debug("source [{}]", this.source);
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
        //log.debug("connect()");
        if (null == this.connection)
            {
            this.connection = this.source().getConnection();
            }

        //log.debug("source [{}]",     this.source);
        //log.debug("connection [{}]", this.connection);
        return this.connection; 
        }

    /**
     * Initialise our database connection.
     * @throws SQLException 
     * 
     */
    public void init()
        {
        //log.debug("init");
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

            this.connection.createStatement().executeUpdate(
                "CREATE INDEX zoneidx "
                + "ON zones ("
                + "    zone"
                + ")"
                );

            this.connection.createStatement().executeUpdate(
                "CREATE INDEX zoneradidx "
                + "ON zones ("
                + "    zone,"
                + "    ra,"
                + "    dec"
                + ")"
                );

            this.connection.createStatement().executeUpdate(
                "CREATE INDEX radidx "
                + "ON zones ("
                + "    ra,"
                + "    dec"
                + ")"
                );

            this.connection.createStatement().executeUpdate(
                "CREATE INDEX raidx "
                + "ON zones ("
                + "    ra"
                + ")"
                );

            this.connection.createStatement().executeUpdate(
                "CREATE INDEX decidx "
                + "ON zones ("
                + "    dec"
                + ")"
                );

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
        log.debug("done");
        if (this.connection != null)
            {
            this.connection.close();
            }
        this.connection = null ;
        }

    /**
     * Small offset to avoid divide by zero.
     * 
     */
    protected static final Double epsilon = 10E-6;

    /**
     * Height of each zone slice.
     * 
     */
    private Double height ;
    
    @Override
    public Iterable<Position> matches(final Position target, final Double radius)
        {
        log.debug("preparing");

        log.debug("radius [{}]", radius);
        log.debug("height [{}]", height);

        log.debug("ra  [{}]", target.ra());
        log.debug("dec [{}]", target.dec());

        log.debug("cx  [{}]", target.cx());
        log.debug("cy  [{}]", target.cy());
        log.debug("cz  [{}]", target.cz());

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

        final Integer minzone = (int) Math.floor(((target.dec() + 90) - radius) / this.height) ;
        final Integer maxzone = (int) Math.floor(((target.dec() + 90) + radius) / this.height) ;
        
        double minra = (target.ra() - radius) / (Math.cos(Math.toRadians(Math.abs(target.dec()))) + epsilon);
        double maxra = (target.ra() + radius) / (Math.cos(Math.toRadians(Math.abs(target.dec()))) + epsilon);

        double mindec = (target.dec() - radius) ; 
        double maxdec = (target.dec() + radius) ; 

        double squaresin = 4 * (
                Math.pow(
                    Math.sin(
                        Math.toRadians(
                            radius
                            )/2
                        ),
                    2)
                );
        
        log.debug("querying");
        final List<Position> list = new ArrayList<Position>();
        try {
            final PreparedStatement statement = connection.prepareStatement(template);

            statement.setDouble(1, minzone);
            statement.setDouble(2, maxzone);            
            
            statement.setDouble(3, minra);
            statement.setDouble(4, maxra);

            statement.setDouble(5, mindec);
            statement.setDouble(6, maxdec);

            statement.setDouble(7, squaresin);            

            statement.setDouble(8,  target.cx());            
            statement.setDouble(9,  target.cy());            
            statement.setDouble(10, target.cz());            
            
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
        log.debug("done");
        return list;
        }

    @Override
    public void insert(Position position)
        {
        log.trace("insert() [{}][{}]", position.ra(), position.dec());
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

        final Integer zone = (int) Math.floor((position.dec() + 90) / this.height);

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

    public Iterable<Position> verify()
        {
        log.debug("preparing");

        String query = "SELECT "
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
            + "        :zonemin "
            + "    AND "
            + "        :zonemax ";
                
        final NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(
            this.source()
            );

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("zonemin",     0.0);
        params.addValue("zonemax",  1000.0);
        
        log.debug("querying");
        final SqlRowSet rowset = template.queryForRowSet(
            query,
            params
            );
        final List<Position> list = new ArrayList<Position>();
        while (rowset.next())
            {
            list.add(
                new PositionImpl(
                    rowset.getDouble(2),                        
                    rowset.getDouble(3),                        
                    rowset.getDouble(4),                        
                    rowset.getDouble(5),                        
                    rowset.getDouble(6)                        
                    )
                );
            }
        log.debug("done");
        return list;
        }

    private long total = 0;

    @Override
    public long total()
        {
        return total;
        }
    }
