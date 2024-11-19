# 动态代码

## 背景和目的

在项目型任务的工作中，一个开发者会负责很多项目，随之而来的是堆积如山的任务；

经过分析许多任务实际上业务逻辑并不复杂，但受限于 MVC 的架构模式，往往需要编写很多重复代码.

另外，在开发过程中每次需求变更都需要重新重新编写代码、部署服务，相当繁琐。虽然需求变动并不是开发者的问题，但如果这种现状无法避免，开发者也将疲于应对。

因此，为减轻开发者负担，使开发者能聚焦于疑难杂症，从容面对不稳定的需求，此项目应运而生。

## 概要

`动态代码` 是的动态代码工具包；它本质上是动态组装 SQL 来实现数据库的操作；

由 `行为定义` 和 `行为执行` 两部分组成。定义保存于数据库表中，行为执行通过 java 代码编写。


## 使用指南

1. 在数据库中新增表 `dynamic_logic_c`，如下是 MYSQL数据的建表语句
``` sql
DROP TABLE IF EXISTS `dynamic_logic_c`;
CREATE TABLE `dynamic_logic_c`
(
    `id`          bigint unsigned NOT NULL,
    `title`       varchar(100)    NOT NULL DEFAULT '' COMMENT '标题',
    `note`        varchar(255)    NULL COMMENT '备注',
    `grammar`     TEXT            NOT NULL COMMENT '语法：json格式',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='动态逻辑配置';

```

2. 在 `pom.xml` 中加入 spring-boot-starter-dynamic-code依赖：
``` xml
 </dependencies>
  .....
    <dependency>
        <groupId>com.smart</groupId>
        <artifactId>spring-boot-starter-dynamic-code</artifactId>
        <version>1.0-RELEASE</version>
    </dependency>
  ....
 </dependencies>
```
3. 编写C层实现类 `CustomerDynamicController.java` 实现 `AbstractDynamicController`
``` java
@RestController
public class DynamicController extends AbstractDynamicController {

    @Override
    protected DynamicResult<?> callAfter(DynamicResult<?> res) {
        return res;
    }

    @Override
    protected JSONObject callBefore(JSONObject paramData, HttpServletRequest request) {
        return initUserParams(paramData, request);
    }

    private Record getLoginUser(HttpServletRequest request) {
        String JID = request.getHeader("Jid");
        BusinessAssert.notNull(JID, "缺失Jid");
        Claims claims = JWTUtil.parseJWT(JID);
        BusinessAssert.notNull(claims, "Jid解析失败");
        String loginName = JWTUtil.getLoginNa(claims);
        BusinessAssert.notEmpty(loginName, "Jid获取当前用户信息失败");
        Record user = Db.findFirst("SELECT * FROM ywpz_user_s WHERE login_name=?",
                loginName);
        BusinessAssert.notNull(user, "用户不存在");
        return user;
    }

    private JSONObject initUserParams(JSONObject data, HttpServletRequest request) {
        Record user = getLoginUser(request);
        // 初始化用户参数
        user.getColumns().forEach((k, v) -> {
            String alias = "user." + k.trim();
            data.put(alias, v);
        });
        return data;
    }
}
```

好的，你的项目已经自动拥有了如下接口：

> 行为定义的接口

1. `@GetMapping("/v1/dynamic/doc")`  生成markdow文档
2. `@GetMapping("/v1/dynamic/list")` 查询定义列表
3. `@GetMapping("/v1/dynamic/detail")` 查询定义详情
4. `@PostMapping("/v1/dynamic/save")` 新增/更新一个定义

> 行为执行的接口

SIMPLE_SAVE类型的定义只支持POST
1. `@GetMapping("/v1/dynamic/call")`   GET方式执行一条定义
2. `@PostMapping("/v1/dynamic/call")`  POST方式执行一条定义


## 行为语法
dynamic_logic_c 表中的`grammar`指定了动态代码的行为，这是动态代码的核心。

在数据库中是json存储，对应JAVA中（*代码见附录*）的`DynamicGrammar.java` 类。

其中 `ParamDefine.java` 是请求参数的定义，`DynamicSQLAct.java` 是行为逻辑的定义。

---
`ParamDefine`中有如下属性:
+ `alias`参数名，同样也可以作为后续SQL中的变量名
    + 当变量是一个`值`:
        + `${变量名}` 是类型格式化填入,`#{变量名}`则是原值填入
    + 当变量是一个`数组`：
        + `${变量名}` 会将每个项类型格式化后再用逗号拼接成字符串，**不支持**`#{变量名}`
