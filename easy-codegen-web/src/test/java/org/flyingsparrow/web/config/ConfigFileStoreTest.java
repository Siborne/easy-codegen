package org.flyingsparrow.web.config;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConfigFileStore 行尾保持与合并逻辑测试
 */
class ConfigFileStoreTest {

    @Test
    void savePreservesLfLineSeparator() throws Exception {
        File f = File.createTempFile("cfg-lf", ".properties");
        try {
            Files.write(f.toPath(), "a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));
            Map<String, String> updates = new HashMap<>();
            updates.put("b", "22");
            ConfigFileStore.save(f, updates);

            byte[] bytes = Files.readAllBytes(f.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals("a=1\nb=22\n", content, "值应被更新且其余行原样保留");
            assertEquals(0, countCrlf(bytes), "LF 文件应保持 LF 行尾，不得被改为 CRLF");
        } finally {
            f.delete();
        }
    }

    @Test
    void savePreservesCrlfLineSeparator() throws Exception {
        File f = File.createTempFile("cfg-crlf", ".properties");
        try {
            Files.write(f.toPath(), "a=1\r\nb=2\r\n".getBytes(StandardCharsets.UTF_8));
            Map<String, String> updates = new HashMap<>();
            updates.put("a", "11");
            ConfigFileStore.save(f, updates);

            byte[] bytes = Files.readAllBytes(f.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertEquals("a=11\r\nb=2\r\n", content, "值应被更新且其余行原样保留");
            assertEquals(2, countCrlf(bytes), "CRLF 文件应保持 CRLF 行尾");
        } finally {
            f.delete();
        }
    }

    @Test
    void saveKeepsCommentsAndOrder() throws Exception {
        File f = File.createTempFile("cfg-comment", ".properties");
        try {
            Files.write(f.toPath(), "# 注释\nk1=v1\n# 中间注释\nk2=v2\n".getBytes(StandardCharsets.UTF_8));
            Map<String, String> updates = new HashMap<>();
            updates.put("k2", "new2");
            updates.put("k3", "new3");
            ConfigFileStore.save(f, updates);

            String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            int idxComment = content.indexOf("# 注释");
            int idxK1 = content.indexOf("k1=v1");
            int idxMid = content.indexOf("# 中间注释");
            int idxK2 = content.indexOf("k2=new2");
            int idxK3 = content.indexOf("k3=new3");
            assertTrue(idxComment >= 0 && idxK1 > idxComment && idxMid > idxK1
                    && idxK2 > idxMid && idxK3 > idxK2, "注释与顺序应保留，新 key 追加末尾");
        } finally {
            f.delete();
        }
    }

    private int countCrlf(byte[] bytes) {
        int n = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\n' && i > 0 && bytes[i - 1] == '\r') {
                n++;
            }
        }
        return n;
    }
}
