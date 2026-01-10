package com.thedevyellowy.practicalTask.games;

import com.mojang.brigadier.context.CommandContext;
import com.thedevyellowy.practicalTask.util.ExternalLootTable;
import com.thedevyellowy.practicalTask.util.HGLootTable;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HungerGames extends Game { // Code this after Hide and Seek as a proof of concept for different mini-games
  /*Todo
   * spectating | make invisible, disable all attack events, set teammate as glowing ( not possible with plugins )
   */
  List<Location> lootable = new ArrayList<>();
  List<Location> looted = new ArrayList<>();
  LootTable lootTable = null;

  public HungerGames(int maxPlayers) {
    super(maxPlayers);
    if(ExternalLootTable.exists()) this.lootTable = new HGLootTable();
}

  @Override
  public void start() {
  }

  @Override
  String getCreationMessage() {
    if(this.lootTable == null) {
      this.host.sendMessage("There is no custom loot table at plugins/Minigames/hungergames_loottable.json, you can create one at https://misode.github.io/loot-table/ to test this functionality. Defaulting to End City loot table");
      this.lootTable = LootTables.END_CITY_TREASURE.getLootTable();
    }
    return "So sorry due to self imposed time constraints this minigame wasn't implemented, you can still look at the loot table functionality by opening any chest, it will clear and reroll on reopen";
  }

  @Override
  public int runCommand(String subCommand, CommandContext<CommandSourceStack> ctx) {
    ctx.getSource().getExecutor().sendMessage("This hasn't been implemented due to time constraints but you ran " + subCommand);
    return 1;
  }

  @Override
  public void unregisterEvents() {
    PlayerInteractEvent.getHandlerList().unregister(this);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      if(event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Chest chest) {
        chest.getInventory().clear();
        lootTable.fillInventory(chest.getInventory(), new Random(), new LootContext.Builder(chest.getLocation()).build());
      }
    }
  }

  @Override
  public String toString() {
    return "Game<HungerGames>";
  }
}
