/*
 * 2019 Kevin Walls
 */
package org.actg.createsfz;

/**
 * Sample information such as filename, the note and velocity it represents.
 */
public class Sample implements Comparable {

    protected String filename;
    protected int noteNumber;
    protected int velocity;
    protected int id;

    public Sample(String filename, String basename, int noteNumber, int velocity, int id) {
        this.filename = filename;
        this.noteNumber = noteNumber;
        this.velocity = velocity;
        this.id = id;
    }

    @Override
    public int compareTo(Object o) {
        Sample other = (Sample) o;
        int result = noteNumber - other.noteNumber;
        if (result == 0) {
            result = velocity - other.velocity;
        }
        if (result == 0) {
            result = velocity - other.velocity;
        }
        if (result == 0) {
            result = id - other.id;
        }
        return result;
    }

}
