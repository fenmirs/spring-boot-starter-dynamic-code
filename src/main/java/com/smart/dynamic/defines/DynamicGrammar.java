package com.smart.dynamic.defines;

import java.util.List;

import com.smart.dynamic.defines.act.SQLAct;

import lombok.Getter;
import lombok.Setter;

/**
 * @BelongProject std_ver2.0_zszj
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/18 17:24
 * @Version V1.0
 */
@Getter
@Setter
public class DynamicGrammar {
    private List<ParamDefine> params;
    private List<SQLAct> acts;
}
