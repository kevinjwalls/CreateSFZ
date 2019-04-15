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
 * MIDI note utilities.
 */
public class MIDI {

    public static String[] notes = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    /**
     * For a note name such as "C3", "A#4", return the MIDI note number.
     *
     * Recognise # for sharp, no flats. The number in the String is the octave
     * number, from -1 to 9.
     *
     * C-2 (C in octave number minus two) maps to note zero.
     *
     * @param name
     * @return note number
     */
    public static int noteNameToNumber(String name) {
        try {
            String noteName = null;
            int octave;
            if (name.charAt(1) == '#') {
                noteName = name.substring(0, 2);
                octave = Integer.parseInt(name.substring(2));
            } else {
                noteName = name.substring(0, 1);
                octave = Integer.parseInt(name.substring(1));
            }
            int noteNumber = findStringInArray(noteName, notes);
            return (octave + 2) * 12 + noteNumber;
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
    }

    public static int findStringInArray(String s, String[] array) {
        int pos = 0;
        for (String a : array) {
            if (s.equals(a)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }
}