+ `type` 参数类型，不填默认是STRING. 
    + 可选值有: `STRING,INT,LONG,FLOAT,DOUBLE,BOOLEAN,DATE,DATETIME`.
+ `must` 参数是否必填，不填默认是非必填.
+ `defaultValue` 参数默认值，当传值为空时，自动赋予此配置值.
+ `validRegex` 参数验证：正则表达式
+ `validSql` 参数验证：SQL方式
+ `note` 参数描述，一般用于文档生成

---

`DynamicSQLAct`中有如下重要属性:
+ `type` 动态SQL类型，是查询还是保存.
    + `FIND_ONE`  查询，返回单条数据  (默认).
    + `FIND_LIST` 查询，返回列表数据,用于少量数据.
    + `FIND_PAGE` 查询，返回分页数据,用于大量数据，可传参page，pageSize.
    + `SIMPLE_SAVE` 保存，简单的数据保存.
+ `base` 基本SQL语句。 如: `SELECT * FROM table1`
+ `orderBy` orderBy语句。 如: `order by A.id desc`
+ `alias` 别名，和参数一样，act的执行结果也可作为后续act的变量使用。
    + FIND_ONE 类型结果： `#{act.别名.属性名}`和`${act.别名.属性名}`
    + FIND_LIST 类型结果： `#{act.别名.[序号].属性名}`和`${act.别名.[序号].属性名}`
+ `needReturn` 当前行为是否返回到客户端,默认`false`。
+ `errorMsgWhenNull` 当执行结果为空时抛出的错误。
+ `conditions` 额外条件,采用数组的方式支持`多个条件项顺序`匹配。
    ``` json
     "conditions":[
        {"formula":"status=1&nodeId='ut_90'","sql":" AND A.node_id='ut_99'"},
        {"formula":"status=2|nodeId='ut_90'","sql":"AND A.node_id='end'"},
        {"formula":"status!=2&(nodeId='ut_90' | type='BZJX')","sql":"AND (A.type = ${type} OR B.#{type} = 'BZJX')"},
        {"formula":"status=null&nodeId!=null","sql":"AND A.node_id in ('ut_99','end')"},
      ]
    ```
    + 条件项的定义如下：
        + 格式： `{"formula":"***","sql":"***"}`
        + 多个判断可使用 `& |` 关联，& 并且，| 或者。支持()提升优先级。*很明显不支持`值`包含& | ()的情况*
        + null 代表参数为空的情况
--- 


### 一、最简单的配置: 没有参数，也没有任何额外的查询条件

``` json
{
    "acts":[
       {
         "type":"FIND_LIST",
         "base":"SELECT * FROM table1"
       }
    ]
}
```


### 二、常用的配置：增加参数及查询条件

``` json
{
    "params":[
        {
            "alias":"keyword",
            "type":"STRING",
            "must":true,
            "defaultValue":"",

        }
    ],
    "acts":[
       {
         "type":"FIND_LIST",
         "base":"SELECT * FROM table1 WHERE name like concat('%',#{keyword},'%')"
       }
    ]
}
```




## 附录

``` java
public class DynamicGrammar {
    private List<ParamDefine> params;
    private List<DynamicSQLAct> acts;
    ...
}

public class ParamDefine implements Serializable {
    private static final long serialVersionUID = 1L;
    /*名称:用于调用方传参*/
    private String alias;
    /*必填:Y/N*/
    private boolean must = false;
    /*默认值*/
    private Object defaultValue;
    /*参数类型*/
    private ParamType type;
    /*格式:正则验证*/
    private String validRegex;
    /*数据库SQL验证*/
    private String validSql;
    /*备注*/
    private String note;
    ...
}

public class DynamicSQLAct {
    /**
     * 基本查询语句 <br/>
     * eg: SELECT * FROM a
     */
    private String base;
    /*排序SQL*/
    private String orderBy;
    private DynamicSQLType type;
    private Boolean needReturn = false;
    /*当执行结果为null时的错误信息 */
    private String  errorMsgWhenNull;
    private List<MatchItem> conditions;
    private String alias;
    private String note;
    ...
}

public class MatchItem {
    private String formula;
    private String sql;
    ...
}

public enum ParamType {
    STRING,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    DATE,
    DATETIME,
    ...
}

public enum DynamicSQLType {
    SIMPLE_SAVE,
    FIND_ONE,
    FIND_LIST,
    FIND_PAGE,
    ...
}

```