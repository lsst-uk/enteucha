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
            + "        floor(({dec} + 90 - {radius}) / {height}) "
            + "    AND "
            + "        floor(({dec} + 90 + {radius}) / {height}) "
            + " AND "
            + "    ra BETWEEN "
            + "        ({ra} - {radius})/(cos(radians(abs({dec}))) + {epsilon}) "
            + "    AND "
            + "        ({ra} + {radius})/(cos(radians(abs({dec}))) + {epsilon}) "
            + " AND "
            + "    dec BETWEEN "
            + "        {dec} - {radius} "
            + "    AND "
            + "        {dec} + {radius} "
            + "    AND  "
            + "        (4 * power(sin(radians({radius} / 2)),2)) > (power((cx - {cx}), 2) + power((cy - {cy}), 2) + power(cz - {cz}, 2)) ";

        final String query = template.replace(
            "{ra}",   String.format("%e", target.ra()).toString()
            ).replace(
            "{dec}",  String.format("%e", target.dec()).toString()
            ).replace(
            "{cx}",  String.format("%e", target.cx()).toString()
            ).replace(
            "{cy}",  String.format("%e", target.cy()).toString()
            ).replace(
            "{cz}",  String.format("%e", target.cz()).toString()
            ).replace(
            "{radius}",  String.format("%e", radius).toString()
            ).replace(
            "{height}",  String.format("%e", height).toString()
            ).replace(
            "{epsilon}",  String.format("%e", epsilon).toString()
            );

        log.debug("querying");
        final List<Position> list = new ArrayList<Position>();
        try {
            final Statement statement = connection.createStatement();
            final ResultSet resultset = statement.executeQuery(query);
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
        //log.debug("preparing");
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
            + "        {zone}, "
            + "        {ra}, "
            + "        {dec}, "
            + "        {cx}, "
            + "        {cy}, "
            + "        {cz} "
            + "        ) ";

        final Integer zone = (int) Math.floor((position.dec() + 90) / this.height);

        final String query = template.replace(
            "{zone}", String.format("%d", zone).toString()
            ).replace(
            "{ra}",   String.format("%e", position.ra()).toString()
            ).replace(
            "{dec}",  String.format("%e", position.dec()).toString()
            ).replace(
            "{cx}",  String.format("%e", position.cx()).toString()
            ).replace(
            "{cy}",  String.format("%e", position.cy()).toString()
            ).replace(
            "{cz}",  String.format("%e", position.cz()).toString()
            );

        //log.debug("inserting");
        try {
            connection.createStatement().executeQuery(query);
            total++;
            }
        catch (SQLException ouch)
            {
            log.error("SQLException during insert [{}]", ouch);
            }
        //log.debug("done");
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
