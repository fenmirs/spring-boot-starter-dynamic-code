package com.smart.dynamic.defines.act;

import com.smart.dynamic.enums.ActType;

public interface IAct {
    default ActType support() {
        return ActType.SQLAct;
    }
}
