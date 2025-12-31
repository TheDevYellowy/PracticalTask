package com.thedevyellowy.practicalTask.util;

import org.joml.Vector3d;

public class Position {
    public final Point x;
    public final Point y;
    public final Point z;

    public Position(Vector3d one, Vector3d two) {
        this.x = new Point(one.x, two.x);
        this.y = new Point(one.y, two.y);
        this.z = new Point(one.z, two.z);
    }
}
