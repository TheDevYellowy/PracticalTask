package com.thedevyellowy.practicalTask.games;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class Game implements Listener {
  HashMap<Player, Location> returnLocations = new HashMap<>();
  List<String> commands = new ArrayList<>();
  List<Player> players = new ArrayList<>();
  int gracePeriodTime = 0; // Seconds
  boolean started = false;
  boolean gracePeriod = true;
  BoundingBox radius;
  long startTime;
  int maxPlayers;
  int timeLimit; // Minutes
  Player host;
  String id;

  // Time constants
  int second = 1000;
  int minute = 60000;

  String id_chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public Game(int maxPlayers) {
    this.maxPlayers = maxPlayers;
    this.id = generateId();

    if (this.gracePeriodTime < 1) this.gracePeriod = false;
  }

  public abstract void start();

  abstract String getCreationMessage();

  public abstract int runCommand(String subCommand, CommandContext<CommandSourceStack> ctx);

  public abstract void unregisterEvents();

  public boolean hasCommand(String command) {
      return commands.contains(command);
  }

  private String generateId() {
    /* Use random characters that include [a-z A-Z 0-9]
     * # of characters and their resulting max game amount ( 36^x )
     * 1  = 36
     * 2  = 1296
     * 3  = 46656 <- will probably use this one, we don't need more than 46 thousand games at one time
     * 4  = 1679616 | million
     * 5  = 60466176
     * 6  = 2176782336 | billion
     * 7  = 78364164096
     * 8  = 2.8211099e+12 | trillion
     * 9  = 1.0155996e+14
     * 10 = 3.6561584e+15 | quintillion
     */

    Random random = new Random();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i <= 3; i++) {
      int ranI = random.nextInt(id_chars.length());
      builder.append(id_chars.charAt(ranI));
    }

    return builder.toString();
  }

  public String getId() {
    return this.id;
  }

  public void setHost(@NotNull Player host) {
    this.host = host;
    this.players.add(host);
    this.host.sendMessage(this.getCreationMessage());
  }

  public boolean started() { return started; }

  public Player getHost() {
    return host;
  }

  public boolean isHost(Player player) {
    return this.host.equals(player);
  }

  public void setRadius(Location pointOne, Location pointTwo) {
    this.radius = new BoundingBox(pointOne.blockX(), pointOne.blockY(), pointOne.blockZ(), pointTwo.blockX(), pointTwo.blockY(), pointTwo.blockZ());
  }

  public List<Player> getPlayers() {
    return players;
  }

  public boolean addPlayer(Player player) {
    if (players.size() > maxPlayers) return false;
    players.add(player);
    return true;
  }

  boolean notInsideArea(Entity player) {
    return !this.radius.contains(player.getBoundingBox());
  }

  boolean notInsideArea(Location position) {
    return !this.radius.contains(position.toVector());
  }

  boolean isInGracePeriod() {
    if (this.gracePeriodTime == 0) return false;
    return Instant.now().toEpochMilli() <= this.startTime + ((long) this.gracePeriodTime * second);
  }

  double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  @Override
  public String toString() {
    return "Game";
  }
}
