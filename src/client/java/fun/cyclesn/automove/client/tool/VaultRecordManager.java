package fun.cyclesn.automove.client.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class VaultRecordManager {
    // 存储已开启宝库的绝对坐标集合
    private static Set<String> openedVaultPositions = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // 使用 TypeToken<Set<String>> 的正确方式
    private static final Type SET_TYPE = new TypeToken<Set<String>>() {}.getType();

    // 存储当前世界 ID，用于确定读写哪个文件
    private static Identifier currentWorldId = null;

    /**
     * 获取当前世界记录文件的路径。
     */
    private static Path getRecordFilePath() {
        if (currentWorldId == null) {
            // 如果世界ID为空，返回一个默认/通用的路径，但在加载/保存时应避免这种情况
            return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("automove_vault_records").resolve("default_world.json");
        }

        Path configDir = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("automove_vault_records");

        try {
            // 使用 Files.createDirectories 确保目录存在，并健壮地处理异常
            Files.createDirectories(configDir);
        } catch (Exception e) {
            LOGGER.error("无法创建配置目录: {}", configDir, e);
        }

        // 使用世界维度 ID 作为文件名的一部分 (例如：minecraft_overworld.json)
        String fileName = currentWorldId.getPath().replace('/', '_') + ".json";
        return configDir.resolve(fileName);
    }

    /**
     * 加载当前世界的记录。
     * 必须在知道当前世界是哪一个（例如在 ClientLifecycleEvents.CLIENT_WORLD_LOAD 事件中）时调用。
     */
    public static void loadRecords(ClientWorld world) {
        currentWorldId = world.getRegistryKey().getValue();
        Path file = getRecordFilePath();
        openedVaultPositions.clear(); // 清空旧世界的记录

        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                Set<String> loaded = GSON.fromJson(reader, SET_TYPE);
                if (loaded != null) {
                    openedVaultPositions = loaded;
                }
                LOGGER.info("已加载世界 [{}] 的 {} 条宝库记录。", currentWorldId, openedVaultPositions.size());
            } catch (Exception e) {
                LOGGER.error("无法加载世界 [{}] 的宝库记录。", currentWorldId, e);
            }
        }
    }

    /**
     * 保存当前世界的记录。
     */
    public static void saveRecords() {
        if (currentWorldId == null) {
            LOGGER.warn("无法保存宝库记录：未知的世界 ID。");
            return;
        }
        Path file = getRecordFilePath();

        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(openedVaultPositions, writer);
            // LOGGER.info("已保存 " + openedVaultPositions.size() + " 条宝库记录到 JSON。");
        } catch (Exception e) {
            LOGGER.error("无法保存宝库记录到 JSON 文件: {}", file, e);
        }
    }

    /**
     * 标记一个宝库为已开启。
     */
    public static void markAsOpened(BlockPos pos) {
        String posString = pos.toImmutable().toString();
        if (openedVaultPositions.add(posString)) {
            saveRecords(); // 每次添加新记录后保存
        }
    }

    /**
     * 检查宝库是否已被本地记录。
     */
    public static boolean isOpened(BlockPos pos) {
        LOGGER.info(openedVaultPositions.toString());
        return openedVaultPositions.contains(pos.toImmutable().toString());
    }
}
