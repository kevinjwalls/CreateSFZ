# CreateSFZ
Free software (GPL), NO WARRANTY.

Create SFZ format sound sample definitions from a directory of samples.
Load the .SFZ file in a player (e.g. Sforzando).

    java -jar CreateSFZ.jar [ -filter FILENAME_FILTER] [ -format FORMAT_NAME ] [ -o OUPTUTFILE ] [ -note NOTENAME ]  FILE or DIRECTORY
    where:
    [ ... ] options are optional
     -filter FILENAME_FILTER      Specifies text that must be in sample filenames
    DIRECTORY is a directory name to scan entirely for samples
    FILE is a single file to use
    FORMAT_NAME can be 'pianobook' or 'format1' (the default)



CreateSFZ scans a directory for filenames in known formats, extracts base instrument name,
velocity, note name (C0, G#3, etc...),
a variation number (for multiple samples per note, will be cycled through...).
The found files are used to create an instrument file: the base instrument name is used plus a file extension ".sfz", unless the -o option is used to specify an output filename.

Notes are given a range of keys extended DOWNWARD from the pitch named in the sample filename, to the next lowest sample.

The default format recognises:
"baseName\_velocityName-NoteName-VariationNumber.wav"

..where velocity name is Soft, Medium or Hard.

Specifying "pianobook" recognises:
 "baseName velocity NoteName RT?.wav"
..where velocity is any number of m, p, f characters,
and RT is present only for a release trigger (played on key or sustain pedal up).


# Single Sample Usage

Specifying a single file creates a .sfz instrument with just that one sample.
The filename is not interpreted for pitch etc, so the argument -note is needed, e.g. -note c3
Also -o is needed.


# Building
    git clone ...
    cd CreateSFZ                   # Presuming the clone directory name is CreateSFZ
    
Build with "make":

    make
    java -jar build/CreateSFZ.jar ...options....
    
Build manually:

    cd src
    javac -d ..\build -sourcepath src org\actg\createsfz\*.java
    
(reverse the slashes on Linux...)
then...
    cd ..
    jar cvfe CreateSFZ.jar org/actg/createsfz/CreateSFZ -C build org/actg/createsfz


# Previous github "release" has more basic argument parsing:

    java -jar CreateSFZ.jar directoryName 
    java -jar CreateSFZ.jar directoryName pianobook




# SFZ Format References

http://www.sfzformat.com/index.php?title=Main_Page

https://en.wikipedia.org/wiki/SFZ_(file_format)


