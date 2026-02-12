package org.flyingsparrow.builder;

import org.apache.commons.lang3.ArrayUtils;
import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.bean.FieldInfo;
import org.flyingsparrow.bean.TableInfo;
import org.flyingsparrow.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BuildMapperXml {

    private static final Logger logger = LoggerFactory.getLogger(BuildMapperXml.class);

    private static final String BASE_COLUMN_LIST = "base_column_list";

    private static final String BASE_QUERY_CONDITION = "base_query_condition";

    private static final String BASE_QUERY_CONDITION_EXTEND = "base_query_condition_extend";

    private static final String QUERY_CONDITION = "query_condition";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPERS_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPERS;
        File mapperXmlFile = new File(folder, className + ".xml");

        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;
        try {
            out = new FileOutputStream(mapperXmlFile);
            outw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(outw);

            //构建MapperXml头部信息
            buildMapperXmlHeader(bw);

            //构建MapperXml内容
            buildMapperXmlBody(tableInfo, bw, className);

            bw.flush();
        } catch (Exception e) {
            logger.error("创建mappers XML失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void buildMapperXmlBody(TableInfo tableInfo, BufferedWriter bw, String className) throws IOException {
        bw.write("<mapper namespace=\"" + Constants.PACKAGE_MAPPERS + "." + className + "\">");
        bw.newLine();
        String poClass = Constants.PACKAGE_PO + "." + tableInfo.getBeanName();

        //实体映射
        buildBaseResultMap(tableInfo, bw, poClass);

        //通用查询列
        buildBaseColumnList(tableInfo, bw);

        //基础查询条件
        buildBaseQueryCondition(tableInfo, bw);

        //拓展的查询条件
        buildBaseQueryConditionExtend(tableInfo, bw);

        //通用查询条件
        buildQueryCondition(bw);

        //查询列表
        buildSelectList(tableInfo, bw);

        //查询数量
        buildSelectCount(tableInfo, bw);

        //单条插入
        buildInsert(tableInfo, bw, poClass);

        //插入或者更新
        buildInsertOrUpdate(tableInfo, bw, poClass);

        //添加(批量插入)
        buildInsertBatch(tableInfo, bw, poClass);

        //批量插入或更新
        buildInsertOrUpdateBatch(tableInfo, bw, poClass);

        //根据主键查询、更新和删除
        buildSelectAndUpdateAndDelete(tableInfo, bw, poClass);

        bw.newLine();
        bw.write("</mapper>");
        bw.newLine();
    }

    private static void buildInsertOrUpdateBatch(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        bw.newLine();
        bw.write("\t<!--批量插入或更新-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertOrUpdateBatch\" parameterType=\"" + poClass + "\">");
        bw.newLine();
        StringBuffer insertFieldBuffer = new StringBuffer();
        StringBuffer insertPropertyBuffer = new StringBuffer();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.isAutoIncrement()) {
                continue;
            }
            insertFieldBuffer.append(fieldInfo.getFieldName()).append(",");
            insertPropertyBuffer.append("#{item." + fieldInfo.getPropertyName() + "}").append(",");
        }
        String insertFieldBufferStr = insertFieldBuffer.substring(0, insertFieldBuffer.lastIndexOf(","));
        bw.write("\t\tINSERT INTO " + tableInfo.getTableName() + "(" + insertFieldBufferStr + ")values");
        bw.newLine();
        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
        bw.newLine();
        String insertPropertyBufferStr = insertPropertyBuffer.substring(0, insertPropertyBuffer.lastIndexOf(","));
        bw.write("\t\t\t(" + insertPropertyBufferStr + ")");
        bw.newLine();
        bw.write("\t\t</foreach>");
        bw.newLine();
        bw.write("\t\ton DUPLICATE key update");
        bw.newLine();
        StringBuffer insertBatchUpdateBuffer = new StringBuffer();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            insertBatchUpdateBuffer.append("\t\t" + fieldInfo.getFieldName() + " = VALUES(" + fieldInfo.getFieldName() +
                    ")").append(",").append("\n");
        }
        String insertBatchUpdateBufferStr = insertBatchUpdateBuffer.substring(0, insertBatchUpdateBuffer.lastIndexOf(","));
        bw.write(insertBatchUpdateBufferStr);
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildSelectAndUpdateAndDelete(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();

        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            List<FieldInfo> keyFieldInfoList = entry.getValue();

            Integer index = 0;
            StringBuilder methodName = new StringBuilder();

            StringBuffer paramsNames = new StringBuffer();
            for (FieldInfo fieldInfo : keyFieldInfoList) {
                index++;
                methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                paramsNames.append(fieldInfo.getFieldName() + "=#{" + fieldInfo.getPropertyName() + "}");
                if (index < keyFieldInfoList.size()) {
                    methodName.append("And");
                    paramsNames.append(" and ");
                }
            }
            bw.newLine();
            bw.write("\t<!--根据" + methodName + "查询-->");
            bw.newLine();
            bw.write("\t<select id=\"selectBy" + methodName + "\" resultMap=\"base_result_map\">");
            bw.newLine();
            bw.write("\t\tselect <include refid=\"" + BASE_COLUMN_LIST + "\"/> from " + tableInfo.getTableName() + " " +
                    "where " + paramsNames);
            bw.newLine();
            bw.write("\t</select>");
            bw.newLine();

            bw.newLine();
            bw.write("\t<!--根据" + methodName + "更新-->");
            bw.newLine();
            bw.write("\t<update id=\"updateBy" + methodName + "\" parameterType=\"" + poClass + "\">");
            bw.newLine();
            bw.write("\t\tupdate " + tableInfo.getTableName());
            bw.newLine();
            bw.write("\t\t<set>");
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
                bw.newLine();
                bw.write("\t\t\t\t" + fieldInfo.getFieldName() + " = #{bean." + fieldInfo.getPropertyName() + "},");
                bw.newLine();
                bw.write("\t\t\t</if>");
                bw.newLine();
            }
            bw.write("\t\t</set>");
            bw.newLine();
            bw.write("\t\twhere " + paramsNames);
            bw.newLine();
            bw.write("\t</update>");
            bw.newLine();

            bw.newLine();
            bw.write("\t<!--根据" + methodName + "删除-->");
            bw.newLine();
            bw.write("\t<delete id=\"deleteBy" + methodName + "\">");
            bw.newLine();
            bw.write("\t\tdelete from " + tableInfo.getTableName() + " where " + paramsNames);
            bw.newLine();
            bw.write("\t</delete>");
            bw.newLine();
        }
    }

    private static void buildInsertBatch(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        bw.newLine();
        bw.write("\t<!--添加(批量插入)-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertBatch\" parameterType=\"" + poClass + "\">");
        bw.newLine();
        StringBuffer insertFieldBuffer = new StringBuffer();
        StringBuffer insertPropertyBuffer = new StringBuffer();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.isAutoIncrement()) {
                continue;
            }
            insertFieldBuffer.append(fieldInfo.getFieldName()).append(",");
            insertPropertyBuffer.append("#{item." + fieldInfo.getPropertyName() + "}").append(",");
        }
        String insertFieldBufferStr = insertFieldBuffer.substring(0, insertFieldBuffer.lastIndexOf(","));
        bw.write("\t\tINSERT INTO " + tableInfo.getTableName() + "(" + insertFieldBufferStr + ")values");
        bw.newLine();
        bw.write("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">");
        bw.newLine();
        String insertPropertyBufferStr = insertPropertyBuffer.substring(0, insertPropertyBuffer.lastIndexOf(","));
        bw.write("\t\t\t(" + insertPropertyBufferStr + ")");
        bw.newLine();
        bw.write("\t\t</foreach>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildMapperXmlHeader(BufferedWriter bw) throws IOException {
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
        bw.newLine();
        bw.write("\t\t\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
    }

    private static void buildInsertOrUpdate(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        bw.newLine();
        bw.write("\t<!--插入或者更新(匹配有值的字段)-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertOrUpdate\" parameterType=\"" + poClass + "\">");
        bw.newLine();
        bw.write("\t\tINSERT INTO " + tableInfo.getTableName());
        bw.newLine();
        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t" + fieldInfo.getFieldName() + ",");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
        bw.write("\t\t</trim>");
        bw.newLine();
        bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t#{bean." + fieldInfo.getPropertyName() + "},");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
        bw.write("\t\t</trim>");
        bw.newLine();
        bw.write("\t\tON DUPLICATE KEY UPDATE");

        Map<String, String> keyTempMap = new HashMap<>();
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            List<FieldInfo> fieldInfoList = entry.getValue();
            for (FieldInfo item : fieldInfoList) {
                keyTempMap.put(item.getFieldName(), item.getFieldName());
            }
        }

        bw.newLine();
        bw.write("\t\t<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (keyTempMap.containsKey(fieldInfo.getFieldName())) {
                continue;
            }
            bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t " + fieldInfo.getFieldName() + " = VALUES(" + fieldInfo.getFieldName() + "),");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
        bw.write("\t\t</trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildInsert(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        bw.newLine();
        bw.write("\t<!--插入(匹配有效的值)-->");
        bw.newLine();
        bw.write("\t<insert id=\"insert\" parameterType=\"" + poClass + "\">");
        bw.newLine();

        FieldInfo autoIncrementField = null;
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            if (fieldInfo.isAutoIncrement() != null && fieldInfo.isAutoIncrement()) {
                autoIncrementField = fieldInfo;
                break;
            }
        }

        if (autoIncrementField != null) {
            bw.write("\t\t<selectKey keyProperty=\"bean." + autoIncrementField.getFieldName() + "\" resultType" +
                    "=\"" + autoIncrementField.getJavaType() + "\" order=\"AFTER\">");
            bw.newLine();
            bw.write("\t\t\tSELECT LAST_INSERT_ID()");
            bw.newLine();
            bw.write("\t\t</selectKey>");
            bw.newLine();
        }
        bw.write("\t\tINSERT INTO " + tableInfo.getTableName());
        bw.newLine();
        bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t" + fieldInfo.getFieldName() + ",");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
        bw.write("\t\t</trim>");
        bw.newLine();
        bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("\t\t\t<if test=\"bean." + fieldInfo.getPropertyName() + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t#{bean." + fieldInfo.getPropertyName() + "},");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
        bw.write("\t\t</trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
    }

    private static void buildSelectCount(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--查询数量-->");
        bw.newLine();
        bw.write("\t<select id=\"selectCount\" resultType=\"java.lang.Integer\">");
        bw.newLine();
        bw.write("\t\tSELECT count(1) FROM " + tableInfo.getTableName() + " " + "<include " + "refid" + "=\"" + QUERY_CONDITION + "" + "\"/>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
    }

    private static void buildSelectList(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--查询列表-->");
        bw.newLine();
        bw.write("\t<select id=\"selectList\" resultMap=\"base_result_map\">");
        bw.newLine();
        bw.write("\t\tSELECT <include refid=\"" + BASE_COLUMN_LIST + "\"/> FROM " + tableInfo.getTableName() + " " + "<include " + "refid" + "=\"" + QUERY_CONDITION + "" + "\"/>");
        bw.newLine();
        bw.write("\t\t<if test=\"query.orderBy!=null\">order by ${query.orderBy}</if>");
        bw.newLine();
        bw.write("\t\t<if test=\"query.simplePage!=null\">limit #{query.simplePage.start},#{query.simplePage" + ".end}</if>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
    }

    private static void buildQueryCondition(BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--通用查询条件-->");
        bw.newLine();
        bw.write("\t<sql id=\"" + QUERY_CONDITION + "\">");
        bw.newLine();
        bw.write("\t\t<where>");
        bw.newLine();
        bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION + "\"/>");
        bw.newLine();
        bw.write("\t\t\t<include refid=\"" + BASE_QUERY_CONDITION_EXTEND + "\"/>");
        bw.newLine();
        bw.write("\t\t</where>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
    }

    private static void buildBaseQueryConditionExtend(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--拓展的查询条件-->");
        bw.newLine();
        bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION_EXTEND + "\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getExtendFieldList()) {
            String andWhere = "";
            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                andWhere = " and " + fieldInfo.getFieldName() + " like concat('%',#{query." + fieldInfo.getPropertyName() + "},'%')";
            } else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)) {
                    andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " >= str_to_date(#{query." + fieldInfo.getPropertyName() + "}, '%Y-%m-%d') ]]>";
                } else if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END)) {
                    andWhere = "<![CDATA[ and " + fieldInfo.getFieldName() + " < date_sub(str_to_date(#{query." + fieldInfo.getPropertyName() + "}, '%Y-%m-%d'),interval -1 day) ]]>";
                }
            }


            bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null and query." + fieldInfo.getPropertyName() + " != ''\">");
            bw.newLine();
            bw.write("\t\t\t" + andWhere);
            bw.newLine();
            bw.write("\t\t</if>");
            bw.newLine();
        }
        bw.write("\t</sql>");
        bw.newLine();
    }

    private static void buildBaseQueryCondition(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--基础查询条件-->");
        bw.newLine();
        bw.write("\t<sql id=\"" + BASE_QUERY_CONDITION + "\">");
        bw.newLine();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            String stringQuery = "";
            if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                stringQuery = " and query." + fieldInfo.getPropertyName() + " != ''";
            }

            bw.write("\t\t<if test=\"query." + fieldInfo.getPropertyName() + " != null" + stringQuery + "\">");
            bw.newLine();
            bw.write("\t\t\tand " + fieldInfo.getFieldName() + " = #{query." + fieldInfo.getPropertyName() + "}");
            bw.newLine();
            bw.write("\t\t</if>");
            bw.newLine();
        }
        bw.write("\t</sql>");
        bw.newLine();
    }

    private static void buildBaseColumnList(TableInfo tableInfo, BufferedWriter bw) throws IOException {
        bw.newLine();
        bw.write("\t<!--通用查询列-->");
        bw.newLine();

        bw.write("\t<sql id=\"" + BASE_COLUMN_LIST + "\">");
        bw.newLine();
        StringBuilder columnBuilder = new StringBuilder();
        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            columnBuilder.append(fieldInfo.getFieldName()).append(",");
        }
        bw.write("\t\t" + columnBuilder.substring(0, columnBuilder.lastIndexOf(",")));
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
    }

    private static void buildBaseResultMap(TableInfo tableInfo, BufferedWriter bw, String poClass) throws IOException {
        bw.write("\t<!--实体映射-->");
        bw.newLine();

        bw.write("\t<resultMap id=\"base_result_map\" type=\"" + poClass + "\">");
        bw.newLine();

        FieldInfo idField = null;
        Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
        for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
            if ("PRIMARY".equals(entry.getKey())) {
                List<FieldInfo> FieldInfoList = entry.getValue();
                if (FieldInfoList.size() == 1) {
                    idField = FieldInfoList.get(0);
                    break;
                }
            }
        }

        for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
            bw.write("\t\t<!--" + fieldInfo.getComment() + "-->");
            bw.newLine();
            String key = "";
            if (idField != null && fieldInfo.getPropertyName().equals(idField.getPropertyName())) {
                key = "id";
            } else {
                key = "result";
            }
            bw.write("\t\t<" + key + " column=\"" + fieldInfo.getFieldName() + "\" property=\"" + fieldInfo.getPropertyName() + "\"/>");
            bw.newLine();
        }
        bw.write("\t</resultMap>");
        bw.newLine();
    }


}
