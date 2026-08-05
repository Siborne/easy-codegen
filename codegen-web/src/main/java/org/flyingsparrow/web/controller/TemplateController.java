package org.flyingsparrow.web.controller;

import org.flyingsparrow.web.config.ConfigFileStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置模板 API：保存/加载/删除多套生成配置
 *
 * @author Siborne
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Value("${codegen.template-dir}")
    private String templateDir;

    private File getDir() {
        File dir = new File(templateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private File templateFile(String name) {
        return new File(getDir(), name + ".properties");
    }

    /**
     * 列出所有模板名
     */
    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        File[] files = getDir().listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".properties")) {
                    names.add(f.getName().substring(0, f.getName().length() - ".properties".length()));
                }
            }
        }
        result.put("success", true);
        result.put("templates", names);
        return result;
    }

    /**
     * 保存当前配置为模板
     */
    @PostMapping("/{name}")
    public Map<String, Object> save(@PathVariable String name, @RequestBody Map<String, String> config) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (name == null || name.trim().isEmpty() || name.contains("/") || name.contains("\\") || name.contains("..")) {
            result.put("success", false);
            result.put("message", "模板名不合法");
            return result;
        }
        try {
            ConfigFileStore.save(templateFile(name.trim()), config);
            result.put("success", true);
            result.put("message", "模板已保存: " + name.trim());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "保存模板失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 读取模板内容
     */
    @GetMapping("/{name}")
    public Map<String, Object> load(@PathVariable String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        File file = templateFile(name);
        if (!file.exists()) {
            result.put("success", false);
            result.put("message", "模板不存在: " + name);
            return result;
        }
        try {
            result.put("success", true);
            result.put("data", ConfigFileStore.load(file));
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取模板失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{name}")
    public Map<String, Object> delete(@PathVariable String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        File file = templateFile(name);
        if (!file.exists()) {
            result.put("success", false);
            result.put("message", "模板不存在: " + name);
            return result;
        }
        if (file.delete()) {
            result.put("success", true);
            result.put("message", "模板已删除: " + name);
        } else {
            result.put("success", false);
            result.put("message", "删除模板失败");
        }
        return result;
    }
}
