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

package uk.ac.roe.wfau.enteucha.webapp;

import org.springframework.http.MediaType;

/**
 * ModelViewController service paths and parameter names. 
 * 
 */
public interface WebappModel
    {
    /**
     * HTTP content type for JSON.
     * 
     */
    public static final String JSON_MIME = MediaType.APPLICATION_JSON_VALUE ;

    /**
     * HTTP content type for URL encoded form fields.
     * 
     */
    public static final String FORM_MIME = MediaType.APPLICATION_FORM_URLENCODED_VALUE;

    /**
     * MVC property for the 'ra' parameter.
     *
     */
    public static final String PARAM_RA = "enteucha.pos.ra" ;

    /**
     * MVC property for the 'dec' parameter.
     *
     */
    public static final String PARAM_DEC = "enteucha.pos.dec" ;

    /**
     * MVC property for the 'position' parameter.
     *
     */
    public static final String PARAM_POINT = "enteucha.pos.point" ;

    /**
     * MVC property for the 'radius' parameter.
     *
     */
    public static final String PARAM_RADIUS = "enteucha.pos.radius" ;

    /**
     * MVC property for the 'circle' parameter.
     *
     */
    public static final String PARAM_CIRCLE = "enteucha.pos.circle" ;


    }
