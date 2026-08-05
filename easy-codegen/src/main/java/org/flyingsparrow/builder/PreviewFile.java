package org.flyingsparrow.builder;

/**
 * 单文件预览/下载条目：相对路径 + 内容
 *
 * @author Siborne
 */
public class PreviewFile {

    private String name;

    private String content;

    public PreviewFile() {
    }

    public PreviewFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
