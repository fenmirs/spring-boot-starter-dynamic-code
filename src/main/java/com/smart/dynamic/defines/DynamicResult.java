package com.smart.dynamic.defines;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import com.jfinal.plugin.activerecord.Page;

/**
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/12 13:12
 * @Version V1.0
 */
@Getter
@Setter
public class DynamicResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    private boolean status;
    private T data;
    private List<T> list;
    //--page--
    private int totalPage;
    private int totalRow;
    private int pageNum;
    private int pageSize;

    public static <E> DynamicResult<E> success(E data) {
        DynamicResult<E> res = new DynamicResult<>();
        res.setCode(0);
        res.setData(data);
        res.setStatus(true);
        res.setMessage("操作成功");
        return res;
    }

    public static <E> DynamicResult<E> success(List<E> data) {
        DynamicResult<E> res = new DynamicResult<>();
        res.setCode(0);
        res.setList(data);
        res.setTotalPage(0);
        res.setPageNum(0);
        res.setPageSize(0);
        res.setStatus(true);
        res.setMessage("操作成功");
        return res;
    }

    public static <E> DynamicResult<E> success(Page<E> pageData) {
        DynamicResult<E> res = new DynamicResult<>();
        res.setCode(0);
        res.setList(pageData.getList());
        res.setStatus(true);
        res.setTotalRow(pageData.getTotalRow());
        res.setTotalPage(pageData.getTotalPage());
        res.setPageNum(pageData.getPageNumber());
        res.setPageSize(pageData.getPageSize());
        res.setMessage("操作成功");
        return res;
    }

    public static <E> DynamicResult<E> fail(String message) {
        DynamicResult<E> res = new DynamicResult<>();
        res.setCode(1);
        res.setStatus(false);
        res.setMessage(message);
        return res;
    }

    public static <E> DynamicResult<E> fail(int code,String message) {
        DynamicResult<E> res = new DynamicResult<>();
        res.setCode(code);
        res.setStatus(false);
        res.setMessage(message);
        return res;
    }
}
