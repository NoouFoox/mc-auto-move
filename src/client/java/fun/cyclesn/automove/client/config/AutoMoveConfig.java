package fun.cyclesn.automove.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class AutoMoveConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/automove.json");

    public boolean enabled = false;
    public boolean fishEnabled = true;
    public boolean jumpEnabled = false;
    public boolean autoEat = false ;
    public boolean autoSword = false;

    private AutoMoveConfig() {
    }

    public static AutoMoveConfig INSTANCE = new AutoMoveConfig();

    private static AutoMoveConfig load() {
        try {
            if (FILE.exists()) {
                return gson.fromJson(new FileReader(FILE), AutoMoveConfig.class);
            }
        } catch (Exception ignored) {
        }
        return new AutoMoveConfig();
    }

    public void save() {
        try {
            FILE.getParentFile().mkdirs();
            gson.toJson(this, new FileWriter(FILE));
        } catch (Exception ignored) {
        }
    }
}
