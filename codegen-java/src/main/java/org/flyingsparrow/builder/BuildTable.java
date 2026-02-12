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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取数据库表结构
 *
 * @author Siborne
 */
public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn = null;

    private static String SQL_SHOW_TABLE_STATUS = "show table status";

    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";

    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";

    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String username = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            logger.error("数据库链接失败", e);
        }
    }

    public static List<TableInfo> getTables() {
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<>();
        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while (tableResult.next()) {
                String tableName = tableResult.getString("Name");
                String comment = tableResult.getString("Comment");
//                logger.info("tableName:{},comment:{}", tableName, comment);

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

                readFiledInfo(tableInfo);

                getKeyIndexInfo(tableInfo);

//                logger.info("tableInfo:{}", JsonUtils.convertObj2Json(tableInfo));
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

    private static void readFiledInfo(TableInfo tableInfo) {
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

    private static void getKeyIndexInfo(TableInfo tableInfo) {
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
                /*for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                    if (fieldInfo.getFieldName().equals(columnName)) {
                        keyFieldList.add(fieldInfo);
                    }
                }*/
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
