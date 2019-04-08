/*
 * 2019 Kevin Walls
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
