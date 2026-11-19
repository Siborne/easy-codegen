package org.flyingsparrow.web.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 后端接口返回消息的极简 i18n：按 X-Lang 请求头返回中文或英文。
 * 前端 api.js 会携带当前语言，接口消息字段直接返回对应语言文本。
 *
 * @author Siborne
 */
public final class I18nMessages {

    private static final Map<String, String[]> MESSAGES = new HashMap<>();

    static {
        put("config.read.error", "读取配置失败: {0}", "Failed to read configuration: {0}");
        put("config.save.success", "配置已保存并生效", "Configuration saved");
        put("config.save.error", "保存配置失败: {0}", "Failed to save configuration: {0}");
        put("connection.success", "连接成功", "Connected");
        put("connection.error", "连接失败: {0}", "Connection failed: {0}");
        put("tables.error", "加载表列表失败: {0}", "Failed to load tables: {0}");
        put("generate.path.missing", "输出路径 path.base 未配置", "Output path (path.base) is not configured");
        put("generate.dir.create.failed", "输出目录无法创建: {0}", "Cannot create output directory: {0}");
        put("generate.dir.not.dir", "输出路径不是目录: {0}", "Output path is not a directory: {0}");
        put("generate.success", "生成成功，共 {0} 个文件", "Generated {0} files");
        put("generate.error", "生成失败: {0}", "Generation failed: {0}");
        put("preview.error", "预览失败: {0}", "Preview failed: {0}");
        put("download.error", "打包下载失败: {0}", "Failed to build zip: {0}");
        put("template.name.invalid", "模板名不合法", "Invalid template name");
        put("template.save.success", "模板已保存: {0}", "Template saved: {0}");
        put("template.save.error", "保存模板失败: {0}", "Failed to save template: {0}");
        put("template.not.found", "模板不存在: {0}", "Template not found: {0}");
        put("template.load.error", "读取模板失败: {0}", "Failed to read template: {0}");
        put("template.delete.success", "模板已删除: {0}", "Template deleted: {0}");
        put("template.delete.error", "删除模板失败", "Failed to delete template");
    }

    private I18nMessages() {
    }

    private static void put(String code, String zh, String en) {
        MESSAGES.put(code, new String[] { zh, en });
    }

    public static String get(String lang, String code, Object... args) {
        String[] pair = MESSAGES.getOrDefault(code, new String[] { code, code });
        String pattern = "en".equalsIgnoreCase(lang) ? pair[1] : pair[0];
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }
}
