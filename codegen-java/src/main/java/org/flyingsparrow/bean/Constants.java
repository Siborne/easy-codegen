package org.flyingsparrow.bean;

import org.flyingsparrow.utils.PropertiesUtils;

import java.util.Properties;

/**
 * 常量类
 *
 * @author Siborne
 */
public class Constants {

    public static String AUTHOR_COMMENT;

    public static Boolean IGNORE_TABLE_PERFIX;

    public static String SUFFIX_BEAN_QUERY;

    public static String SUFFIX_BEAN_QUERY_FUZZY;

    public static String SUFFIX_BEAN_QUERY_TIME_START;
    public static String SUFFIX_BEAN_QUERY_TIME_END;

    public static String SUFFIX_MAPPERS;

    //需要忽略的属性
    public static String IGNORE_BEAN_TOJSON_FIELD;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;

    //日期序列化，反序列化
    public static String BEAN_DATE_FORMAT_EXPRESSION;
    public static String BEAN_DATE_FORMAT_CLASS;
    public static String BEAN_DATE_UNFORMAT_EXPRESSION;
    public static String BEAN_DATE_UNFORMAT_CLASS;

    private static String PATH_JAVA = "java";

    private static String PATH_RESOURCES = "resources";

    public static String PACKAGE_BASE;

    public static String PACKAGE_PO;

    public static String PACKAGE_QUERY;

    public static String PACKAGE_VO;

    public static String PACKAGE_MAPPERS;

    public static String PACKAGE_UTILS;

    public static String PACKAGE_ENUMS;

    public static String PACKAGE_EXCEPTION;

    public static String PACKAGE_SERVICE;

    public static String PACKAGE_SERVICE_IMPL;

    public static String PACKAGE_CONTROLLER;

    public static String PATH_BASE;

    public static String PATH_PO;

    public static String PATH_VO;

    public static String PATH_QUERY;

    public static String PATH_UTILS;

    public static String PATH_ENUMS;

    public static String PATH_EXCEPTION;

    public static String PATH_MAPPERS;

    public static String PATH_MAPPERS_XMLS;

    public static String PATH_SERVICE;

    public static String PATH_SERVICE_IMPL;

    public static String PATH_CONTROLLER;

    static {
        // 默认从 classpath 配置加载，面板可调用 reload 热刷新
        reload(PropertiesUtils.getProperties());
    }

    /**
     * 根据传入配置刷新全部常量（供可视化面板运行期修改配置后调用）
     */
    public static void reload(Properties props) {

        AUTHOR_COMMENT = props.getProperty("author.comment");

        IGNORE_BEAN_TOJSON_FIELD = props.getProperty("ignore.bean.tojson.field");
        IGNORE_BEAN_TOJSON_EXPRESSION = props.getProperty("ignore.bean.tojson.expression");
        IGNORE_BEAN_TOJSON_CLASS = props.getProperty("ignore.bean.tojson.class");

        //日期序列化，反序列化
        BEAN_DATE_FORMAT_EXPRESSION = props.getProperty("bean.date.format.expression");
        BEAN_DATE_FORMAT_CLASS = props.getProperty("bean.date.format.class");
        BEAN_DATE_UNFORMAT_EXPRESSION = props.getProperty("bean.date.unformat.expression");
        BEAN_DATE_UNFORMAT_CLASS = props.getProperty("bean.date.unformat.class");

        IGNORE_TABLE_PERFIX = Boolean.valueOf(props.getProperty("ignore.table.prefix"));
        SUFFIX_BEAN_QUERY = props.getProperty("suffix.bean.query");

        SUFFIX_BEAN_QUERY_FUZZY = props.getProperty("suffix.bean.query.fuzzy");

        SUFFIX_BEAN_QUERY_TIME_START = props.getProperty("suffix.bean.query.time.start");
        SUFFIX_BEAN_QUERY_TIME_END = props.getProperty("suffix.bean.query.time.end");

        SUFFIX_MAPPERS = props.getProperty("suffix.mappers");

        PACKAGE_BASE = props.getProperty("package.base");
        //PO
        PACKAGE_PO = PACKAGE_BASE + "." + props.getProperty("package.po");
        PACKAGE_QUERY = PACKAGE_BASE + "." + props.getProperty("package.query");
        PACKAGE_VO = PACKAGE_BASE + "." + props.getProperty("package.vo");
        PACKAGE_UTILS = PACKAGE_BASE + "." + props.getProperty("package.utils");
        PACKAGE_ENUMS = PACKAGE_BASE + "." + props.getProperty("package.enums");
        PACKAGE_EXCEPTION = PACKAGE_BASE + "." + props.getProperty("package.exception");
        PACKAGE_MAPPERS = PACKAGE_BASE + "." + props.getProperty("package.mappers");
        PACKAGE_SERVICE = PACKAGE_BASE + "." + props.getProperty("package.service");
        PACKAGE_SERVICE_IMPL = PACKAGE_BASE + "." + props.getProperty("package.service.impl");
        PACKAGE_CONTROLLER = PACKAGE_BASE + "." + props.getProperty("package.controller");

        PATH_BASE = props.getProperty("path.base");
        PATH_BASE = PATH_BASE + PATH_JAVA;

        PATH_PO = PATH_BASE + "/" + PACKAGE_PO.replace(".", "/");
        PATH_QUERY = PATH_BASE + "/" + PACKAGE_QUERY.replace(".", "/");
        PATH_VO = PATH_BASE + "/" + PACKAGE_VO.replace(".", "/");
        PATH_UTILS = PATH_BASE + "/" + PACKAGE_UTILS.replace(".", "/");
        PATH_EXCEPTION = PATH_BASE + "/" + PACKAGE_EXCEPTION.replace(".", "/");
        PATH_ENUMS = PATH_BASE + "/" + PACKAGE_ENUMS.replace(".", "/");
        PATH_MAPPERS = PATH_BASE + "/" + PACKAGE_MAPPERS.replace(".", "/");
        PATH_SERVICE = PATH_BASE + "/" + PACKAGE_SERVICE.replace(".", "/");
        PATH_SERVICE_IMPL = PATH_BASE + "/" + PACKAGE_SERVICE_IMPL.replace(".", "/");
        PATH_CONTROLLER = PATH_BASE + "/" + PACKAGE_CONTROLLER.replace(".", "/");

        PATH_MAPPERS_XMLS = props.getProperty("path.base")  + PATH_RESOURCES + "/" + PACKAGE_MAPPERS.replace(".", "/");

    }

    public final static String[] SQL_DATE_TIME_TYPES = new String[]{"datetime", "timestamp"};

    public final static String[] SQL_DATE_TYPES = new String[]{"date"};

    public static final String[] SQL_DECIMAL_TYPE = new String[]{"decimal", "double", "float"};

    public static final String[] SQL_STRING_TYPE = new String[]{"char", "varchar", "text", "mediumtext", "longtext"};

    //Integer
    public static final String[] SQL_INTEGER_TYPE = new String[]{"int", "tinyint"};

    //Long
    public static final String[] SQL_LONG_TYPE = new String[]{"bigint"};

    public static void main(String[] args) {
        System.out.println(PACKAGE_BASE);
        System.out.println(PATH_PO);
    }

}
