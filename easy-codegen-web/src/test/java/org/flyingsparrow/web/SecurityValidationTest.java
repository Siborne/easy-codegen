package org.flyingsparrow.web;

import org.flyingsparrow.builder.BuildTable;
import org.flyingsparrow.web.util.JsonUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全校验规则单测：表名校验与 JSON 转义
 */
class SecurityValidationTest {

    @Test
    void tableNameValidation() {
        // 常规表名放行
        assertTrue(BuildTable.isValidTableName("user_info"));
        assertTrue(BuildTable.isValidTableName("log-2024"));
        assertTrue(BuildTable.isValidTableName("中文表"));
        assertTrue(BuildTable.isValidTableName("tbl$1"));
        // 危险字符拒绝
        assertFalse(BuildTable.isValidTableName("a`b"));
        assertFalse(BuildTable.isValidTableName("a/b"));
        assertFalse(BuildTable.isValidTableName("a\\b"));
        assertFalse(BuildTable.isValidTableName("a\0b"));
        assertFalse(BuildTable.isValidTableName(""));
        assertFalse(BuildTable.isValidTableName(null));
    }

    @Test
    void jsonEscape() {
        assertEquals("", JsonUtil.escape(null));
        assertEquals("a\\\"b", JsonUtil.escape("a\"b"));
        assertEquals("a\\\\b", JsonUtil.escape("a\\b"));
        assertEquals("a\\nb", JsonUtil.escape("a\nb"));
        assertEquals("a\\tb", JsonUtil.escape("a\tb"));
        assertEquals("a\\bb", JsonUtil.escape("a\bb"));
        assertEquals("a\\fb", JsonUtil.escape("a\fb"));
        assertEquals("a" + '\\' + "u0001b", JsonUtil.escape("a\u0001b"));
        assertEquals("正常文本", JsonUtil.escape("正常文本"));
    }
}
