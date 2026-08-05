package org.flyingsparrow.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 收集代码生成过程中写出的文件路径，供可视化面板展示生成日志
 *
 * @author Siborne
 */
public class GenerateResultCollector {

    private static final List<String> GENERATED_FILES = new ArrayList<>();

    private GenerateResultCollector() {
    }

    public static synchronized void add(String filePath) {
        if (filePath != null) {
            GENERATED_FILES.add(filePath);
        }
    }

    /**
     * 取出全部生成文件并清空
     */
    public static synchronized List<String> getAndClear() {
        List<String> result = new ArrayList<>(GENERATED_FILES);
        GENERATED_FILES.clear();
        return result;
    }

    public static synchronized void clear() {
        GENERATED_FILES.clear();
    }
}
