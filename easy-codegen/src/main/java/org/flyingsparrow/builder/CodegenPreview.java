package org.flyingsparrow.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码预览与 zip 打包
 *
 * @author Siborne
 */
public class CodegenPreview {

    private static final Logger logger = LoggerFactory.getLogger(CodegenPreview.class);

    private CodegenPreview() {
    }

    /**
     * 预览：生成指定表与类型到临时目录，返回每个文件的相对路径与内容（不写真实输出）
     */
    public static List<PreviewFile> preview(Properties props, Collection<String> tableNames, Collection<String> types) {
        List<GenerateToTemp.TempFile> tempFiles = GenerateToTemp.generateToTempDir(props, tableNames, types);
        try {
            List<PreviewFile> result = new ArrayList<>();
            for (GenerateToTemp.TempFile tf : tempFiles) {
                result.add(new PreviewFile(tf.relativePath, GenerateToTemp.readContent(tf.file)));
            }
            return result;
        } finally {
            // 清理临时目录（取第一个文件的父目录向上回溯）
            if (!tempFiles.isEmpty()) {
                File dir = tempFiles.get(0).file.getParentFile();
                while (dir != null && dir.getName().startsWith("easy-codegen-tmp")) {
                    GenerateToTemp.deleteRecursively(dir);
                    break;
                }
            }
        }
    }

    /**
     * 将生成结果打包为 zip 字节数组
     */
    public static byte[] buildZip(Properties props, Collection<String> tableNames, Collection<String> types) {
        List<GenerateToTemp.TempFile> tempFiles = GenerateToTemp.generateToTempDir(props, tableNames, types);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);
            for (GenerateToTemp.TempFile tf : tempFiles) {
                ZipEntry entry = new ZipEntry(tf.relativePath);
                zos.putNextEntry(entry);
                zos.write(GenerateToTemp.readContent(tf.file).getBytes("UTF-8"));
                zos.closeEntry();
            }
            zos.finish();
            return bos.toByteArray();
        } catch (Exception e) {
            logger.error("打包 zip 失败", e);
            throw new RuntimeException("打包 zip 失败: " + e.getMessage(), e);
        } finally {
            if (!tempFiles.isEmpty()) {
                File dir = tempFiles.get(0).file.getParentFile();
                while (dir != null && dir.getName().startsWith("easy-codegen-tmp")) {
                    GenerateToTemp.deleteRecursively(dir);
                    break;
                }
            }
        }
    }
}
