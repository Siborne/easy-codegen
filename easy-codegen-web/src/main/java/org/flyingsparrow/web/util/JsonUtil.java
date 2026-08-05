package org.flyingsparrow.web.util;

/**
 * JSON 工具
 *
 * @author Siborne
 */
public class JsonUtil {

    private JsonUtil() {
    }

    /**
     * JSON 字符串转义（RFC 8259）：处理引号/反斜杠/控制字符，防止异常消息等动态内容污染 JSON 响应
     */
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        // 其余 C0 控制符转 unicode 转义（拼接避免源码转义冲突）
                        sb.append('\\').append(String.format("u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
