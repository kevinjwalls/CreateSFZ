/*
 * 2019 Kevin Walls
 */
package org.actg.createsfz;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * CreateSFZ is the Main class.
 *
 */
public class CreateSFZ {

    public static String USAGE = "java CreateSFZ NEWFILE.SFZ DIRECTORY [ FORMAT_NAME ] \n"
            + "where:\n"
            + "FORMAT_NAME can be 'pianobook'";
    public static String HEADER = "//\n// SFZ file created by CreateSFZ.\n//";
    public static String FOOTER = "//\n// End of SFZ file created by CreateSFZ.\n//";

    protected static String REGEX_NOTENAME_GROUP = "([a-z]#?)";

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
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println(USAGE);
            System.exit(1);
        }
        String newSFZFilename = args[0];
        File outputFile = new File(newSFZFilename);
        if (outputFile.exists()) {
            throw new IOException("destination/output file exists: " + outputFile.getCanonicalFile());
        }
        String dirname = args[1];
        String formatName = null;
        if (args.length > 2) {
            formatName = args[2];
        } else {
            formatName = "format1";
        }
        CreateSFZ createSFZ = new CreateSFZ(formatName, dirname, outputFile);
    }

    public CreateSFZ(String formatName, String sampleDirName, File outputFile) throws IOException {
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
        samples = new SampleCollection(format, sampleDirName);
        if (samples.samples.size() >= 0) {
            write(outputFile);
        }
    }

    public void write(File outputFile) throws IOException {
        PrintStream out = new PrintStream(new FileOutputStream(outputFile));
        out.println(HEADER);
        out.println("<control>");
        out.println("default_path=" + sampleDirName);
        samples.printRegions(out);
        out.println(FOOTER);
    }
}
