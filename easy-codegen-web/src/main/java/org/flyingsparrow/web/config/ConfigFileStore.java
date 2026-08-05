package org.flyingsparrow.web.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 读写 properties 配置文件：保留注释与原顺序，仅更新/追加键值
 *
 * @author Siborne
 */
public class ConfigFileStore {

    private ConfigFileStore() {
    }

    /**
     * 读取配置文件（UTF-8），返回有序键值
     */
    public static LinkedHashMap<String, String> load(File file) throws IOException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (file == null || !file.exists()) {
            return map;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx < 0) {
                    idx = line.indexOf(':');
                }
                if (idx < 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                map.put(key, value);
            }
        } finally {
            reader.close();
        }
        return map;
    }

    /**
     * 将新配置合并写回文件：已有 key 原位更新值，新 key 追加到末尾，注释与顺序保留，行尾符与原文保持一致
     */
    public static void save(File file, Map<String, String> newValues) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        String lineSeparator = detectLineSeparator(file);

        LinkedHashMap<String, String> current = load(file);
        StringBuilder sb = new StringBuilder();

        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    boolean replaced = false;
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!")) {
                        int idx = trimmed.indexOf('=');
                        if (idx < 0) {
                            idx = trimmed.indexOf(':');
                        }
                        if (idx >= 0) {
                            String key = trimmed.substring(0, idx).trim();
                            if (newValues.containsKey(key)) {
                                sb.append(key).append("=").append(newValues.get(key));
                                current.put(key, newValues.get(key));
                                replaced = true;
                            }
                        }
                    }
                    if (!replaced) {
                        sb.append(line);
                    }
                    sb.append(lineSeparator);
                }
            } finally {
                reader.close();
            }
        }

        for (Map.Entry<String, String> entry : newValues.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(lineSeparator);
            }
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
        try {
            writer.write(sb.toString());
            writer.flush();
        } finally {
            writer.close();
        }
    }

    /**
     * 检测文件行尾符：多数出现的 \r\n 或 \n；文件不存在时用系统默认
     */
    private static String detectLineSeparator(File file) {
        if (file == null || !file.exists()) {
            return System.lineSeparator();
        }
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            int crlf = 0;
            int lf = 0;
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == '\n') {
                    if (i > 0 && bytes[i - 1] == '\r') {
                        crlf++;
                    } else {
                        lf++;
                    }
                }
            }
            return crlf >= lf && crlf > 0 ? "\r\n" : "\n";
        } catch (Exception e) {
            return System.lineSeparator();
        }
    }
}
