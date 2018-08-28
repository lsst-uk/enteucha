/*
 *  Copyright (C) 2014 Royal Observatory, University of Edinburgh, UK
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
package uk.ac.roe.wfau.enteucha.util;

import java.util.Iterator;

/**
 * Wrapper class to solve a problem with Java generics.  
 *
 */
public class GenericIterable<I, T extends I>
    implements Iterable<I>
    {

    /**
     * Public constructor.
     *
     */
    public GenericIterable(Iterable<T> iterable)
        {
        this.iterable = iterable ;
        }

    /**
     * The original Iterable<T>.
     *
     */
    private Iterable<T> iterable ;

    /**
     * Inner class to implement the Iterator<I> interface.
     *
     */
    private class GenericIterator
        implements Iterator<I>
        {

        /**
         * Private constructor.
         *
         */
        private GenericIterator(Iterator<T> iterator)
            {
            this.iterator = iterator ;
            }

        /**
         * Our source iterator.
         *
         */
        private Iterator<T> iterator ;

        @Override
        public boolean hasNext()
            {
            return iterator.hasNext();
            }

        @Override
        public I next()
            {
            return iterator.next();
            }

        @Override
        public void remove()
            {
            iterator.remove();
            }
        }

    @Override
    public Iterator<I> iterator()
        {
        return new GenericIterator(
            this.iterable.iterator()
            );
        }

    /**
     * Bean style method to support JSTL forEach.
     * http://stackoverflow.com/questions/2978536/jstl-using-foreach-to-iterate-over-a-user-defined-class
     * http://stackoverflow.com/questions/6212622/how-can-i-work-with-iterables-in-my-jsp-pages
     *
     */
    public Iterator<I> getIter()
        {
        return iterator();
        }
    }
