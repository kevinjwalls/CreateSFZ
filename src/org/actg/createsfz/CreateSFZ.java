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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CreateSFZ is the Main class.
 */
public class CreateSFZ {

    public static final String COPYTEXT = "CreateSFZ: Create SFZ format files from a directory of sound samples.\n"
            + "Copyright (C) 2019 Kevin Walls\n"
            + "This program comes with ABSOLUTELY NO WARRANTY.\n"
            + "This is free software, and you are welcome to redistribute it\n"
            + "under certain conditions.";

    public static final String USAGE = "java CreateSFZ [ -filter FILENAME_FILTER]  [ -format FORMAT_NAME ] [ -o OUPTUTFILE ] [ -note NOTENAME ]  FILE or DIRECTORY \n"
            + "where:\n"
            + "[ ... ] options are optional\n"
            + " -filter FILENAME_FILTER    Specifies text that must be in sample filenames\n"
            + "         DIRECTORY          is a directory name to scan entirely for samples\n"
            + "         FILE               is a single file to use";

    public static final String DEFAULT_FORMAT_NAME = "format1";

    public static final String[] KNOWN_FORMATS = new String[]{"format1", "format2", "pianobook"};

    public static final int KEY_RANGE = 24;

    public interface Format {

        public String filenameRegex();

        public String filenameExample();

        public int getBaseNameGroup();

        public int getNoteNameGroup();

        public int getVelocityGroup();

        public int getReleaseTriggerGroup();

        public int getVariationNumberGroup();

        public List<String> velocities();
    }

    /**
     * Format1: "baseName_velocityName-NoteName-VariationNumber.wav" where
     * VelocityName is Soft, Medium or Hard.
     */
    public class Format1 implements Format {

        public String filenameRegex() {
            return "(.*)_([a-zA-Z]+)\\-(.*)\\-(\\d+)\\.wav"; // "baseName_velocityName-NoteName-VariationNumber.wav"
        }

        public String filenameExample() {
            return "baseName_velocityName-NoteName-VariationNumber.wav";
        }

        public int getBaseNameGroup() {
            return 1;
        }

        public int getNoteNameGroup() {
            return 3;
        }

        public int getVelocityGroup() {
            return 2;
        }

        public int getReleaseTriggerGroup() {
            return -1;
        }

        public int getVariationNumberGroup() {
            return 4;
        }

        public List<String> velocities() {
            return Arrays.asList("Soft", "Medium", "Hard");
        }
    }

    /**
     * Very simple sample names of the format: "NAME NOTE.wav" or "NAME RT
     * NOTE.wav".
     */
    public class Format2 implements Format {

        public String filenameRegex() {
            return "(.*?) (RT )?(.*)\\.wav"; // baseName notename
        }

        public String filenameExample() {
            return "baseName notename.wav";
        }

        public int getBaseNameGroup() {
            return 1;
        }

        public int getNoteNameGroup() {
            return 3;
        }

        public int getVelocityGroup() {
            return -1;
        }

        public int getReleaseTriggerGroup() {
            return 2;
        }

        public int getVariationNumberGroup() {
            return -1;
        }

        public List<String> velocities() {
            return Arrays.asList("Soft", "Medium", "Hard");
        }
    }

    /**
     * A format for pianobook samples... e.g.
     * "1911+Bechstein+Upright+-+Andrew+Ward" with sample names such as
     * "AWBechstein mf D#5.wav" and "AWBechstein A#5 RT.wav"
     */
    public class Format_PianoBook1 implements Format {

        public String filenameRegex() {
            return "([A-Za-z]*)?\\s+([mpf]+)?\\s*?([A-Z#0-9]*?)(\\s+RT)?\\.wav"; // "baseName velocity? NoteName RT?"
            //return "([A-Za-z]*)?\\s+([mpf]+)?\\s*?([A-Z#0-9]*?)(\\s+RT)?\\.wav"; // "baseName velocity? NoteName RT?"
        }

        public String filenameExample() {
            return "baseName velocity? NoteName RT?";
        }

        public int getBaseNameGroup() {
            return 1;
        }

        public int getNoteNameGroup() {
            return 3;
        }

        public int getVelocityGroup() {
            return 2;
        }

        public int getReleaseTriggerGroup() {
            return 4;
        }

        public int getVariationNumberGroup() {
            return -1;
        }

        public List<String> velocities() {
            return Arrays.asList("p", "mf", "f");
            // return Arrays.asList("pp", "p", "f", "ff");
        }
    }

    protected SampleCollection sampleCollection;
    //protected String sampleDirName;
    protected int releaseLevel;
    protected boolean overwrite;

