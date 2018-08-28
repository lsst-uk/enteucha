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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * 
 */
public interface MatcherService
extends MatcherModel
    {

    @SuppressWarnings("serial")
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public static class MatcherServiceException
    extends Exception
        {
        public MatcherServiceException(Exception ouch)
            {
            super(ouch);
            }
        }
    
    /**
     * Find matching {Position}s within a target circle.
     * @param target The target circle.
     * @return A list of {@link {Position}s found inside the target circle.
     * @throws MatcherServiceException
     * 
     */
    @RequestMapping(params={PARAM_CIRCLE}, method=RequestMethod.POST, produces=JSON_MIME)
    public ResponseEntity<Iterable<Position>> match(
        @RequestParam(value=PARAM_CIRCLE, required=true)
        final Double[] target
        ) throws MatcherServiceException;

    /**
     * Find matching {Position}s within a target circle.
     * @param target The target position.
     * @param radius The match radius.
     * @return A list of {@link {Position}s found within the radius of the target position.
     * @throws MatcherServiceException
     * 
     */
    @RequestMapping(params={PARAM_POINT, PARAM_RADIUS}, method=RequestMethod.POST, produces=JSON_MIME)
    public ResponseEntity<Iterable<Position>> match(
        @RequestParam(value=PARAM_POINT, required=true)
        final Double[] target,
        @RequestParam(value=PARAM_RADIUS, required=true)
        final Double   radius
        ) throws MatcherServiceException;

    /**
     * Find matching {Position}s within a target circle.
     * @param ra  The target right ascension.
     * @param dec The target declination.
     * @param radius The match radius.
     * @return A list of {@link {Position}s found inside the target.
     * @throws MatcherServiceException
     * 
     */
    @RequestMapping(params={PARAM_RA, PARAM_DEC, PARAM_RADIUS}, method=RequestMethod.POST, produces=JSON_MIME)
    public ResponseEntity<Iterable<Position>> match(
        @RequestParam(value=PARAM_RA, required=true)
        final Double ra,
        @RequestParam(value=PARAM_DEC, required=true)
        final Double dec,
        @RequestParam(value=PARAM_RADIUS, required=true)
        final Double radius
        ) throws MatcherServiceException;
    
    }
