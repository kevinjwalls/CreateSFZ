/**
 * Copyright (C) 2019 Kevin Walls
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.actg.createsfz;

/**
 * Sample information such as filename, the note and velocity it represents.
 */
public class Sample implements Comparable {

    protected String filename;
    protected int noteNumber;
    protected int velocity;
    protected int id;

    public Sample(String filename, String basename, int noteNumber, int velocity, int id) {
        this.filename = filename;
        this.noteNumber = noteNumber;
        this.velocity = velocity;
        this.id = id;
    }

    @Override
    public int compareTo(Object o) {
        Sample other = (Sample) o;
        int result = noteNumber - other.noteNumber;
        if (result == 0) {
            result = velocity - other.velocity;
        }
        if (result == 0) {
            result = id - other.id;
        }
        return result;
    }

}
