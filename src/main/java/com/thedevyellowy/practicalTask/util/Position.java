package com.thedevyellowy.practicalTask.util;

import org.bukkit.Location;
import org.joml.Vector3d;

public class Position {
    public final Point x;
    public final Point y;
    public final Point z;

    public Position(Location one, Location two) {
        this.x = new Point(one.getX(), two.getX());
        this.y = new Point(one.getY(), two.getY());
        this.z = new Point(one.getZ(), two.getZ());
    }
}
