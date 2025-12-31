package com.thedevyellowy.practicalTask.games;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thedevyellowy.practicalTask.util.Position;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class Game implements Listener {
    List<Player> players = new ArrayList<>();
    int maxPlayers;
    String id;
    Position radius;
    boolean started = false;

    public Game(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.id = generateId();
    }

    abstract void start();

    abstract void addCommands(LiteralArgumentBuilder<CommandSourceStack> commandSource);

    abstract void unregisterEvents();

    private String generateId() {
        return "";
    }

    public void setRadius(Vector3d pointOne, Vector3d pointTwo) {
        this.radius = new Position(pointOne, pointTwo);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if(players.size() > maxPlayers) return false;
        players.add(player);
        return true;
    }

    boolean isInsideArea(Player player) {
        Location location = player.getLocation();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return (radius.x.lower <= x && x >= radius.x.upper)
                || (radius.y.lower <= y && y >= radius.y.upper)
                || (radius.z.lower <= z && z >= radius.z.upper);
    }
}
