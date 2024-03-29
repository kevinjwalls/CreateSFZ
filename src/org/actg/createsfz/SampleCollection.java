/**
 * Copyright (C) 2019, 2022, Kevin Walls
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.actg.createsfz.CreateSFZ.Format;

/**
 * A SampleCollection is a sorted collection of filenames, created from a
 * directory.
 *
 * Use patterns in filenames to recognise notes, categorising the samples based
 * on:
 *
 * MIDI note Velocity Sample number (handle multiple samples per note/velocity).
 */
public class SampleCollection {

    protected String sampleDirName;
    protected String sampleGroupBaseName;
    protected String outputFilename;
    protected Format format;
    protected List<String> filesUsed;

    // Map a note Number to a List of samples of increasing velocity/loudness:
    protected Map<Integer, Set> samples;
    // Release Triggers, effectively another layer...
    protected Map<Integer, Set> samplesReleaseTriggers;

    public static String HEADER = "//\n// SFZ file created by CreateSFZ.\n//";
    public static String FOOTER = "//\n// End of SFZ file created by CreateSFZ.\n//";

    protected static String REGEX_NOTENAME_GROUP = "([a-z]#?)";

    /**
     * Create a SampleCollection from a named directory.
     *
     * If the given Format is null, probe for a suitable format to recognise
     * files in the directory.
     *
     * @param format
     * @param dirname
     * @throws IOException
     */
    public SampleCollection(Format format, String dirname, String filenameFilter) throws IOException {
        this.sampleDirName = dirname;
        File dir = new File(dirname);
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            throw new IOException("bad directory: " + dirname);
        }
        if (format != null) {
            this.format = format;
        } else {
            this.format = probeFormat(dir);
        }
        samples = new HashMap<Integer, Set>();
        samplesReleaseTriggers = new HashMap<Integer, Set>();
        filesUsed = addFiles(dir, filenameFilter, format);
        System.out.println(dirname + ": files used: " + filesUsed.size());
    }

    /**
     * Create a SampleCollection from specific files, with a given root note
     * number.
     *
     * Presumes the sample names in the List are not release triggers.
     *
     * @param format
     * @param sampleNames
     * @param rootNote
     */
    public SampleCollection(Format format, List<String> sampleNames, int rootNote) {
        this.format = format;
        samples = new HashMap<Integer, Set>();
        int note = rootNote;
        for (String s : sampleNames) {
            addSample(new Sample(s, note++, 0 /* velocity */, 1), false);
        }
    }

    public Format probeFormat(File dir) {
        throw new RuntimeException("missing sample name format");
    }

    /**
     * Scan a directory of Files and add sample files to this collection.
     *
     * Use the base name of samples to set out output .sfz filename, throwing an
     * IOException if it already exists.
     *
     * @param dir
     * @param format
     * @return
     * @throws IOException
     */
    public List<String> addFiles(File dir, String filenameFilter, Format format) throws IOException {
        List<String> filesUsed = new LinkedList<>();
        List<String> filesNotUsed = new LinkedList<>();
        Pattern pat_filename = Pattern.compile(format.filenameRegex());
        for (File f : dir.listFiles()) {
            String filename = f.getName();
            if (filenameFilter != null && !filename.contains(filenameFilter)) {
                filesNotUsed.add(f.getName());
                continue; // Skip a file that does not contain the given filenameFilter.
            }
            Matcher m = pat_filename.matcher(filename);
            if (m.find()) {
                // e.g. Name_Hard-C4-1.wav
                // "(.*)_(.*)\\-()\\-(\\d+)\\.wav";
                // "baseName_velocity-NOTE-variation"
                String thisBaseName = m.group(format.getBaseNameGroup());
                if (sampleGroupBaseName == null) {
                    // Use first recognised Sample to set a sampleBaseName:
                    sampleGroupBaseName = thisBaseName;
                    outputFilename = sampleGroupBaseName + ".sfz";
                } else {
                    if (!sampleGroupBaseName.equals(thisBaseName)) {
                        if (!thisBaseName.startsWith(sampleGroupBaseName)) {
                            System.err.println("Note: sample base name: " + sampleGroupBaseName
                                    + ": ignoring sample file with different base name: " + thisBaseName + ": " + f);
                            continue;
                        }
                    }
                }
                String noteName = m.group(format.getNoteNameGroup());
                boolean isReleaseTrigger = false;
                if (format.getReleaseTriggerGroup() >= 0) {
                    String rt = m.group(format.getReleaseTriggerGroup());
                    if (rt != null) {
                        isReleaseTrigger = true;
                    }
                }
                int velocity = -1;
                if (format.getVelocityGroup() > 0) {
                    String velocityName = m.group(format.getVelocityGroup());
                    // Check as release trigger may not have have velocity:
                    if (velocityName != null && !velocityName.isEmpty()) {
                        velocityName = velocityName.trim();
                        velocity = parseVelocityName(velocityName);
                    }
                }
                // Decode note:
                int noteNumber = -1;
                try {
                    noteName = noteName.trim();
                    noteNumber = MIDI.noteNameToNumber(noteName);
                } catch (NumberFormatException nfe) {
                    System.err.println("Skipping: '" + f + " with noteName='" + noteName + "', due to: " + nfe);
                    continue;
                }
                // Decode optional variation number:
                int variationNumber = -1;
                if (format.getVariationNumberGroup() > 0) {
                    String variation = m.group(format.getVariationNumberGroup());
                    if (variation != null) {
                        try {
                            variationNumber = Integer.parseInt(variation);
                        } catch (NumberFormatException nfe) {
                            System.err.println("Skipping: '" + f + " with noteName='" + noteName + "' and variation#='" + variation
                                    + "', due to: " + nfe);
                            continue;
                        }
                    }
                }
                addSample(new Sample(f.getName(), noteNumber, velocity, variationNumber), isReleaseTrigger);
                filesUsed.add(f.getName());
            } else {
                filesNotUsed.add(f.getName());
            }
        }
        return filesUsed;
    }

    /**
     * Add a Sample to this SampleCollection.
     *
     * @param s
     * @param isReleaseTrigger
     */
    protected synchronized void addSample(Sample s, boolean isReleaseTrigger) {
        if (!isReleaseTrigger) {
            addSample(samples, s);
        } else {
            addSample(samplesReleaseTriggers, s);
        }
    }

    /**
     * Add a Sample to this SampleCollection.
     *
     * Place the Sample in a Set of Samples, in a Map of note numbers to those
     * Sets of Samples.
     *
     * @param samples
     * @param s
     */
    protected synchronized void addSample(Map<Integer, Set> samples, Sample s) {
        Set<Sample> set = samples.get(s.noteNumber);
        if (set == null) {
            set = new TreeSet<>();
            samples.put(s.noteNumber, set);
        }
        set.add(s);
    }

    /**
     * Return an int for the velocity. Not a full-MIDI range velocity, just an
     * int so we can sort samples.
     *
     * @param velocityName
     * @return int velocity value
     */
    public int parseVelocityName(String velocityName) {
        int velocity = format.velocities().indexOf(velocityName);
        if (velocity == -1) {
            velocity = 1;
        }
        return velocity;
    }

    /**
     * Export all the samples in SFZ format: i.e. print as plaintext.
     */
    public void printRegions(Map<Integer, Set> samples, int rangeLow, int rangeHigh, boolean releaseTriggers,
            int level, PrintStream out) {

        // Get the note names, sorted:
        Set<Integer> notes = new TreeSet<>();
        notes.addAll(samples.keySet());
        // Iterate the notes, retrieve a Set of samples for each note.
        int noteCount = 0;
        int prevKey = -1;
        out.println("// " + notes);
        for (Integer note : notes) {
            out.println("\n// Note: " + note);
            Set<Sample> set = samples.get(note);
            // Some global info for this note, need to use the first Sample in the set for note value etc...
            Sample s1 = set.iterator().next();
            out.println("<global>");
            out.println("pitch_keycenter=" + s1.noteNumber);
            // Expand key range downwards, to the note after the previous note seen,
            // or by KEY_RANGE on first iteration:
            int lokey = s1.noteNumber;
            if (noteCount == 0) {
                lokey = lokey - rangeLow;
            } else {
                lokey = prevKey + 1; // ...which can equal s1.noteNumber
            }
            out.println("lokey=" + lokey);
            out.println("hikey=" + s1.noteNumber);
            // The sample Set sorts samples for that note, from soft to hard.
            // Get the velocity range for these samples:
            int velocities = countVelocities(set);  // Redundant: samplesByVelocity.size() should equal velocities.
            List<String> velocityStrings = getVelocityRanges(velocities);
            System.out.println("velocities: " + velocities + " : " + velocityStrings);
            List<Set<Sample>> samplesByVelocity = splitByVelocity(set);
            for (Set<Sample> setPerVelocity : samplesByVelocity) {
                // How many samples for that note (and velocity): round-robin sequence.
                int seq = 1;
                for (Sample s : setPerVelocity) {
                    if (seq == 1) {
                        String velocityInfo = "";
                        if (s.velocity >= 0) {
                            velocityInfo = velocityStrings.get(Math.min(s.velocity, velocityStrings.size() - 1));
                        }
                        out.println("<group> " + velocityInfo); // e.g. lovel=55 hivel=90
                        out.println("seq_length=" + setPerVelocity.size());
                    }
                    out.println("<region>");
                    out.println("sample=" + s.filename);
                    out.println("seq_position=" + seq);
                    if (releaseTriggers) {
                        out.println("trigger=release");
                    }
                    if (level != 0) {
                        // Optional volume scale.  Used to trim level of release triggers.
                        out.println("volume=" + level);
                    }
                    out.println();
                    seq++;
                }
                out.println();
                prevKey = s1.noteNumber;
                noteCount++;
            }
            out.println("// noteCount = " + noteCount);
        }
    }

    /**
     * Split the 0-127 range of velocities, to the given number of equal parts.
     *
     * @param number
     * @return
     */
    protected List<String> getVelocityRanges(int number) {
        List<String> v = new ArrayList<String>();
        int each = 127 / number;
        for (int i = 0; i < number; i++) {
            // e.g. lovel=55 hivel=90
            int lo = i * each;
            int hi = ((i + 1) * each) - 1;
            if (i == number - 1 && hi < 127) {
                hi = 127;
            }
            v.add("lovel=" + lo + " hivel=" + hi);
        }
        return v;
    }

    /**
     * Split a Set of Samples for a note, into Sets into a List of Sets,
     * separated by different Velocities.
     *
     * @param set
     * @return
     */
    public List<Set<Sample>> splitByVelocity(Set<Sample> set) {
        List<Set<Sample>> results = new ArrayList<>();
        int lastVel = -1;
        int v = 0;
        Set<Sample> current = new TreeSet<>();
        results.add(current);
        for (Sample s : set) {
            if (lastVel < 0) {
                // First Sample:
                lastVel = s.velocity;
            } else {
                if (s.velocity != lastVel) {

                    current = new TreeSet<>();
                    results.add(current);
                    lastVel = s.velocity;
                }
            }
            current.add(s);
        }
        return results;
    }

    /**
     * Count the distinct velocity levels in a Set of Samples.
     *
     * @param set
     * @return int number of distinct velocity levels
     */
    protected int countVelocities(Set<Sample> set) {
        int v = 0;
        int lastVel = -1;
        for (Sample s : set) {
            if (lastVel < 0) {
                v = 1;
                lastVel = s.velocity;
            } else {
                if (s.velocity != lastVel) {
                    v++;
                    lastVel = s.velocity;
                }
            }
        }
        return v;
    }

    /**
     * Write this collection as a .sfz format file.
     *
     * @throws IOException
     */
    public void writeSFZ(String filename, int rangeLow, int rangeHigh, int releaseLevel) throws IOException {
        if (filename == null) {
            filename = outputFilename;
        }
        if (filename == null) {
            throw new IOException("no output file");
        }
        if (samples == null || samples.isEmpty()) {
            throw new IOException("no samples");
        }
        File outputFile = new File(filename);
        System.err.println("CreateSFZ: " + outputFile);
        PrintStream out = new PrintStream(outputFile);
        out.println(HEADER);
        out.println("// Sample filename format: " + format.toString());
        out.println("//");
        out.println("<control>");
        if (sampleDirName != null) {
            out.println("default_path=" + sampleDirName);
        }
        printRegions(samples, rangeLow, rangeHigh, false, 0, out);
        if (samplesReleaseTriggers != null) {
            printRegions(samplesReleaseTriggers, rangeLow, rangeHigh, true, releaseLevel, out);
        }
        out.println(FOOTER);
    }
}
