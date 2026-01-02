package com.thedevyellowy.practicalTask.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thedevyellowy.practicalTask.PracticalTask;

import java.io.File;
import java.io.FileReader;

public class ExternalLootTable {
  private static final String path = "plugins/Minigames/";
  private static final String name = "hungergames_loottable";
  private static final String extension = ".json";

  public final pools[] pools;

  public ExternalLootTable() {
    pools = new pools[]{};
  }

  public static final class pools {
    public final rolls rolls;
    public final int bonus_rolls;
    public final entries[] entries;

    public pools() {
      this.rolls = new rolls();
      this.bonus_rolls = 0;
      this.entries = new entries[]{};
    }
  }

  public static final class rolls {
    public final int max;
    public final int min;

    public rolls() {
      min = 1;
      max = -1;
    }

    public rolls(int min, int max) {
      this.min = min;
      this.max = max;
    }
  }

  public static final class count {
    public final int max;
    public final int min;

    public count() {
      min = 1;
      max = -1;
    }
  }

  public static final class entries {
    public final String type;
    public final String name;
    public final functions[] functions;
    public final conditions[] conditions;

    public entries() {
      type = "";
      name = "";
      functions = new functions[]{};
      conditions = new conditions[]{};
    }
  }

  public static final class functions {
    public final String function;
    public final int levels;
    public final count count;
    public final conditions[] conditions;

    public functions() {
      function = "";
      levels = 0;
      count = new count();
      conditions = new conditions[]{};
    }
  }

  public static final class conditions {
    public final String condition;
    public final double chance;

    public conditions() {
      condition = "";
      chance = 0;
    }
  }

  public static boolean exists() {
    File file = new File(path+name+extension);
    File folder = new File(path);
    if(!folder.exists()) folder.mkdirs();

    return file.exists();
  }

  public static ExternalLootTable load() {
    if(!ExternalLootTable.exists()) return null;
    try {
      File file = new File(path+name+extension);

      Gson gson = new GsonBuilder().registerTypeAdapter(rolls.class, new RollTypeAdapter()).create();

      return gson.fromJson(new FileReader(file), ExternalLootTable.class);
    } catch (Exception e) {
      PracticalTask.Logger.error(e);
      return null;
    }
  }
}
