package com.anjingwsno1.langchain4jknowledgebase.common.base;

import com.anjingwsno1.langchain4jknowledgebase.common.enums.ErrorEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private String code;

    private String message;

    private T data;

    public BaseResponse() {
    }

    public BaseResponse(boolean success) {
        this.success = success;
    }

    public BaseResponse(boolean success, T data) {
        this.data = data;
        this.success = success;
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.success = false;
        this.message = message;
        this.data = data;
    }

    public static BaseResponse success(String message){
        return new BaseResponse(ErrorEnum.SUCCESS.getCode(), message, "");
    }
}
