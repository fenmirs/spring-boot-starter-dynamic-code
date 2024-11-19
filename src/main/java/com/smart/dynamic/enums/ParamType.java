package com.smart.dynamic.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public enum ParamType {
    STRING,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    DATE,
    DATETIME,
    ;

    public boolean isNull(Object o) {
        switch (this) {
            case STRING:
            case DATE:
            case DATETIME:
                return o == null || StringUtils.isBlank(String.valueOf(o));
            default:
                return Objects.isNull(o);
        }
    }

    public Object format(Object o) {
        if(o == null) {
            return null;
        }
        switch (this) {
            case INT:
                return Integer.parseInt(String.valueOf(o));
            case LONG:
                return Long.parseLong(String.valueOf(o));
            case FLOAT:
                return Float.parseFloat(String.valueOf(o));
            case DOUBLE:
                return Double.parseDouble(String.valueOf(o));
            case BOOLEAN:
                return Boolean.parseBoolean(String.valueOf(o));
            default:
                return String.valueOf(o);
        }
    }
}
