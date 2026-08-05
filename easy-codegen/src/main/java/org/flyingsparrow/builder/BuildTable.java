package org.flyingsparrow.builder;


import org.apache.commons.lang3.ArrayUtils;
import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.bean.FieldInfo;
import org.flyingsparrow.bean.TableInfo;
import org.flyingsparrow.utils.PropertiesUtils;
import org.flyingsparrow.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 获取数据库表结构
 *
 * @author Siborne
 */
public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);

    private static String SQL_SHOW_TABLE_STATUS = "show table status";

    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";

    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";

    /**
     * 使用当前生效配置获取全部表
     */
    public static List<TableInfo> getTables() {
        return getTables(PropertiesUtils.getProperties(), null);
    }

    /**
     * 使用指定配置获取表结构；tableNames 为空时返回全部表
     *
     * @param props      数据库连接等配置
     * @param tableNames 需要生成的表名集合，null/空 表示全部表
     */
    public static List<TableInfo> getTables(Properties props, Collection<String> tableNames) {
        Connection conn = null;
        try {
            conn = openConnection(props);
        } catch (Exception e) {
            logger.error("数据库连接失败", e);
            return new ArrayList<>();
        }

        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while (tableResult.next()) {
                String tableName = tableResult.getString("Name");
                // 表名合法性校验：仅允许常规标识符（表名来自数据库元数据，防御性校验）
                if (tableName == null || !tableName.matches("[A-Za-z0-9_$]+")) {
                    continue;
                }
                if (tableNames != null && !tableNames.isEmpty() && !tableNames.contains(tableName)) {
                    continue;
                }
                String comment = tableResult.getString("Comment");

                String beanName = tableName;
                if (Constants.IGNORE_TABLE_PERFIX) {
                    beanName = tableName.substring(beanName.indexOf("_") + 1);
                }
                beanName = processFiled(beanName, true);

                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanQueryName(beanName + Constants.SUFFIX_BEAN_QUERY);

                readFiledInfo(conn, tableInfo);

                getKeyIndexInfo(conn, tableInfo);

                tableInfoList.add(tableInfo);
            }
        } catch (Exception e) {
            logger.error("读取表失败", e);
        } finally {
            if (tableResult != null) {
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableInfoList;
    }

    /**
     * 测试数据库连接，成功返回 null，失败返回错误信息
     */
    public static String testConnection(Properties props) {
        Connection conn = null;
        try {
            conn = openConnection(props);
            return null;
        } catch (Exception e) {
            logger.error("数据库连接失败", e);
            return e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Connection openConnection(Properties props) throws Exception {
        String driverName = props.getProperty("db.driver.name");
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        Class.forName(driverName);
        return DriverManager.getConnection(url, username, password);
    }

    private static void readFiledInfo(Connection conn, TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<>();

        List<FieldInfo> extendFieldInfoList = new ArrayList<>();

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();

            Boolean haveDateTime = false;
            Boolean haveDate = false;
            Boolean haveBigDecimal = false;

            while (fieldResult.next()) {
                String field = fieldResult.getString("Field");
                String type = fieldResult.getString("Type");
                String extra = fieldResult.getString("Extra");
                String comment = fieldResult.getString("Comment");
                if (type.indexOf("(") > 0) {
                    type = type.substring(0, type.indexOf("("));
                }
                String propertyName = processFiled(field, false);

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                fieldInfo.setComment(comment);
                fieldInfo.setSqlType(type);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
                    haveDateTime = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {
                    haveDate = true;
                }
                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
                    haveBigDecimal = true;
                }

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
                    FieldInfo fuzzyField = new FieldInfo();
                    fuzzyField.setJavaType(fieldInfo.getJavaType());
                    fuzzyField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyField.setFieldName(fieldInfo.getFieldName());
                    fuzzyField.setComment(comment);
                    fuzzyField.setAutoIncrement(fieldInfo.isAutoIncrement());
                    fuzzyField.setSqlType(fieldInfo.getSqlType());
                    extendFieldInfoList.add(fuzzyField);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {

                    FieldInfo timesStartField = new FieldInfo();
                    timesStartField.setJavaType("String");
                    timesStartField.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    timesStartField.setFieldName(fieldInfo.getFieldName());
                    timesStartField.setComment(comment);
                    timesStartField.setAutoIncrement(fieldInfo.isAutoIncrement());
                    timesStartField.setSqlType(fieldInfo.getSqlType());
                    extendFieldInfoList.add(timesStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setJavaType("String");
                    timeEndField.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    timeEndField.setComment(comment);
                    timeEndField.setAutoIncrement(fieldInfo.isAutoIncrement());
                    timeEndField.setSqlType(fieldInfo.getSqlType());
                    extendFieldInfoList.add(timeEndField);
                }

            }
            tableInfo.setHaveDate(haveDate);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveBigDecimal(haveBigDecimal);
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setExtendFieldList(extendFieldInfoList);
        } catch (Exception e) {
            logger.error("读取字段失败", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void getKeyIndexInfo(Connection conn, TableInfo tableInfo) {
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        try {

            Map<String, FieldInfo> tempMap = new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(), fieldInfo);
            }
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX, tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            while (fieldResult.next()) {
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");
                if (nonUnique == 1) {
                    continue;
                }
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if (null == keyFieldList) {
                    keyFieldList = new ArrayList<>();
                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }
                keyFieldList.add(tempMap.get(columnName));
            }
        } catch (Exception e) {
            logger.error("读取索引失败", e);
        } finally {
            if (fieldResult != null) {
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String processFiled(String field, Boolean upperCaseFirstLetter) {
        StringBuilder sb = new StringBuilder();
        String[] fields = field.split("_");
        sb.append(upperCaseFirstLetter ? StringUtils.upperCaseFirstLetter(fields[0]) : fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(StringUtils.upperCaseFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    private static String processJavaType(String type) {
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE, type)) {
            return "Integer";
        } else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE, type)) {
            return "Long";
        } else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
            return "String";
        } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type)) {
            return "Date";
        } else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE, type)) {
            return "BigDecimal";
        } else {
            throw new RuntimeException("无法识别的类型" + type);
        }
    }

}
