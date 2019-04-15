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
import java.util.Arrays;
import java.util.List;

/**
 * CreateSFZ is the Main class.
 */
public class CreateSFZ {

    public static String COPYTEXT = "CreateSFZ: Create SFZ format files from a directory of sound samples.\n"
            + "Copyright (C) 2019 Kevin Walls\n"
            + "This program comes with ABSOLUTELY NO WARRANTY.\n"
            + "This is free software, and you are welcome to redistribute it\n"
            + "under certain conditions.";

    public static String USAGE = "java CreateSFZ DIRECTORY [ FORMAT_NAME ] \n"
            + "where:\n"
            + "FORMAT_NAME can be 'pianobook'";

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
     * Required argument: directory name
     *
     * Optional: sample format name
     *
     * Output filename is implied from sample names.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.err.println(USAGE);
            System.exit(1);
        }
        String dirname = args[0];
        String formatName = null;
        if (args.length > 1) {
            formatName = args[1];
        } else {
            formatName = "format1";
        }
        // System.out.println(COPYTEXT);
        CreateSFZ createSFZ = new CreateSFZ(formatName, dirname);
    }

    public CreateSFZ(String formatName, String sampleDirName) {
        this.sampleDirName = sampleDirName;
        // SForzando at least doesn't add a separator between the dir name we give and any samples...
        if (!sampleDirName.endsWith(File.separator)) {
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
        // Create a SampleCollection from the given directory:
        try {
            samples = new SampleCollection(format, this.sampleDirName);
            samples.writeSFZ();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
