package com.smart.dynamic.service;

import java.util.Map;

import com.jfinal.plugin.activerecord.Page;
import com.smart.dynamic.defines.DynamicLogicDefine;
import com.smart.dynamic.defines.DynamicResult;

/**
 * @Description 动态查询的定义及执行接口 <br/>
 * @Author H.Y.F
 * @Date 2024-04-16 10:46
 * @Version V1.0
 */
public interface IDynamicLogicService {
    String TABLE = "dynamic_logic_c";

    DynamicLogicDefine save(DynamicLogicDefine query);

    DynamicLogicDefine detail(Long id);

    DynamicResult<?> execute(DynamicLogicDefine define, Map<String, Object> data);

    Page<DynamicLogicDefine> list(int pageNum, int pageSize, String keyword);

    String generateMarkDown();

    String MARKDOWN_START = "\n" +
            "## 1. 前端调用参考\n" +
            "\n" +
            "> 请求相关\n" +
            "\n" +
            "- 请求URL：https://192.168.2.223:23400/lcgl-prj/v1/dynamic/query/execute\n" +
            "- 请求方式：GET\n" +
            "- 请求参数：\n" +
            "\n" +
            "| 参数名       | 类型     | 位置     | 示例                                             | 备注                      |\n"
            +
            "|-----------|--------|--------|:-----------------------------------------------|:------------------------|\n"
            +
            "| Jid       | string | Header | abcdefg                                        | 用于登录用户身份识别              |\n"
            +
            "| dynamicId | long   | Query  | 1                                              | 动态查询定义的标识               |\n"
            +
            "| data      | obj    | Query  | {\"id\":1355876707136,\"pageNum\":1,\"pageSize\":20} | 查询参数,不同的动态查询定义可能具有不同的参数 |\n"
            +
            "\n" +
            "- 示例\n" +
            "\n" +
            "```url\n" +
            "https://192.168.2.223:23400/lcgl-prj/v1/dynamic/query/execute?dynamicId=1&data=%7B%22id%22%3A1355876707136%2C%22pageNum%22%3A1%2C%22pageSize%22%3A20%7D\n"
            +
            "```\n" +
            "\n" +
            "> 响应相关\n" +
            "\n" +
            "| 属性名       | 类型     | 示例                            | 备注                 |\n" +
            "|:----------|:-------|:------------------------------|:-------------------|\n" +
            "| code      | int    | 200                           | 状态码,成功:200         |\n" +
            "| data      | obj    | {\"name\": \"测试\", \"no\": \"abc\"}   | 返回对象:一般见于详情查询      |\n" +
            "| list      | array  | [{\"name\": \"测试\", \"no\": \"abc\"}] | 返回列表：一般见于列表/分页查询   |\n" +
            "| message   | string | \"操作成功\"                        | 执行结果提示             |\n" +
            "| totalPage | int    | 10                            | 分页查询时返回总页数,其他时候为0  |\n" +
            "| pageNum   | int    | 1                             | 分页查询时返回当前页,其他时候为0  |\n" +
            "| pageSize  | int    | 20                            | 分页查询时返回每页条数,其他时候为0 |\n" +
            "\n" +
            "- 成功示例\n" +
            "\n" +
            "```json\n" +
            "{\n" +
            "  \"code\": 200,\n" +
            "  \"data\": {\n" +
            "    \"up_ver\": 3,\n" +
            "    \"ca_id_wt\": \"1253686089320\",\n" +
            "    \"task_st\": \"1218948963264,1218948963286,1218948963296\",\n" +
            "    \"ut_80_name\": null\n" +
            "  },\n" +
            "  \"list\": [],\n" +
            "  \"message\": \"操作成功\",\n" +
            "  \"pageNum\": 0,\n" +
            "  \"pageSize\": 0,\n" +
            "  \"totalPage\": 0\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "- 失败示例\n" +
            "\n" +
            "```json\n" +
            "{\n" +
            "  \"code\": 500,\n" +
            "  \"data\": null,\n" +
            "  \"list\": [],\n" +
            "  \"message\": \"未找到查询规则\",\n" +
            "  \"pageNum\": 0,\n" +
            "  \"pageSize\": 0,\n" +
            "  \"totalPage\": 0\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "## 2. 动态查询定义列表";
}
