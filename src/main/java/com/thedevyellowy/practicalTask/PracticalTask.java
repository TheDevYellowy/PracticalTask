package com.thedevyellowy.practicalTask;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.thedevyellowy.practicalTask.games.Game;
import com.thedevyellowy.practicalTask.games.HideAndSeek;
import com.thedevyellowy.practicalTask.games.HungerGames;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class PracticalTask extends JavaPlugin implements Listener { // Minigame plugin
  public static Logger Logger = LogManager.getLogger("PracticalTask");

  public static List<RGame> games = new ArrayList<>();
  public List<Class<? extends Game>> gameTypes = new ArrayList<>();

  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);
    var root = Commands.literal("minigames");
    gameTypes.add(HideAndSeek.class);
    gameTypes.add(HungerGames.class);

    for (Class<? extends Game> gameType : this.gameTypes) {
      root
        .then(Commands.literal(gameType.getSimpleName())
          .then(Commands.literal("create")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player host)) return false;
              return games.stream().noneMatch(data -> (data.host().equals(host) && data.gClass().equals(gameType)) || data.game().getPlayers().contains(host));
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if (!(executor instanceof Player host)) return 1;
              long hosted = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).count();
              if (hosted > 0) {
                executor.sendMessage("You can only host one game of this type at this time");
                return 1;
              }
              try {
                Constructor<?> constructor = gameType.getDeclaredConstructor(int.class);
                Object game = constructor.newInstance(10);

                ((Game) game).setHost(host);
                this.addGame((Game) game);
                host.updateCommands();
              } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                       IllegalAccessException e) {
                throw new RuntimeException(e);
              }

              return 1;
            }))
          .then(Commands.literal("join")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player player)) return false;
              return games.stream().noneMatch(data -> (data.host().equals(player) && data.gClass().equals(gameType)) || data.game().getPlayers().contains(player));
            })
            .then(Commands.argument("id", StringArgumentType.word())
              .suggests((provider, builder) -> {
                games.stream().filter(data -> data.gClass.equals(gameType)).forEach(game -> {
                  builder.suggest(game.id, () -> "Host: " + game.host().getName());
                });
                return builder.buildFuture();
              })
              .executes(ctx -> {
                Entity executor = ctx.getSource().getExecutor();
                if (!(executor instanceof Player player)) return 1;
                String id = ctx.getArgument("id", String.class);
                RGame data = games.stream().filter(gdata -> gdata.game().getId().equals(id)).findAny().orElse(null);
                if(data == null) {
                  player.sendMessage("There is no game with that id");
                  return 1;
                }

                data.game().addPlayer(player);
                player.updateCommands();
                return 1;
              }))
          )
          .then(Commands.literal("setSpawn")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player host)) return false;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).findAny();
              return optional.map(data -> data.game().hasCommand("setSpawn")).orElse(false);
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if (!(executor instanceof Player host)) return 1;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
              return optional.map(rGame -> rGame.game().runCommand("setSpawn", ctx)).orElse(1);
            }))
          .then(Commands.literal("setArea")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player host)) return false;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).findAny();
              return optional.map(data -> data.game().hasCommand("setArea")).orElse(false);
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if (!(executor instanceof Player host)) return 1;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
              return optional.map(rGame -> rGame.game().runCommand("setArea", ctx)).orElse(1);
            }))
          .then(Commands.literal("start")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if(!(executor instanceof Player host)) return false;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType) && !data.game().started()).findAny();
              return optional.isPresent();
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if(!(executor instanceof Player host)) return 1;

              RGame gameData = this.getGame(host, gameType);
              gameData.game().start();
              return 1;
            })
          )
          .then(Commands.literal("test") // Shows that since no games have test defined as a command it doesn't show up
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player host)) return false;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).findAny();
              return optional.map(data -> data.game().hasCommand("test")).orElse(false);
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if (!(executor instanceof Player host)) return 1;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
              return optional.map(rGame -> rGame.game().runCommand("test", ctx)).orElse(1);
            }))
        );
    }

    this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
      commands.registrar().register(root.build());
    });
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

  public RGame getGame(Player host, Class<? extends Game> clazz) { // This is a terrible function and I need to figure out a better way of getting games
    return games.stream().filter(data -> data.host.equals(host) && data.gClass.equals(clazz)).toList().getFirst();
  }

  public void addGame(Game game) {
    Bukkit.getPluginManager().registerEvents(game, this);
    games.add(new RGame(game.getId(), game.getHost(), game, game.getClass()));
  }

  public static void removeGame(Game game) {
    games.remove(games.stream().filter(data -> data.game().equals(game)).toList().getFirst());
    game.getPlayers().forEach(Player::updateCommands);
  }

  public record RGame(String id, Player host, Game game, Class<? extends Game> gClass) { }
}
