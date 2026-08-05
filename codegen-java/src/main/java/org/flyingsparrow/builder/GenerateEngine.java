package org.flyingsparrow.builder;

import org.flyingsparrow.bean.Constants;
import org.flyingsparrow.bean.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 代码生成统一入口：根据配置刷新常量、按需生成指定表、收集生成文件清单。
 * 供可视化面板直接调用，也兼容原有命令行流程。
 *
 * @author Siborne
 */
public class GenerateEngine {

    private static final Logger logger = LoggerFactory.getLogger(GenerateEngine.class);

    private GenerateEngine() {
    }

    /**
     * 执行一次代码生成
     *
     * @param props      完整生成配置（数据库连接 + 生成参数）
     * @param tableNames 要生成的表名集合，null/空 表示全部表
     * @return 生成结果（文件清单 + 错误信息）
     */
    public static GenerateResult generate(Properties props, Collection<String> tableNames) {
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

        try {
            BuildBase.execute();

            for (TableInfo tableInfo : tableInfoList) {
                BuildPo.execute(tableInfo);
                BuildQuery.execute(tableInfo);
                BuildMapper.execute(tableInfo);
                BuildMapperXml.execute(tableInfo);
                BuildService.execute(tableInfo);
                BuildServiceImpl.execute(tableInfo);
                BuildController.execute(tableInfo);
            }
        } catch (Exception e) {
            logger.error("代码生成失败", e);
            return GenerateResult.fail("代码生成失败: " + e.getMessage());
        }

        return GenerateResult.success(GenerateResultCollector.getAndClear());
    }
}
