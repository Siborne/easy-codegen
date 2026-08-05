package org.flyingsparrow.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次代码生成的结果
 *
 * @author Siborne
 */
public class GenerateResult {

    private boolean success;

    private List<String> files;

    private String error;

    public static GenerateResult success(List<String> files) {
        GenerateResult result = new GenerateResult();
        result.success = true;
        result.files = files == null ? new ArrayList<String>() : files;
        return result;
    }

    public static GenerateResult fail(String error) {
        GenerateResult result = new GenerateResult();
        result.success = false;
        result.error = error;
        result.files = new ArrayList<String>();
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getError() {
        return error;
    }
}
