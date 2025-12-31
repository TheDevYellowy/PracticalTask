package com.thedevyellowy.practicalTask;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thedevyellowy.practicalTask.games.Game;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PracticalTask extends JavaPlugin implements Listener { // Minigame plugin
    public List<Game> games = new ArrayList<>();
    public LiteralArgumentBuilder<CommandSourceStack> commandSource;

    @Override
    public void onEnable() {
        commandSource = Commands.literal("minigames");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Get all classes that extend the Game class and get the name of the class for future use
    public List<String> getGameTypes() {
        List<String> gameTypes = new ArrayList<>();
        Reflections reflections = new Reflections("com.thedevyellowy.practicalTask");
        Set<Class<? extends Game>> types = reflections.getSubTypesOf(Game.class);

        for (Class<? extends Game> type : types) {
            gameTypes.add(type.getName());
        }

        return gameTypes;
    }

    public <T extends Game> void addGame(T game) {
        Bukkit.getPluginManager().registerEvents(game, this);
        games.add(game);
    }
}
