package com.thedevyellowy.practicalTask.games;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class HungerGames extends Game { // Code this after Hide and Seek as a proof of concept for different mini-games
  /*Todo
   * Loot Table for Chests
   * InventoryInteractEvent for right-clicking events
   * spectating | make invisible, disable all attack events, allow ability to fly (maybe), set teamate as glowing
   * */
  List<Location> lootable = new ArrayList<>();
  List<Location> looted = new ArrayList<>();

  HungerGames(int maxPlayers) {
    super(maxPlayers);
  }

  @Override
  public void start() {
  }

  @Override
  String getCreationMessage() {
    return "";
  }

  @Override
  public int runCommand(String subCommand, CommandContext<CommandSourceStack> ctx) {
    return 0;
  }

  @Override
  public void unregisterEvents() {

  }

  @Override
  public String toString() {
    return "Game<HungerGames>";
  }
}
