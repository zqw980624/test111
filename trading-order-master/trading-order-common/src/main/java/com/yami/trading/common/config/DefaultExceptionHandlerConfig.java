/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.common.config;

import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义错误处理器
 * @author LGH
 */
@Slf4j
@Controller
@RestControllerAdvice
public class DefaultExceptionHandlerConfig {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result <String> exception(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        List<ErrorMsg> errorMsgs = new ArrayList<>();

        allErrors.forEach(objectError -> {
            ErrorMsg errorMsg = new ErrorMsg();
            FieldError fieldError = (FieldError)objectError;
            errorMsg.setField(fieldError.getField());
            errorMsg.setMessage(fieldError.getDefaultMessage());
            errorMsgs.add(errorMsg);
        });
        return Result.failed(errorMsgs.toString());    }


    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result handleValidationException(ConstraintViolationException e) {
        StringBuffer errorBuffer = new StringBuffer();
        for(ConstraintViolation<?> s:e.getConstraintViolations()){
            errorBuffer.append(s.getMessage()+";") ;
        }
        return Result.failed(errorBuffer.toString());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result <String> validExceptionHandler(BindException e) {
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors) {
            message.append(error.getField()).append(error.getDefaultMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return Result.failed( message.toString());

    }



    @ExceptionHandler(value = RuntimeException.class)
    public  Result <String> handlerRuntimeException(HttpServletRequest req,
                                            RuntimeException ex) {
        log.error("{}", ex);
        String message = ex.getMessage();
        if(StringUtils.isNotEmpty(message)){
            return Result.failed(message);
        }else{
            return Result.failed(ex.toString());
        }
    }



    @ExceptionHandler(YamiShopBindException.class)
    public Result unauthorizedExceptionHandler(YamiShopBindException e){
        log.error("YamiException Message :{}",e.getMessage());
        return  Result.of(null,e.getCode(),e.getMessage());
    }
    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionHandler(BusinessException e){
        log.error("YamiException Message :{}",e.getMessage());
        return  Result.failed(e.getMessage());
    }
}
