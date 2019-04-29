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

import java.io.File;
import java.io.IOException;
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

    public static final String USAGE = "java CreateSFZ[ -format FORMAT_NAME ] [ -o OUPTUTFILE ] [ -note NOTENAME ]  FILE or DIRECTORY \n"
            + "where:\n"
            + "[ ... ] options are optional\n"
            + "DIRECTORY is a directory name to scan entirely for samples\n"
            + "FILE is a single file to use\n"
            + "FORMAT_NAME can be 'pianobook' or 'format1' (the default)";

    public static final String DEFAULT_FORMAT_NAME = "format1";

    public interface Format {

        public String filenameRegex();

        public List<String> velocities();
    }

    public class Format1 implements Format {

        public String filenameRegex() {
            return "(.*)_([a-zA-Z]+)\\-(.*)\\-(\\d+)\\.wav"; // "baseName_velocityName-NoteName-VariationNumber"
        }

        public List<String> velocities() {
            return Arrays.asList("Soft", "Medium", "Hard");
        }
    }

    public class Format_PianoBook implements Format {

        public String filenameRegex() {
            return "(.*)\\s+([pf]+)\\s+(.*)\\.wav"; // "baseName p|f NoteName"
        }

        public List<String> velocities() {
            return Arrays.asList("p", "f");
            // return Arrays.asList("pp", "p", "f", "ff");
        }
    }

    protected SampleCollection samples;
    protected String sampleDirName;

    /**
     * Command-line arguments:
     *
     * Optional: sample format name specified with: -format FORMAT_NAME
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
            System.err.println(USAGE);
            System.exit(1);
        }
        String dirname = null;
        String formatName = DEFAULT_FORMAT_NAME;
        int rootNote = -1; // MIDI.noteNameToNumber("C3");
        String outputFilename = null;
        List<String> sampleNames = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-format")) {
                i++;
                formatName = args[i];
                continue;
            } else if (args[i].equals("-note")) {
                i++;
                rootNote = MIDI.noteNameToNumber(args[i]);
                continue;
            } else if (args[i].equals("-o")) {
                i++;
                outputFilename = args[i];
                continue;
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
        // Consider checking we either set a directory name or gave a sample.
        if (!sampleNames.isEmpty() && dirname != null) {
            throw new RuntimeException("Specify EITHER a filename or directory name.");
        }
        if (sampleNames.isEmpty() && dirname == null) {
            dirname = ".";
        }
        // System.out.println(COPYTEXT);
        CreateSFZ createSFZ = new CreateSFZ(formatName, dirname, sampleNames, rootNote);
        createSFZ.writeSFZ(outputFilename);
    }

    /**
     * Construct a CreateSFZ tool.
     *
     * @param formatName
     * @param sampleDirName
     * @param sampleNames
     * @param rootNote
     */
    public CreateSFZ(String formatName, String sampleDirName, List<String> sampleNames, int rootNote) {
        this.sampleDirName = sampleDirName;
        // SForzando at least doesn't add a separator between the dir name we give and any samples...
        if (sampleDirName != null && !sampleDirName.endsWith(File.separator)) {
            this.sampleDirName += File.separator;
        }
        Format format = null;
        switch (formatName) {
            case "pianobook": {
                format = new Format_PianoBook();
                break;
            }
            default:
                format = new Format1();
        }
        // Create a SampleCollection from the given directory or sample names:
        try {
            if (!sampleNames.isEmpty()) {
                samples = new SampleCollection(format, sampleNames, rootNote);
            } else {
                samples = new SampleCollection(format, this.sampleDirName);
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     * Write out the SFZ file.
     *
     * @param outputFilename
     * @throws IOException
     */
    protected void writeSFZ(String outputFilename) throws IOException {
        samples.writeSFZ(outputFilename);
    }
}
