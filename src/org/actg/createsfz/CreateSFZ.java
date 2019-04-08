/*
 * 2019 Kevin Walls
 */
package org.actg.createsfz;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.PrintStream;

/**
 * CreateSFZ is the Main class.
 *
 */
public class CreateSFZ {

    public static String USAGE = "java CreateSFZ NEWFILE.SFZ DIRECTORY";
    public static String HEADER = "//\n// SFZ file created by CreateSFZ.\n//";
    public static String FOOTER = "//\n// End of SFZ file created by CreateSFZ.\n//";

    protected SampleCollection samples;
    protected String sampleDirName;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println(USAGE);
            System.exit(1);
        }
        String newSFZFilename = args[0];
        File outputFile = new File(newSFZFilename);
        if (outputFile.exists()) {
            throw new IOException("destination/output file exists: " + outputFile.getCanonicalFile());
        }
        CreateSFZ createSFZ = new CreateSFZ(args[1]);
        createSFZ.write(outputFile);
    }

    public CreateSFZ(String sampleDirName) throws IOException {
        this.sampleDirName = sampleDirName;
        // SForzando at least doesn't add a separator between the dir name we give and any samples...
        if (!sampleDirName.endsWith(File.separator)) {
            this.sampleDirName += File.separator;
        }
        // Create a SampleCollection from the given directory:
        samples = new SampleCollection(sampleDirName);
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
