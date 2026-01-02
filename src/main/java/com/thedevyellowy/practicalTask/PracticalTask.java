package com.thedevyellowy.practicalTask;

import com.thedevyellowy.practicalTask.games.Game;
import com.thedevyellowy.practicalTask.games.HideAndSeek;
import com.thedevyellowy.practicalTask.games.HungerGames;
import com.thedevyellowy.practicalTask.util.HGLootTable;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class PracticalTask extends JavaPlugin implements Listener { // Minigame plugin
  public static Logger Logger = LogManager.getLogger("PracticalTask");

  public List<RGame> games = new ArrayList<>();
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
              return games.stream().noneMatch(data -> data.host().equals(host) && data.gClass().equals(gameType));
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
          .then(Commands.literal("setSpawn")
            .requires(src -> {
              Entity executor = src.getExecutor();
              if (!(executor instanceof Player host)) return false;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host) && data.gClass().equals(gameType)).findAny();
              return optional.map(data -> {

                return data.game().hasCommand("setSpawn");
              }).orElse(false);
            })
            .executes(ctx -> {
              Entity executor = ctx.getSource().getExecutor();
              if (!(executor instanceof Player host)) return 1;
              Optional<RGame> optional = games.stream().filter(data -> data.host().equals(host)).findFirst();
              return optional.map(rGame -> rGame.game().runCommand("setSpawn", ctx)).orElse(1);
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

  // Get all classes that extend the Game class and get the name of the class for future use
  public List<Class<? extends Game>> getGameTypes() {
    Reflections reflections = new Reflections("com.thedevyellowy.practicalTask");
    Set<Class<? extends Game>> types = reflections.getSubTypesOf(Game.class);

    return new ArrayList<>(types);
  }

  public <T extends Game> void addGame(T game) {
    Bukkit.getPluginManager().registerEvents(game, this);
    games.add(new RGame(game.getId(), game.getHost(), game, game.getClass()));
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) { // Here to reference when I need to populate the Hunger Games chests
    if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      if(event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Chest chest) {
        chest.getInventory().clear();
        HGLootTable table = new HGLootTable();
        table.fillInventory(chest.getInventory(), new Random(), new LootContext.Builder(chest.getLocation()).build());
      }
    }
  }

  public record RGame(String id, Player host, Game game, Class<? extends Game> gClass) {
  }
}
