package com.smart.dynamic.defines;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description 动态查询定义 <br/>
 * @Author H.Y.F
 * @Date 2024-04-18 11:41
 * @Version V1.0
 */
@Getter
@Setter
public class DynamicLogicDefine implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private String note;


    /**
     * 查询内容举例:
     * 
     * <pre>
     *{
     "params": [
     *     {
     *       "alias": "status",
     *       "must": false,
     *       "note": "状态: 1待办 2已办 不传值查询全部",
     *       "type": "LONG",
     *       "validRegex": ""
     *     },
     *     {
     *       "alias": "keyword",
     *       "must": false,
     *       "note": "模糊查询关键字",
     *       "type": "STRING",
     *       "validRegex": ""
     *     },
     *     {
     *       "alias": "nodeId",
     *       "must": true,
     *       "note": "节点ID",
     *       "type": "STRING",
     *       "validRegex": ""
     *     }
     *   ],
     *   "act": {
     *     "base": " SELECT A.name,B.name,A.id,A.node_id,
     *               CASE A.type WHEN #{nodeId} THEN 1 ELSE 2 END as status
     *               FROM A LEFT JOIN B ON A.id = B.p_id
     *               WHERE A.sa_no like concat('%',${keyword},'%') AND B.creat_time <= now()",
     *     "orderBy":"A.id asc",
     *     "condition":[
     *          {"formula":"status=1&nodeId='ut_90'","sql":" AND A.node_id='ut_99'"},
     *          {"formula":"status=2|nodeId='ut_90'","sql":"AND A.node_id='end'"},
     *          {"formula":"status!=2&(nodeId='ut_90' | type='BZJX')","sql":"AND (A.type = ${type} OR B.#{type} = 'BZJX')"},
     *          {"formula":"status=null&nodeId!=null","sql":"AND A.node_id in ('ut_99','end')"},
     *      ]
     *   }
     * }
     * </pre>
     */
    private DynamicGrammar grammar;
}
