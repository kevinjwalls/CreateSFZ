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

import org.actg.createsfz.MIDI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test MIDI note conversions.
 */
public class TestMIDI {

    public TestMIDI() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMIDINamesToNumbers() {
        Assert.assertEquals(0, MIDI.noteNameToNumber("C-2"));
        Assert.assertEquals(12, MIDI.noteNameToNumber("C-1"));
        Assert.assertEquals(24, MIDI.noteNameToNumber("C0"));
        Assert.assertEquals(36, MIDI.noteNameToNumber("C1"));
        Assert.assertEquals(48, MIDI.noteNameToNumber("C2"));

        Assert.assertEquals(60, MIDI.noteNameToNumber("C3")); // middle C
        Assert.assertEquals(61, MIDI.noteNameToNumber("C#3"));
        Assert.assertEquals(62, MIDI.noteNameToNumber("D3"));
        Assert.assertEquals(63, MIDI.noteNameToNumber("D#3"));
        Assert.assertEquals(64, MIDI.noteNameToNumber("E3"));

        Assert.assertEquals(69, MIDI.noteNameToNumber("A3"));
        Assert.assertEquals(70, MIDI.noteNameToNumber("A#3"));
        Assert.assertEquals(71, MIDI.noteNameToNumber("B3"));

        Assert.assertEquals(72, MIDI.noteNameToNumber("C4"));
        Assert.assertEquals(84, MIDI.noteNameToNumber("C5"));
        Assert.assertEquals(96, MIDI.noteNameToNumber("C6"));
        Assert.assertEquals(108, MIDI.noteNameToNumber("C7"));
    }
}
