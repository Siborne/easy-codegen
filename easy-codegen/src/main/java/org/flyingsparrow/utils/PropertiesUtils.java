package org.flyingsparrow.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取配置文件信息
 * @author Siborne
 */
public class PropertiesUtils {

    private static Properties props = new Properties();
    private static Map<String, String> PROPER_MAP = new ConcurrentHashMap<>();

    static {
        try (InputStream is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");
             InputStreamReader isr = new InputStreamReader(is, "utf-8")) {
            props.load(isr);

            Iterator<Object> iterator = props.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                PROPER_MAP.put(key, props.getProperty(key));
            }

        } catch (Exception e) {

        }
    }

    public static String getString(String key) {
        return PROPER_MAP.get(key);
    }

    /**
     * 用外部配置覆盖/刷新运行时配置（供可视化面板在运行期修改配置）
     */
    public static synchronized void reload(Properties newProps) {
        PROPER_MAP.clear();
        if (newProps != null) {
            for (String key : newProps.stringPropertyNames()) {
                PROPER_MAP.put(key, newProps.getProperty(key));
            }
        }
    }

    /**
     * 返回当前生效配置的副本
     */
    public static synchronized Properties getProperties() {
        Properties copy = new Properties();
        copy.putAll(PROPER_MAP);
        return copy;
    }

    public static void main(String[] args) {
        System.out.println(getString("db.driver.name"));
    }

}
