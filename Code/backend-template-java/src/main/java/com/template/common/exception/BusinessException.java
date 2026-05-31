package com.template.common.exception;

import com.template.common.response.ApiCode;

/**
 * 业务异常，用于表达可预期的业务失败。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.code = apiCode.getCode();
    }

    public BusinessException(ApiCode apiCode, String message) {
        super(message);
        this.code = apiCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
