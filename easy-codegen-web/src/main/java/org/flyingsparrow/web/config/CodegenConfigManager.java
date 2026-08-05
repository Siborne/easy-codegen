package org.flyingsparrow.web.config;

import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 代码生成配置管理：负责加载/保存配置文件并刷新核心模块运行时配置
 *
 * @author Siborne
 */
@Component
public class CodegenConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(CodegenConfigManager.class);

    @Value("${codegen.config-file}")
    private String configFilePath;

    private File configFile;

    @PostConstruct
    public void init() {
        configFile = new File(configFilePath);
        try {
            configFile = configFile.getCanonicalFile();
        } catch (IOException e) {
            logger.warn("解析配置文件路径失败，使用原始路径: {}", configFilePath, e);
        }
        try {
            LinkedHashMap<String, String> map = ConfigFileStore.load(configFile);
            if (!map.isEmpty()) {
                PropertiesUtils.reload(toProperties(map));
                Constants.reload(PropertiesUtils.getProperties());
                logger.info("已加载配置文件: {}", configFile.getAbsolutePath());
            } else {
                logger.info("配置文件不存在或为空，使用默认配置: {}", configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("加载配置文件失败: {}", configFile.getAbsolutePath(), e);
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    /**
     * 读取当前配置文件内容
     */
    public LinkedHashMap<String, String> loadConfig() throws IOException {
        return ConfigFileStore.load(configFile);
    }

    /**
     * 保存配置并刷新运行时生效
     */
    public synchronized void saveConfig(Map<String, String> config) throws IOException {
        ConfigFileStore.save(configFile, config);
        PropertiesUtils.reload(toProperties(config));
        Constants.reload(PropertiesUtils.getProperties());
        logger.info("配置已保存并生效: {}", configFile.getAbsolutePath());
    }

    private Properties toProperties(Map<String, String> map) {
        Properties props = new Properties();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return props;
    }
}
