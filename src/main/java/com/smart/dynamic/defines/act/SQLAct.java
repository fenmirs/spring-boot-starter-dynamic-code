package com.smart.dynamic.defines.act;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.smart.dynamic.defines.MatchItem;
import com.smart.dynamic.enums.DynamicSQLType;

/**
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/18 17:25
 * @Version V1.0
 */
@Getter
@Setter
public class SQLAct {
    /**
     * 基本查询语句 <br/>
     * eg: SELECT * FROM a
     */
    private String base;

    /**
     * 排序SQL
     */
    private String orderBy;

    /**
     * 类型
     */
    private DynamicSQLType type;


    /**
     * 类型
     */
    private Boolean needReturn = false;


    /**
     * 当执行结果为null时的错误信息
     */
    private String  errorMsgWhenNull;

    /**
     * 条件
     * <pre>
   
     * 1. 条件表达式可以使用 ${} 进行格式化占位，#{} 进行原样占位。
     * 比如 type = "name" 时， A.type = ${type} AND B.#{type} = 'BZJX' 解析为  AND (A.type = 'name' AND B.name = 'BZJX')
     * 2. 当有多个参数时 使用 &,| 关联，其中 & 代表并且 |代表或者,并支持()提升优先级【很明显不支持值包含& |的情况，但一般也遇不到值为& |还做条件分支的情况】
     * 3. null 代表参数为空的情况
     * "condition":[
     *    {"formula":"status=1&nodeId='ut_90'","sql":" AND A.node_id='ut_99'"},
     *    {"formula":"status=2|nodeId='ut_90'","sql":"AND A.node_id='end'"},
     *    {"formula":"status!=2&(nodeId='ut_90' | type='BZJX')","sql":"AND (A.type = ${type} OR B.#{type} = 'BZJX')"},
     *    {"formula":"status=null&nodeId!=null","sql":"AND A.node_id in ('ut_99','end')"},
     *  ]
     * </pre>
     */
    private List<MatchItem> conditions;


    /**
     * 别名
     * 可用于后续执行序列获取执行结果,
     * 返回结果会将序列的返回值装配为map
     */
    private String alias;

    private String note;
}
