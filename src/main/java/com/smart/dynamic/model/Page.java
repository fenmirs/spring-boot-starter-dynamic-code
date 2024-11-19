package com.smart.dynamic.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Page {
    private int pageSize = 20;
    private int pageNum = 1;
}
