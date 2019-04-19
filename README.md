# CreateSFZ
Free software (GPL), NO WARRANTY.


Create SFZ format sound sample definitions from a directory of samples.
Load the .SFZ file in a player (e.g. Sforzando).


CreateSFZ scans a directory for filenames in known formats, extracts 
base instrument name,
velocity,
note name (C0, G#3, etc...),
a variation number (for multiple samples per note, will be cycled through...).

The base instrument name is used plus a file extension ".sfz".

Notes are given a range of keys extended DOWNWARD from the pitch named in the sample filename.

The default format recognises:
"baseName\_velocityName-NoteName-VariationNumber.wav"

..where velocity name is Soft, Medium or Hard.

Specifying "pianobook" recognises:
 "baseName p|f NoteName.wav"

i.e. velocity is p or f


# Building
    git clone ...
    cd src
    javac -d ..\build -sourcepath src org\actg\createsfz\*.java

(reverse the slashes on Linux...)


then...

    cd ..
    jar cvfe CreateSFZ.jar org/actg/createsfz/CreateSFZ -C build org/actg/createsfz


# Running

    java -jar CreateSFZ.jar directoryName 
    java -jar CreateSFZ.jar directoryName pianobook




