package com.thedevyellowy.practicalTask.games;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HideAndSeek extends Game {
    List<Player> seekers = new ArrayList<>();
    List<Player> hiders = new ArrayList<>();
    Location spawn;

    public HideAndSeek(int maxPlayers) {
        super(maxPlayers);

        this.timeLimit = 3; // Minutes
        this.gracePeriodTime = 30; // Seconds

        this.commands.add("setSpawn");
        this.commands.add("setArea");
    }

    @Override
    void start() {
        List<String> errors = new ArrayList<>();
        if(this.radius == null) {
            errors.add("radius");
        }

        if(this.spawn == null) {
            errors.add("spawn");
        }

        if(!errors.isEmpty()) {
            host.sendMessage("Couldn't start the game as the following required parameters aren't set", String.join(", ", errors));
            return;
        }

        this.started = true;
        this.startTime = Instant.now().toEpochMilli();
    }

    @Override
    String getCreationMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("You have created a Hide and Seek game, to make sure everything runs smoothly please make sure you run the following commands\n");
        sb.append("/minigames ").append(this.getClass().getName()).append(" setSpawn | This sets the current spawn to your location, at the start and when players are caught they will be teleported here\n");
        sb.append("/minigames ").append(this.getClass().getName()).append(" setArea | This sets the play area, you'll need to run it twice to set the bounding box\n");
        return sb.toString();
    }

    @Override
    public int runCommand(String subCommand, CommandContext<CommandSourceStack> ctx) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void unregisterEvents() {
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        ServerTickEndEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    boolean hasEnded() {
        return this.hiders.isEmpty() || this.seekers.isEmpty();
    }

    void onWin(String team) {}

    @EventHandler
    void onTick(ServerTickEndEvent event) {
        if(!this.started) return;
        if(!this.isInGracePeriod() && this.gracePeriod) {
            this.gracePeriod = false;
        }

        if(this.isInGracePeriod()) {
            for (Player seeker : this.seekers) {
                if(!seeker.hasPotionEffect(PotionEffectType.BLINDNESS)) seeker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999, 4, false, false));
            }
        }
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!this.players.contains(player)) return;
        if(!this.started) return;

        if(this.gracePeriod && this.seekers.contains(player) && !event.isCancelled()) event.setCancelled(true);
        if(!this.isInsideArea(player) && !event.isCancelled()) {
            event.setCancelled(true);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("That is not within the playable area"));
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!this.players.contains(player)) return;

        if(!event.isCancelled()) event.setCancelled(true);
    }

    @EventHandler
    void onDamageEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if(!(damager instanceof Player hunter)) return;
        if(!(damaged instanceof Player hider)) return;

        if(!this.seekers.contains(hunter) && !event.isCancelled()) event.setCancelled(true);
        if(!this.hiders.contains(hider) && !event.isCancelled()) event.setCancelled(true);
        if(event.isCancelled()) return;

        this.hiders.remove(hider);
        this.seekers.add(hider);

        if(this.hiders.isEmpty()) {
            this.onWin("Seekers");
            return;
        }

        for (Player player : this.players) {
            player.sendMessage(String.format("%s has been found, only %s hider%s remain%s", hider.getDisplayName(), this.hiders.size(), this.hiders.size() > 1 ? "s" : "", this.hiders.size() > 1 ? "" : "s"));
        }
    }

    @Override
    public String toString() {
        return "Game<HideAndSeek>";
    }
}
