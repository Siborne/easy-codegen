package org.flyingsparrow.builder;

import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.bean.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 代码生成统一入口：根据配置刷新常量、按需生成指定表与指定类型、收集生成文件清单。
 * 供可视化面板直接调用，也兼容原有命令行流程。
 *
 * @author Siborne
 */
public class GenerateEngine {

    private static final Logger logger = LoggerFactory.getLogger(GenerateEngine.class);

    /** 生成类型：基础类 */
    public static final String TYPE_BASE = "base";
    /** 生成类型：PO 实体 */
    public static final String TYPE_PO = "po";
    /** 生成类型：Query 查询对象 */
    public static final String TYPE_QUERY = "query";
    /** 生成类型：Mapper 接口 */
    public static final String TYPE_MAPPER = "mapper";
    /** 生成类型：Mapper XML */
    public static final String TYPE_MAPPER_XML = "mapperXml";
    /** 生成类型：Service 接口 */
    public static final String TYPE_SERVICE = "service";
    /** 生成类型：ServiceImpl 实现 */
    public static final String TYPE_SERVICE_IMPL = "serviceImpl";
    /** 生成类型：Controller */
    public static final String TYPE_CONTROLLER = "controller";

    private GenerateEngine() {
    }

    /**
     * 执行一次代码生成（默认生成全部类型）
     */
    public static GenerateResult generate(Properties props, Collection<String> tableNames) {
        return generate(props, tableNames, null);
    }

    /**
     * 执行一次代码生成
     *
     * @param props      完整生成配置（数据库连接 + 生成参数）
     * @param tableNames 要生成的表名集合，null/空 表示全部表
     * @param types      要生成的类型集合（TYPE_* 常量），null/空 表示全部类型
     * @return 生成结果（文件清单 + 错误信息）
     */
    public static GenerateResult generate(Properties props, Collection<String> tableNames, Collection<String> types) {
        GenerateResultCollector.clear();
        try {
            Constants.reload(props);
        } catch (Exception e) {
            logger.error("配置刷新失败", e);
            return GenerateResult.fail("配置解析失败: " + e.getMessage());
        }

        List<TableInfo> tableInfoList = BuildTable.getTables(props, tableNames);
        if (tableInfoList.isEmpty()) {
            return GenerateResult.fail("未读取到任何表，请检查数据库连接和表名");
        }

        boolean allTypes = types == null || types.isEmpty();

        try {
            if (allTypes || types.contains(TYPE_BASE)) {
                BuildBase.execute();
            }

            for (TableInfo tableInfo : tableInfoList) {
                if (allTypes || types.contains(TYPE_PO)) {
                    BuildPo.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_QUERY)) {
                    BuildQuery.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_MAPPER)) {
                    BuildMapper.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_MAPPER_XML)) {
                    BuildMapperXml.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_SERVICE)) {
                    BuildService.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_SERVICE_IMPL)) {
                    BuildServiceImpl.execute(tableInfo);
                }
                if (allTypes || types.contains(TYPE_CONTROLLER)) {
                    BuildController.execute(tableInfo);
                }
            }
        } catch (Exception e) {
            logger.error("代码生成失败", e);
            return GenerateResult.fail("代码生成失败: " + e.getMessage());
        }

        return GenerateResult.success(GenerateResultCollector.getAndClear());
    }
}
