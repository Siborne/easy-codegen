package org.flyingsparrow.web.controller;

import org.flyingsparrow.web.config.CodegenConfigManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置 API：读取/保存代码生成配置
 *
 * @author Siborne
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final CodegenConfigManager configManager;

    public ConfigController(CodegenConfigManager configManager) {
        this.configManager = configManager;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("success", true);
            result.put("data", configManager.loadConfig());
            result.put("file", configManager.getConfigFile().getAbsolutePath());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "读取配置失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> saveConfig(@RequestBody Map<String, String> config) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            configManager.saveConfig(config);
            result.put("success", true);
            result.put("message", "配置已保存并生效");
            result.put("file", configManager.getConfigFile().getAbsolutePath());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存配置失败: " + e.getMessage());
        }
        return result;
    }
}
