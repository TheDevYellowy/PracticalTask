package com.thedevyellowy.practicalTask.games;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.mojang.brigadier.context.CommandContext;
import com.thedevyellowy.practicalTask.PracticalTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class HideAndSeek extends Game {
  List<Player> seekers = new ArrayList<>();
  List<Player> hiders = new ArrayList<>();
  Location spawn;
  Location temp;

  Scoreboard board;
  Team hiderTeam;
  Team seekerTeam;

  public HideAndSeek(int maxPlayers) {
    super(maxPlayers);

    this.timeLimit = 3; // Minutes
    this.gracePeriodTime = 30; // Seconds

    this.commands.add("setSpawn");
    this.commands.add("setArea");
    this.board = Bukkit.getScoreboardManager().getNewScoreboard();
    this.hiderTeam = this.board.registerNewTeam(this.getId() + "_hiders");
    this.seekerTeam = this.board.registerNewTeam(this.getId() + "_seekers");
  }

  @Override
  public void start() {
    List<String> errors = new ArrayList<>();
    if (this.radius == null) {
      errors.add("radius");
    }

    if (this.spawn == null) {
      errors.add("spawn");
    }

    if (!errors.isEmpty()) {
      host.sendMessage("Couldn't start the game as the following required parameters aren't set", String.join(", ", errors));
      return;
    }

    this.seekers.add(this.chooseSeeker());
    this.hiders.addAll(this.players.stream().filter(player -> !this.seekers.contains(player)).toList());

    this.hiderTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
    this.seekerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);

    this.seekers.forEach(player -> this.seekerTeam.addPlayer(player));
    this.hiders.forEach(player -> this.hiderTeam.addPlayer(player));

    this.players.forEach(player -> {
      returnLocations.put(player, player.getLocation());

      if (this.seekers.contains(player)) {
        Location tp = this.spawn;
        tp.setYaw(-180);
        tp.setPitch(90);
        player.teleportAsync(tp);
      } else player.teleportAsync(this.spawn);
    });

    this.started = true;
    this.startTime = Instant.now().toEpochMilli();
  }

  @Override
  String getCreationMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("You have created a Hide and Seek game, to make sure everything runs smoothly please make sure you run the following commands\n");
    sb.append("/minigames ").append(this.getClass().getSimpleName()).append(" setSpawn | This sets the current spawn to your location, at the start and when players are caught they will be teleported here\n");
    sb.append("/minigames ").append(this.getClass().getSimpleName()).append(" setArea | This sets the play area, you'll need to run it twice to set the bounding box\n");
    return sb.toString();
  }

  @Override
  public int runCommand(String subCommand, CommandContext<CommandSourceStack> ctx) {
    return switch(subCommand) {
      case "setArea":
        Location pos = ctx.getSource().getLocation();
        if(temp == null) {
          temp = pos;
          ctx.getSource().getExecutor().sendMessage("position 1 has been set to " + String.format("%s %s %s", temp.getX(), temp.getY(), temp.getZ()));
        } else {
          this.setRadius(temp, pos);
          ctx.getSource().getExecutor().sendMessage("The play area has been set!");
          temp = null;
        }
        yield 1;
      case "setSpawn":
        this.setSpawn(ctx.getSource().getLocation());
        yield 1;
      default:
        ctx.getSource().getExecutor().sendMessage("This hasn't been implemented due to time constraints but you ran " + subCommand);
        yield 1;
    };
  }

  @Override
  public void unregisterEvents() {
    BlockBreakEvent.getHandlerList().unregister(this);
    PlayerMoveEvent.getHandlerList().unregister(this);
    ServerTickEndEvent.getHandlerList().unregister(this);
    EntityDamageByEntityEvent.getHandlerList().unregister(this);
  }

  public void setSpawn(Location spawn) {
    if(this.radius == null) {
      this.host.sendMessage("please set the playable area before setting the spawn point");
      return;
    }
    if(this.notInsideArea(spawn)) {
      this.host.sendMessage("That position is not inside your set playable area");
      return;
    }
    this.host.sendMessage("Spawn set!");
    this.spawn = spawn;
  }

  void onWin(String team) {
    Title title = Title.title(Component.text(team + " have won!"), Component.text("teleporting everyone back"), 20, 100, 10);
    this.players.forEach(player -> {
      player.showTitle(title);
      player.teleport(returnLocations.get(player));
    });

    this.started = false;
    this.unregisterEvents();
    this.seekerTeam.unregister();
    this.hiderTeam.unregister();

    PracticalTask.removeGame(this);
  }

  Player chooseSeeker() {
    Random random = new Random();
    if(this.players.size() > 1) return this.players.get(Math.round(random.nextFloat(0, this.players.size())));
    else return this.players.getFirst();
  }

  @EventHandler
  void onTick(ServerTickEndEvent event) {
    if (!this.started) return;
    if (!this.isInGracePeriod() && this.gracePeriod) {
      this.gracePeriod = false;

      for (Player seeker : this.seekers) {
        if(seeker.hasPotionEffect(PotionEffectType.BLINDNESS)) seeker.removePotionEffect(PotionEffectType.BLINDNESS);
      }

      if(this.hiders.isEmpty()) {
        this.onWin("Loners");
        return;
      }
    }

    if (this.isInGracePeriod()) {
      for (Player seeker : this.seekers) {
        if (!seeker.hasPotionEffect(PotionEffectType.BLINDNESS))
          seeker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.gracePeriodTime * 20, 4, false, false));
        else seeker.sendActionBar(Component.text(seeker.getPotionEffect(PotionEffectType.BLINDNESS).getDuration() / 20));
      }
    }

    if(Instant.now().toEpochMilli() >= this.startTime + ((long) this.timeLimit * this.minute)) {
      this.onWin("Hiders");
    }
  }

  @EventHandler(ignoreCancelled = true)
  void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (!this.players.contains(player)) return;
    if (!this.started) return;

    if (this.isInGracePeriod() && this.seekers.contains(player)) player.teleport(event.getFrom());
    if (this.notInsideArea(event.getTo())) {
      Location to = event.getTo();

      double clampedX = this.clamp(to.getX(), this.radius.getMinX(), this.radius.getMaxX());
      double clampedY = to.getY();
      double clampedZ = this.clamp(to.getZ(), this.radius.getMinZ(), this.radius.getMaxZ());

      Location clamped = to.clone();
      clamped.set(clampedX, clampedY, clampedZ);

      event.setTo(clamped);

      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("That is not within the playable area"));
    }
  }

  @EventHandler
  void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (!this.players.contains(player)) return;
    if (!this.started()) return;

    if (!event.isCancelled()) event.setCancelled(true);
  }

  @EventHandler
  void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    if (!this.players.contains(player)) return;
    if (!this.started()) return;

    if (!event.isCancelled()) event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  void onDamageEvent(EntityDamageByEntityEvent event) {
    Entity damaged = event.getEntity();
    Entity damager = event.getDamager();

    if(this.notInsideArea(damaged) || this.notInsideArea(damager)) return;
    if (!(damager instanceof Player seeker)) return;
    if (!(damaged instanceof Player hider)) return;
    if(!this.players.contains(seeker) || !this.players.contains(hider)) return;

    if (!this.seekers.contains(seeker)) {
      event.setCancelled(true);
      return;
    }
    if (!this.hiders.contains(hider)) {
      event.setCancelled(true);
      return;
    }
    event.setCancelled(true);

    this.hiders.remove(hider);
    this.hiderTeam.removePlayer(hider);
    this.seekers.add(hider);
    this.seekerTeam.addPlayer(hider);

    if (this.hiders.isEmpty()) {
      this.onWin("Seekers");
      return;
    }

    for (Player player : this.players) {
      player.sendActionBar(Component.text(String.format("%s has been found, only %s hider%s remain%s", hider.displayName(), this.hiders.size(), this.hiders.size() > 1 ? "s" : "", this.hiders.size() > 1 ? "" : "s")));
    }
  }

  @Override
  public String toString() {
    return "Game<HideAndSeek>";
  }
}
