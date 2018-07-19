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
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.JDBCType;

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
        log.debug("url()");
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
        log.debug("url() [{}]", builder.toString());
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
        log.debug("source()");
        log.debug(" databasehost [{}]", databasehost());
        log.debug(" databaseport [{}]", databaseport());
        log.debug(" databasename [{}]", databasename());
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
        log.debug("source [{}]", this.source);
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
        log.debug("connect()");
        if (null == this.connection)
            {
            this.connection = this.source().getConnection();
            }

        log.debug("source [{}]",     this.source);
        log.debug("connection [{}]", this.connection);
        return this.connection; 
        }

    /**
     * Initialise our database connection.
     * @throws SQLException 
     * 
     */
    public void init()
        {
        log.debug("init");
        try {
            this.connect();
    
            final Statement statement = this.connection.createStatement();
            statement.executeUpdate(
                "CREATE TABLE zones ("
                + "zone INT NOT NULL, "
                + "ra  DOUBLE NOT NULL, "
                + "dec DOUBLE NOT NULL, "
                + "cx  DOUBLE NOT NULL, "
                + "cy  DOUBLE NOT NULL, "
                + "cz  DOUBLE NOT NULL  "
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
            + "        floor((:dec + 90 - :radius) / CONVERT(:height, SQL_DOUBLE)) "
            + "    AND "
            + "        floor((:dec + 90 + :radius) / CONVERT(:height, SQL_DOUBLE)) "
            + " AND "
            + "    ra BETWEEN "
            + "        (:ra - CONVERT(:radius, SQL_DOUBLE))/(cos(radians(abs(:dec))) + CONVERT(:epsilon, SQL_DOUBLE)) "
            + "    AND "
            + "        (:ra + CONVERT(:radius, SQL_DOUBLE))/(cos(radians(abs(:dec))) + CONVERT(:epsilon, SQL_DOUBLE)) "
            + " AND "
            + "    dec BETWEEN "
            + "        :dec - CONVERT(:radius, SQL_DOUBLE) "
            + "    AND "
            + "        :dec + CONVERT(:radius, SQL_DOUBLE) "
            + "    AND  "
            + "        (4 * power(sin(radians(CONVERT(:radius, SQL_DOUBLE) / 2)),2)) > (power((cx - :cxx), 2) + power((cy - :cyy), 2) + power(cz - :czz, 2)) ";
                
        final NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(
            this.source
            );

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ra",  target.ra(), JDBCType.DOUBLE.ordinal());
        params.addValue("dec", target.dec(),JDBCType.DOUBLE.ordinal());
        params.addValue("cxx",  target.cx(), JDBCType.DOUBLE.ordinal());
        params.addValue("cyy",  target.cy(), JDBCType.DOUBLE.ordinal());
        params.addValue("czz",  target.cz(), JDBCType.DOUBLE.ordinal());

        params.addValue("radius",  radius,  JDBCType.DOUBLE.ordinal());
        params.addValue("height",  height,  JDBCType.DOUBLE.ordinal());

        params.addValue("epsilon", epsilon, JDBCType.DOUBLE.ordinal());
        
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

    @Override
    public void insert(Position position)
        {
        log.debug("preparing");
        String query = "INSERT INTO "
            + "    zones ( "
            + "        zone, "
            + "        ra, "
            + "        dec, "
            + "        cx, "
            + "        cy, "
            + "        cz "
            + "        ) "
            + "    VALUES( "
            + "        :zone, "
            + "        :ra, "
            + "        :dec, "
            + "        :cx, "
            + "        :cy, "
            + "        :cz "
            + "        ) ";

        log.debug("source [{}]", this.source);

        final NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(
            this.source
            );

        int zone = (int) Math.floor((position.dec() + 90) / this.height);
        
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("zone", zone);
        params.addValue("ra",  position.ra());
        params.addValue("dec", position.dec());
        params.addValue("cx",  position.cx());
        params.addValue("cy",  position.cy());
        params.addValue("cz",  position.cz());
            
        log.debug("inserting");
        template.update(query, params);
        log.debug("done");
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
    }
