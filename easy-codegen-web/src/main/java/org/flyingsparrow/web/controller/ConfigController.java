package org.flyingsparrow.web.controller;

import org.flyingsparrow.web.config.CodegenConfigManager;
import org.flyingsparrow.web.util.I18nMessages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public Map<String, Object> getConfig(
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("success", true);
            result.put("data", configManager.loadConfig());
            result.put("file", configManager.getConfigFile().getAbsolutePath());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", I18nMessages.get(lang, "config.read.error", e.getMessage()));
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> saveConfig(
            @RequestBody Map<String, String> config,
            @RequestHeader(value = "X-Lang", required = false, defaultValue = "zh") String lang) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            configManager.saveConfig(config);
            result.put("success", true);
            result.put("message", I18nMessages.get(lang, "config.save.success"));
            result.put("file", configManager.getConfigFile().getAbsolutePath());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", I18nMessages.get(lang, "config.save.error", e.getMessage()));
        }
        return result;
    }
}
