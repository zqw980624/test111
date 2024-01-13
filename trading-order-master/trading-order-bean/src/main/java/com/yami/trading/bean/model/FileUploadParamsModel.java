package com.yami.trading.bean.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadParamsModel {


    @ApiModelProperty("文件")
    private MultipartFile file;


    @ApiModelProperty("模块名")
    private String moduleName;
}
