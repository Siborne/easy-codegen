package org.flyingsparrow.builder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 将代码生成到临时目录，支持预览与 zip 打包（不污染真实输出路径）
 *
 * @author Siborne
 */
public class GenerateToTemp {

    private GenerateToTemp() {
    }

    /**
     * 生成结果条目
     */
    public static class TempFile {
        /** 相对临时目录的路径（zip/预览展示用，统一 / 分隔） */
        public String relativePath;
        /** 磁盘文件 */
        public File file;

        public TempFile(String relativePath, File file) {
            this.relativePath = relativePath;
            this.file = file;
        }
    }

    /**
     * 生成结果（文件列表 + 临时根目录，调用方负责清理 tmpDir）
     */
    public static class TempResult {
        public File tmpDir;
        public List<TempFile> files;

        public TempResult(File tmpDir, List<TempFile> files) {
            this.tmpDir = tmpDir;
            this.files = files;
        }
    }

    /**
     * 在临时目录执行一次生成
     *
     * @return 生成结果（含文件列表与临时根目录，调用方 finally 中 deleteRecursively(tmpDir)）
     */
    public static TempResult generateToTempDir(Properties props, Collection<String> tableNames, Collection<String> types) {
        Properties tempProps = new Properties();
        tempProps.putAll(props);

        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("easy-codegen-tmp");
        } catch (Exception e) {
            throw new RuntimeException("创建临时目录失败: " + e.getMessage(), e);
        }
        String tmpPath = tmpDir.toFile().getAbsolutePath() + File.separator;
        tempProps.setProperty("path.base", tmpPath);

        GenerateResult result;
        List<TempFile> files = new ArrayList<>();
        try {
            result = GenerateEngine.generate(tempProps, tableNames, types);
            if (result == null || !result.isSuccess()) {
                throw new RuntimeException(result == null ? "生成失败" : result.getError());
            }
            for (String absolute : result.getFiles()) {
                File file = new File(absolute);
                if (!file.exists()) {
                    continue;
                }
                String relative = absolute.substring(tmpPath.length()).replace('\\', '/');
                // 防御 zip-slip：拒绝任何含 .. 的相对路径
                if (relative.contains("..")) {
                    continue;
                }
                files.add(new TempFile(relative, file));
            }
        } catch (Throwable t) {
            // 生成/收集过程任何异常（含 Error）：清理临时目录后向上抛出，杜绝泄漏
            deleteRecursively(tmpDir.toFile());
            throw t;
        }
        return new TempResult(tmpDir.toFile(), files);
    }

    /**
     * 读取文件内容（UTF-8）
     */
    public static String readContent(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("读取生成文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 递归删除目录
     */
    public static void deleteRecursively(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteRecursively(child);
                } else {
                    child.delete();
                }
            }
        }
        dir.delete();
    }
}
