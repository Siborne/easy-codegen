package org.flyingsparrow.web.config;

import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private final Environment environment;

    private File configFile;

    public CodegenConfigManager(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        configFile = new File(configFilePath);
        try {
            configFile = configFile.getCanonicalFile();
        } catch (IOException e) {
            logger.warn("解析配置文件路径失败，使用原始路径: {}", configFilePath, e);
        }
        // 仅打包分发模式（用户目录配置）需要首次运行时复制默认配置；
        // 开发模式避免误在源码目录下创建配置文件。
        if (environment.acceptsProfiles("packaged")) {
            try {
                ensureConfigFile();
            } catch (IOException e) {
                logger.warn("初始化默认配置失败: {}", configFile.getAbsolutePath(), e);
            }
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

    /**
     * 首次运行时，若配置文件不存在，则从 classpath 复制一份干净的默认配置，
     * 便于打包分发模式（用户目录）也能开箱即用。
     */
    private void ensureConfigFile() throws IOException {
        if (configFile.exists()) {
            return;
        }
        try (InputStream in = CodegenConfigManager.class.getClassLoader()
                .getResourceAsStream("default-application.properties")) {
            if (in == null) {
                return;
            }
            if (configFile.getParentFile() != null) {
                configFile.getParentFile().mkdirs();
            }
            try (OutputStream out = new FileOutputStream(configFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            }
            logger.info("已初始化默认配置: {}", configFile.getAbsolutePath());
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
     * 保存配置并刷新运行时生效；含安全校验（路径穿越/非常规 JDBC 参数）
     */
    public synchronized void saveConfig(Map<String, String> config) throws IOException {
        validateConfig(config);
        ConfigFileStore.save(configFile, config);
        // 前端只提交部分键（如 db.*），reload 必须基于合并写回后的完整配置，
        // 否则未提交的键（suffix.*/package.* 等）会被覆盖为 null，导致生成代码损坏
        LinkedHashMap<String, String> merged = ConfigFileStore.load(configFile);
        PropertiesUtils.reload(toProperties(merged));
        Constants.reload(PropertiesUtils.getProperties());
        logger.info("配置已保存并生效: {}", configFile.getAbsolutePath());
    }

    /**
     * 配置安全校验：拒绝路径穿越与非常规 JDBC 驱动/连接串
     */
    private void validateConfig(Map<String, String> config) throws IOException {
        String pathBase = config.get("path.base");
        if (pathBase != null && pathBase.contains("..")) {
            throw new IOException("path.base 不允许包含 '..'");
        }
        String packageBase = config.get("package.base");
        if (packageBase != null && packageBase.contains("..")) {
            throw new IOException("package.base 不允许包含 '..'");
        }
        String dbUrl = config.get("db.url");
        if (dbUrl != null && !dbUrl.startsWith("jdbc:mysql")) {
            throw new IOException("db.url 必须以 jdbc:mysql 开头");
        }
        String driver = config.get("db.driver.name");
        if (driver != null && !driver.startsWith("com.mysql")) {
            throw new IOException("db.driver.name 仅支持 MySQL 驱动（com.mysql.*）");
        }
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
