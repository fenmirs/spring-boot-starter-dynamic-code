package com.smart.dynamic.defines;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import com.smart.dynamic.enums.ParamType;

/**
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/12 11:38
 * @Version V1.0
 */
@Getter
@Setter
public class ParamDefine implements Serializable {
    private static final long serialVersionUID = 1L;
    /*名称:用于调用方传参*/
    private String alias;
    /*必填:Y/N*/
    private boolean must = false;
    /*默认值*/
    private Object defaultValue;
    /*参数类型:默认STRING*/
    private ParamType type = ParamType.STRING;
    /*格式:正则验证*/
    private String validRegex;
    /*数据库SQL验证*/
    private String validSql;
    /*备注*/
    private String note;
}
