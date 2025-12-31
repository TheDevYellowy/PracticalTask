package com.thedevyellowy.practicalTask.util;

public class Point {
    public final double lower;
    public final double upper;

    public Point(double one, double two) {
        if(one >= two) {
            this.upper = one;
            this.lower = two;
        } else {
            this.upper = two;
            this.lower = one;
        }
    }
}
