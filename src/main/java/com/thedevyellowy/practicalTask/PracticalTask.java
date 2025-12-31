package com.thedevyellowy.practicalTask;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thedevyellowy.practicalTask.games.Game;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PracticalTask extends JavaPlugin implements Listener { // Minigame plugin
    public List<Game> games = new ArrayList<>();

    @Override
    public void onEnable() {
        var root = Commands.literal("minigames");
        for (Class<? extends Game> gameType : getGameTypes()) {
            root
                    .then(Commands.literal(gameType.getName()))
                    .then(Commands.literal("create"))
                    .executes(src -> {
                        Entity executor = src.getSource().getExecutor();
                        if(!(executor instanceof Player host)) return 1;
                        // get instanciation of the game type, and add it to the list of "games"
                        return 1;
                    });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Get all classes that extend the Game class and get the name of the class for future use
    public List<Class<? extends Game>> getGameTypes() {
        List<Class<? extends Game>> gameTypes = new ArrayList<>();
        Reflections reflections = new Reflections("com.thedevyellowy.practicalTask");
        Set<Class<? extends Game>> types = reflections.getSubTypesOf(Game.class);

        gameTypes.addAll(types);

        return gameTypes;
    }

    public <T extends Game> void addGame(T game) {
        Bukkit.getPluginManager().registerEvents(game, this);
        games.add(game);
    }
}
