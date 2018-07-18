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

package uk.ac.roe.wfau.enteucha.crossmatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;


/**
 * Indexing service ....
 * 
 */
@Slf4j
@Controller
@RequestMapping(CrossMatchModel.CROSS_MATCH_PATH)
public class CrossMatchService
implements CrossMatchModel
    {


    @SuppressWarnings("serial")
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public static class CrossMatchException
    extends Exception
        {
        public CrossMatchException(Exception ouch)
            {
            super(ouch);
            }
        }
    
    /**
     * Public constructor.
     *   
     */
    public CrossMatchService()
        {
        log.debug("CrossMatchService() - constructor");
        }

    /**
     * Our crossmatcher.
     * 
     */
    @Autowired
    private CrossMatcher crossmatcher ;


    }
