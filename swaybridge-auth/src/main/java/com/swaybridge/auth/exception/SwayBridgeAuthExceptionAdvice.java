package com.swaybridge.auth.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.swaybridge.common.model.dto.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SwayBridgeAuthExceptionAdvice {

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handlerException(NotLoginException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handlerException(Exception e) {
        return Result.fail(e.getMessage());
    }

}
