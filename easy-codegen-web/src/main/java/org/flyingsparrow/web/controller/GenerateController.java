package org.flyingsparrow.web.controller;

import org.flyingsparrow.builder.BuildTable;
import org.flyingsparrow.builder.CodegenPreview;
import org.flyingsparrow.builder.GenerateEngine;
import org.flyingsparrow.builder.GenerateResult;
import org.flyingsparrow.builder.PreviewFile;
import org.flyingsparrow.bean.TableInfo;
import org.flyingsparrow.utils.PropertiesUtils;
import org.flyingsparrow.web.config.CodegenConfigManager;
import org.flyingsparrow.web.util.I18nMessages;
import org.flyingsparrow.web.util.JsonUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成相关 API：测试连接、加载表列表、一键生成、代码预览、zip 下载
 *
 * @author Siborne
 */
@RestController
@RequestMapping("/api")
public class GenerateController {

    private final CodegenConfigManager configManager;

    public GenerateController(CodegenConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 测试数据库连接（使用当前生效配置）
     */
    @PostMapping("/connection/test")
    public Map<String, Object> testConnection(
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        String error = BuildTable.testConnection(PropertiesUtils.getProperties());
        result.put("success", error == null);
        result.put("message", error == null
                ? I18nMessages.get(lang, "connection.success")
                : I18nMessages.get(lang, "connection.error", error));
        return result;
    }

    /**
     * 加载表列表（使用当前生效配置）
     */
    @PostMapping("/tables")
    public Map<String, Object> listTables(
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<TableInfo> tableInfoList = BuildTable.getTables(PropertiesUtils.getProperties(), null);
            List<Map<String, String>> tables = new ArrayList<>();
            for (TableInfo tableInfo : tableInfoList) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("tableName", tableInfo.getTableName());
                item.put("beanName", tableInfo.getBeanName());
                item.put("comment", tableInfo.getComment());
                item.put("beanQueryName", tableInfo.getBeanQueryName());
                tables.add(item);
            }
            result.put("success", true);
            result.put("tables", tables);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", I18nMessages.get(lang, "tables.error", e.getMessage()));
        }
        return result;
    }

    /**
     * 一键生成：可携带完整配置（自动保存生效）、勾选的表名与生成类型；表名/类型为空表示全部
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(
            @RequestBody(required = false) GenerateRequest request,
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (request != null && request.getConfig() != null && !request.getConfig().isEmpty()) {
                configManager.saveConfig(request.getConfig());
            }
            String pathBase = PropertiesUtils.getString("path.base");
            if (pathBase == null || pathBase.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", I18nMessages.get(lang, "generate.path.missing"));
                return result;
            }
            File outputDir = new File(pathBase.trim());
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                result.put("success", false);
                result.put("message", I18nMessages.get(lang, "generate.dir.create.failed", outputDir.getAbsolutePath()));
                return result;
            }
            if (!outputDir.isDirectory()) {
                result.put("success", false);
                result.put("message", I18nMessages.get(lang, "generate.dir.not.dir", outputDir.getAbsolutePath()));
                return result;
            }

            List<String> tables = request == null ? null : request.getTables();
            List<String> types = request == null ? null : request.getTypes();
            GenerateResult generateResult = GenerateEngine.generate(PropertiesUtils.getProperties(), tables, types);
            result.put("success", generateResult.isSuccess());
            if (generateResult.isSuccess()) {
                result.put("files", generateResult.getFiles());
                result.put("message", I18nMessages.get(lang, "generate.success", generateResult.getFiles().size()));
            } else {
                result.put("message", I18nMessages.get(lang, "generate.error", generateResult.getError()));
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", I18nMessages.get(lang, "generate.error", e.getMessage()));
        }
        return result;
    }

    /**
     * 代码预览：返回每张表每个类型的代码内容（不写文件）
     */
    @PostMapping("/preview")
    public Map<String, Object> preview(
            @RequestBody(required = false) GenerateRequest request,
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (request != null && request.getConfig() != null && !request.getConfig().isEmpty()) {
                configManager.saveConfig(request.getConfig());
            }
            List<String> tables = request == null ? null : request.getTables();
            List<String> types = request == null ? null : request.getTypes();
            List<PreviewFile> files = CodegenPreview.preview(PropertiesUtils.getProperties(), tables, types);
            result.put("success", true);
            result.put("files", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", I18nMessages.get(lang, "preview.error", e.getMessage()));
        }
        return result;
    }

    /**
     * zip 下载：将勾选表与类型生成的代码打包下载
     */
    @PostMapping("/generate/download")
    public ResponseEntity<byte[]> download(
            @RequestBody(required = false) GenerateRequest request,
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        try {
            if (request != null && request.getConfig() != null && !request.getConfig().isEmpty()) {
                configManager.saveConfig(request.getConfig());
            }
            List<String> tables = request == null ? null : request.getTables();
            List<String> types = request == null ? null : request.getTypes();
            byte[] zipBytes = CodegenPreview.buildZip(PropertiesUtils.getProperties(), tables, types);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=easy-codegen.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (Exception e) {
            // 失败时返回错误 JSON（约定前端以 blob 读取时判断 content-type）；消息转义防响应污染
            String msg = "{\"success\":false,\"message\":\"" + JsonUtil.escape(I18nMessages.get(lang, "download.error", e.getMessage())) + "\"}";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(msg.getBytes());
        }
    }

    public static class GenerateRequest {
        private Map<String, String> config;
        private List<String> tables;
        private List<String> types;

        public Map<String, String> getConfig() {
            return config;
        }

        public void setConfig(Map<String, String> config) {
            this.config = config;
        }

        public List<String> getTables() {
            return tables;
        }

        public void setTables(List<String> tables) {
            this.tables = tables;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }
}
