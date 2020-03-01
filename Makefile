all: CreateSFZ

JAVAC=javac -g -source 8 -target 8 

SRCS=org/actg/createsfz/CreateSFZ.java org/actg/createsfz/MIDI.java org/actg/createsfz/Sample.java org/actg/createsfz/SampleCollection.java

JARNAME=build/CreateSFZ.jar

CreateSFZ: 
	cd src && ${JAVAC} -d ../build -sourcepath src ${SRCS}
	jar cvfe ${JARNAME} org/actg/createsfz/CreateSFZ -C build org/actg/createsfz


clean:
	rm -Rf build
	mkdir build

