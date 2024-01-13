package com.yami.trading.api.controller;

import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.AwsS3OSSFileService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class ApiUploadImgController {
    public final String basePath = "/api/public/uploadimg";
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @RequestMapping(value = basePath + "!execute.action")
    public Result<String> execute(MultipartFile file) {
        if (file.getSize() / 1024L > 30720L) {
            return Result.failed("图片大小不能超过30M");
        }
        String path = awsS3OSSFileService.uploadFile("chat", file);
        return Result.succeed(awsS3OSSFileService.getUrl(path));
    }
}
