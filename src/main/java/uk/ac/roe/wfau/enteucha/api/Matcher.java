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

/**
 * Public interface for a {@link Position} matcher. 
 * 
 */
public interface Matcher
    {

    /**
     * Public interface for a {@link Matcher} factory. 
     * 
     */
    public interface Factory
        {
        /**
         * Create a new {@link Matcher}.
         *
         */
        public Matcher create(double zoneheight);

        }

    /**
     * Initialise the {@link Matcher}.
     * 
     */
    public void init();
        
    /**
     * Match {@link Position}s within a radius around a target {@link Position}.
     * 
     */
    public Iterable<Position> matches(final Position target, final Double radius);
        
    /**
     * Insert a {@link Position} into the {@link Matcher}.
     * 
     */
    public void insert(final Position position);

    /**
     * Get the total number of positions in this {@link Matcher}. 
     * 
     */
    public long total();

    /**
     * Get the zone height for this {@link Matcher}. 
     * 
     */
    public double height();
    
    /**
     * Describe the {@link Matcher} configuration.
     * 
     */
    public String info();

    /**
     * Get the indexing shape for this {@link Matcher}.
     * 
     */
    public Enum<?> indexing();
    
    }
