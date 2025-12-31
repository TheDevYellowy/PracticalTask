package com.thedevyellowy.practicalTask.games;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.command.brigadier.Commands.argument;

public class HideAndSeek extends Game {
    List<Player> seekers = new ArrayList<>();
    List<Player> hiders = new ArrayList<>();

    public HideAndSeek(int maxPlayers) {
        super(maxPlayers);
    }

    @Override
    void start() {
        List<String> errors = new ArrayList<>();
        if(this.radius == null) {
            errors.add("radius");
        }
    }

    @Override
    int runCommand(CommandContext<CommandSourceStack> ctx) {
        return Command.SINGLE_SUCCESS;
    }

    @Override
    void unregisterEvents() {
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    void onWin(String team) {}

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!this.players.contains(player)) return;
        if(!this.started) return;

        if(!this.isInsideArea(player) && !event.isCancelled()) event.setCancelled(true);
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!this.players.contains(player)) return;
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
}
