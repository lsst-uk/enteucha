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

package uk.ac.roe.wfau.enteucha.dataset;

import lombok.extern.slf4j.Slf4j;

/**
 * A CQEngine based implementation of {@link Position}
 * 
 */
@Slf4j
public class PositionImpl implements Position
    {

    /**
     * Protected constructor.
     *  
     */
    protected PositionImpl(final Position position)
        {
        this(
            position.ra(),
            position.dec()
            );
        }
    
    /**
     * Protected constructor.
     *  
     */
    protected PositionImpl(final Double ra, final Double dec)
        {
        this.ra  = ra ;
        this.dec = dec;
        }

    private Double ra;
    @Override
    public Double ra()
        {
        return this.ra;
        }

    private Double dec;
    @Override
    public Double dec()
        {
        return this.dec;
        }

    private Double cx;
    @Override
    public Double cx()
        {
        return this.cx;
        }

    private Double cy;
    @Override
    public Double cy()
        {
        return this.cy;
        }

    private Double cz;
    @Override
    public Double cz()
        {
        return this.cz;
        }
    }
