package com.xuecheng.base.Exception;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations ={RestController.class, Controller.class})
public class GlobalException {

    @ExceptionHandler(addCourseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse addCourseException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

    @ExceptionHandler(addCourseMarketException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse addCourseMarketException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

    @ExceptionHandler(updateCourseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse updateCourseException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

    @ExceptionHandler(updateCourseMarketException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse updateCourseMarketException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }
    @ExceptionHandler(deleteTeachplanException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse deleteTeachplanException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }
    @ExceptionHandler(addCourseTeacherException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse addCourseTeacherException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }
    @ExceptionHandler(addMediaException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse addMediaException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }
    @ExceptionHandler(mergevedioException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse mergevedioException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

    @ExceptionHandler(saveCoursePublishMessageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse saveCoursePublishMessageException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

    @ExceptionHandler(uploadFileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse uploadFileException(Exception e){
        log.error(e.getMessage());
        return new RestErrorResponse(e.getMessage());
    }

}
