package fun.cyclesn.automove.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class AutoMoveConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/automove.json");

    public boolean enabled = false;
    public boolean jumpEnabled = false;
    public boolean autoEat = false;
    public boolean autoSword = false;
    //    寻找试炼大厅
    public boolean findChamber = false;
    //    高亮宝库
    public boolean highlightTreasure = true;
    public boolean showHighLight = false;
    public String apiKey = ""; // 默认值为空字符串
    public String apiUrl = "https://api.deepseek.com/";
    public String model = "deepseek-chat";
    public int AI_MAX_HISTORY = 6;
    //    是否开启寻找实体
    public boolean findEntity = false;
    //    实体寻找力度
    public int findStrength = 10;
    public String findEntityName = "";
//    嵌入模型
    public String EmbeddingUrl = "";
    public String EmbeddingKey = "";
    public String EmbeddingModel = "BAAI/bge-m3";

    private AutoMoveConfig() {
    }

    public static AutoMoveConfig INSTANCE;

    public static AutoMoveConfig load() {
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
            try (FileWriter writer = new FileWriter(FILE)) {
                gson.toJson(this, writer);
            }
            LOGGER.info("保存配置文件成功");
        } catch (Exception e) {
            LOGGER.error("保存配置文件失败{}", e.getMessage());
        }
    }
}
