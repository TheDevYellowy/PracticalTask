package com.thedevyellowy.practicalTask;

import com.thedevyellowy.practicalTask.games.Game;
import com.thedevyellowy.practicalTask.games.HideAndSeek;
import com.thedevyellowy.practicalTask.games.HungerGames;
import io.papermc.paper.command.brigadier.Commands;
import net.md_5.bungee.api.ChatMessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class PracticalTask extends JavaPlugin implements Listener { // Minigame plugin
  public Logger Logger = LogManager.getLogger("PracticalTask");

  public List<RGame> games = new ArrayList<>();
  public List<Class<? extends Game>> gameTypes = new ArrayList<>();

  @Override
  public void onEnable() {
    var root = Commands.literal("minigames");
    gameTypes.add(HideAndSeek.class);
    gameTypes.add(HungerGames.class);

    for (Class<? extends Game> gameType : this.gameTypes) {
      root
        .then(Commands.literal(gameType.getName())
          .then(Commands.literal("create").requires(src -> {
            Entity executor = src.getExecutor();
            if (!(executor instanceof Player host)) return false;
            return games.stream().noneMatch(data -> data.gClass().equals(gameType));
          }).executes(ctx -> {
            Entity executor = ctx.getSource().getExecutor();
            if (!(executor instanceof Player host)) return 1;
            Logger.info(gameType.toString());
            long hosted = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).count();
            if (hosted > 0) {
              executor.sendMessage("You can only host one game of this type at this time");
              return 1;
            }
            // get instantiation of the game type, and add it to the list of "games"
            return 1;
          }))
          .then(Commands.literal("setSpawn").requires(src -> {
            Entity executor = src.getExecutor();
            if (!(executor instanceof Player host)) return false;
            Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
            return optional.map(data -> data.game().hasCommand("setSpawn")).orElse(false);
          }).executes(ctx -> {
            Entity executor = ctx.getSource().getExecutor();
            if (!(executor instanceof Player host)) return 1;
            Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
            return optional.map(rGame -> rGame.game().runCommand("setSpawn", ctx)).orElse(1);
          }))
        );
    }
  }

  @Override
  public void onDisable() {
    games.forEach(data -> {
      data.game().unregisterEvents();
      data.game().getPlayers().forEach(player -> {
        player.sendMessage("The plugin has either crashed or been disabled, your game has stopped. You are now free to leave the area!");
      });
    });
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
    games.add(new RGame(game.getId(), game.getHost(), game, game.getClass()));
  }

  public record RGame(String id, Player host, Game game, Class<? extends Game> gClass) {
  }
}