    /**
     * Command-line arguments:
     *
     * Optional: sample format name specified with: -format FORMAT_NAME
     *
     * -note
     *
     * -o
     *
     *
     * Required argument: directory name
     *
     * Output filename is implied from sample filenames plus .sfz extension.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // At least one argument is required:
        if (args.length < 1) {
            CreateSFZ createSFZ = new CreateSFZ();
            createSFZ.showUsage(System.out);
            System.exit(1);
        }
        boolean shownUsage = false;
        String dirname = null;
        String filenameFilter = null;
        String formatName = null; // DEFAULT_FORMAT_NAME;
        int rootNote = -1; // MIDI.noteNameToNumber("C3");
        int releaseLevel = 0; // passed to volume= param for release triggers, specified in db: -144 to 6
        String outputFilename = null;
        boolean overwrite = false;
        List<String> sampleNames = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-format")) {
                i++;
                formatName = args[i];
                continue;
            } else if (args[i].equals("-filter")) {
                i++;
                filenameFilter = args[i];
                System.out.println("Filtering sample files using pattern: '" + filenameFilter + "'");
            } else if (args[i].equals("-note")) {
                i++;
                rootNote = MIDI.noteNameToNumber(args[i]);
                System.out.println("Root note: " + rootNote + " (" + args[i] + ")");
                continue;
            } else if (args[i].equals("-o")) {
                i++;
                outputFilename = args[i];
                continue;
            } else if (args[i].equals("-F")) {
                overwrite = true;
                continue;
            } else if (args[i].equals("-releaseLevel")) {
                i++;
                try {
                    releaseLevel = Integer.parseInt(args[i]);
                    if (releaseLevel < -144 || releaseLevel > 6) {
                        System.out.println("Warning: specified releaseLevel (" + releaseLevel + ") outside documented range of -144 to 6 (db).");
                    }
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException("specify '-releaseLevel PERCENT' where PERCENT is an integer value.");
                }
            } else if (args[i].equals("-?") || args[i].equals("-help")) {
                CreateSFZ createSFZ = new CreateSFZ();
                createSFZ.showUsage(System.out);
                shownUsage = true;
            } else {
                File f = new File(args[i]);
                if (!f.exists()) {
                    throw new IOException("no file or directory: " + args[i]);
                } else if (f.isDirectory()) {
                    dirname = args[i];
                } else {
                    sampleNames.add(args[i]);
                }
            }
        }
        // Don't do anything if we showed usage, unless we were given other settings as well:
        if (!shownUsage || args.length > 1) {
            // Consider checking we either set a directory name or gave a sample.
            if (!sampleNames.isEmpty() && dirname != null) {
                throw new RuntimeException("Specify EITHER a filename or directory name.");
            }
            if (sampleNames.isEmpty() && dirname == null) {
                dirname = ".";
            }
            // System.out.println(COPYTEXT);
            CreateSFZ createSFZ = new CreateSFZ(formatName, dirname, filenameFilter, sampleNames, rootNote, releaseLevel);
            createSFZ.overwrite = overwrite;
            createSFZ.writeSFZ(outputFilename);
        }
    }

    public void showUsage(PrintStream out) {
        System.err.println(USAGE);
        for (String fn : KNOWN_FORMATS) {
            try {
                System.out.println("\nFilename format: " + fn);
                Format f = formatForName(fn);
                out.println(" matches: " + f.filenameRegex() + "  e.g. " + f.filenameExample());
            } catch (Exception e) {
            }
        }
    }

    public CreateSFZ() {

    }

    /**
     * Construct a CreateSFZ tool.
     *
     * @param formatName
     * @param sampleDirName
     * @param filenameFilter
     * @param sampleNames
     * @param rootNote
     * @param releaseLevel
     */
    public CreateSFZ(String formatName, String sampleDirName, String filenameFilter, List<String> sampleNames,
            int rootNote, int releaseLevel) {

        this.releaseLevel = releaseLevel;
        // sampleDirName MUST end in a file separator:
        // SForzando at least doesn't add a separator between the dir name we give and any samples...
        if (sampleDirName != null && !sampleDirName.endsWith(File.separator)) {
            sampleDirName = sampleDirName + File.separator;
        }
        //this.sampleDirName = sampleDirName;
        Format format = formatName != null ? formatForName(formatName) : formatProbe(sampleDirName, filenameFilter, sampleNames);
        if (format == null) {
            throw new RuntimeException("no recognised sample filename format");
        }
        // Create a SampleCollection from the given directory or sample names:
        try {
            if (sampleNames.isEmpty()) {
                sampleCollection = new SampleCollection(format, sampleDirName, filenameFilter);
            } else {
                sampleCollection = new SampleCollection(format, sampleNames, rootNote);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Return a Format for the given name, or null if none recognised.
     *
     * @param formatName
     * @return
     */
    public Format formatForName(String formatName) {
        switch (formatName) {
            case "pianobook": {
                return new Format_PianoBook1();
            }
            case "format1": {
                return new Format1();
            }
            case "format2": {
                return new Format2();
            }
            default:
                return null;
        }
    }

    /**
     * Probe the files in a directory for the sample Format with the most
     * recognised Samples.
     *
     * @param sampleDirName
     * @param filenameFilter
     * @param sampleNames
     * @return Format or null
     */
    public Format formatProbe(String sampleDirName, String filenameFilter, List<String> sampleNames) {
        System.out.println("Probing for recognised sample filename format...");
        int mostFound = 0;
        String bestName = null;
        Format bestFormat = null;
        for (String fn : KNOWN_FORMATS) {
            try {
                System.out.println("Trying filename format: " + fn);
                Format f = formatForName(fn);
                SampleCollection sc = createSampleCollection(f, sampleDirName, filenameFilter, sampleNames);
                if (sc.samples.size() > mostFound) {
                    bestName = fn;
                    bestFormat = f;
                }
            } catch (IOException ioe) {
                // ignore this Format...
            }
        }
        if (bestName != null) {
            System.out.println("Using format: " + bestName);
            return bestFormat;
        } else {
            return null;
        }
    }

    public SampleCollection createSampleCollection(Format format, String sampleDirName, String filenameFilter,
            List<String> sampleNames) throws IOException {

        SampleCollection s = null;
        if (!sampleNames.isEmpty()) {
            s = new SampleCollection(format, sampleNames, 0 /* just probing... */);
        } else {
            s = new SampleCollection(format, sampleDirName, filenameFilter);
        }
        return s;
    }

    /**
     * Write out the SFZ file.
     *
     * @param outputFilename
     * @throws IOException
     */
    protected void writeSFZ(String outputFilename) throws IOException {

        if (!overwrite && new File(outputFilename).exists()) {
            throw new IOException("destination/output file exists: " + outputFilename);
        }
        sampleCollection.writeSFZ(outputFilename, KEY_RANGE, KEY_RANGE, releaseLevel);
    }
}
