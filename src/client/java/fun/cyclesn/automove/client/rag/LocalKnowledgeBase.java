package fun.cyclesn.automove.client.rag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fun.cyclesn.automove.client.AutomoveClient.LOGGER;

public class LocalKnowledgeBase {

    private final String csvFile;
    private final List<Entry> entries;

    public LocalKnowledgeBase(String csvFile) throws IOException {
        this.csvFile = csvFile;
        this.entries = new ArrayList<>();

        // 初始化文件，如果不存在就创建并写入表头
        File f = new File(csvFile);
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), StandardCharsets.UTF_8))) {
                bw.write("id,\"text\",embedding\n");
            }
        }
    }

    public static class Entry {
        public String text;
        public float[] embedding;

        public Entry(String text, float[] embedding) {
            this.text = text;
            this.embedding = embedding;
        }
    }

    /**
     * 添加条目
     */
    public void add(String text, float[] embedding) {
        entries.add(new Entry(text, embedding));
    }

    /**
     * 保存到 CSV 文件（覆盖原文件）
     */
    public void save() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {
            bw.write("id,\"text\",embedding\n");
            for (int i = 0; i < entries.size(); i++) {
                Entry e = entries.get(i);

                // 转义文本里的双引号
                String text = e.text.replace("\"", "\"\"");

                // 手动拼接 float[] 为字符串
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < e.embedding.length; j++) {
                    sb.append(e.embedding[j]);
                    if (j < e.embedding.length - 1) sb.append(";");
                }

                bw.write(i + ",\"" + text + "\"," + sb.toString() + "\n");
            }
        }
    }

    /**
     * 从 CSV 加载
     */
    public void load() throws IOException {
        entries.clear();
        File f = new File(csvFile);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                // 正则分割 CSV，支持引号内逗号
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 3);
                if (parts.length < 3) continue;

                // 处理引号和双引号
                String text = parts[1];
                if (text.startsWith("\"") && text.endsWith("\"")) {
                    text = text.substring(1, text.length() - 1).replace("\"\"", "\"");
                }

                // 解析 embedding
                String[] embStr = parts[2].split(";");
                float[] emb = new float[embStr.length];
                for (int i = 0; i < embStr.length; i++) {
                    emb[i] = Float.parseFloat(embStr[i]);
                }

                entries.add(new Entry(text, emb));
            }
        }
    }

    /**
     * 基于余弦相似度匹配最近条目
     */
    public Entry search(float[] query) {
        LOGGER.info(Arrays.toString(query));
        Entry best = null;
        double bestScore = -1;
        for (Entry e : entries) {
            double sim = cosineSimilarity(query, e.embedding);
            if (sim > bestScore) {
                bestScore = sim;
                best = e;
            }
        }
        return best;
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 测试
     */
    public static void main(String[] args) throws IOException {
        LocalKnowledgeBase kb = new LocalKnowledgeBase("knowledge.csv");

        // 添加条目
        kb.add("你好，世界！", new float[]{0.1f, 0.2f, 0.3f});
        kb.add("再见！", new float[]{0.2f, 0.1f, 0.4f});

        // 保存 CSV
        kb.save();

        // 加载 CSV
        kb.load();

        // 测试匹配
        float[] query = new float[]{0.1f, 0.2f, 0.25f};
        Entry result = kb.search(query);
        System.out.println("Best match: " + result.text);
    }
}
