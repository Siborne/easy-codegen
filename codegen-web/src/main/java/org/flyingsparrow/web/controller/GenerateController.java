package org.flyingsparrow.web.controller;

import org.flyingsparrow.builder.BuildTable;
import org.flyingsparrow.builder.GenerateEngine;
import org.flyingsparrow.builder.GenerateResult;
import org.flyingsparrow.bean.TableInfo;
import org.flyingsparrow.utils.PropertiesUtils;
import org.flyingsparrow.web.config.CodegenConfigManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成相关 API：测试连接、加载表列表、一键生成
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
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new LinkedHashMap<>();
        String error = BuildTable.testConnection(PropertiesUtils.getProperties());
        result.put("success", error == null);
        result.put("message", error == null ? "连接成功" : "连接失败: " + error);
        return result;
    }

    /**
     * 加载表列表（使用当前生效配置）
     */
    @PostMapping("/tables")
    public Map<String, Object> listTables() {
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
            result.put("message", "加载表列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 一键生成：可携带完整配置（自动保存生效）与勾选的表名；表名为空表示全表
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody(required = false) GenerateRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (request != null && request.getConfig() != null && !request.getConfig().isEmpty()) {
                configManager.saveConfig(request.getConfig());
            }
            List<String> tables = request == null ? null : request.getTables();
            GenerateResult generateResult = GenerateEngine.generate(PropertiesUtils.getProperties(), tables);
            result.put("success", generateResult.isSuccess());
            if (generateResult.isSuccess()) {
                result.put("files", generateResult.getFiles());
                result.put("message", "生成成功，共 " + generateResult.getFiles().size() + " 个文件");
            } else {
                result.put("message", generateResult.getError());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        }
        return result;
    }

    public static class GenerateRequest {
        private Map<String, String> config;
        private List<String> tables;

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
    }
}
