package com.yami.trading.api.controller;


import com.google.common.collect.Maps;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.api.util.PropertiesUtil;
import com.yami.trading.bean.model.FileUploadParamsModel;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.AwsS3OSSFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
@Api(tags ="文件上传")
@RequestMapping("api")
public class ApiUploadFileController {

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    UserCacheService userCacheService;
   /* @PostMapping(value = "/api/uploadFile")
    @ApiOperation("文件上传")
    public Result uploadFile(FileUploadParamsModel model) {
        try {
            if (model.getFile().getSize() / 1000L > 4500) {
               throw  new YamiShopBindException("图片大小不能超过4M");
            }
            String path = awsS3OSSFileService.uploadFile(model.getModuleName(), model.getFile());
            return Result.succeed(path);
        }
         catch (Exception e) {
             e.printStackTrace();
            throw  new YamiShopBindException("文件上传失败");
        }
    }*/
    //图片上传
  @PostMapping(value = "/api/uploadFile")
    @ApiOperation("文件上传")
    public Result upload(FileUploadParamsModel model, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");
        Result serverResponse = userCacheService.upload(model.getFile(), path);
        if (serverResponse.isSucceed()) {
            String targetFileName = serverResponse.getData().toString();
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            //fileMap.put("path", targetFileName);
           // fileMap.put("httpUrl", url);
            return Result.succeed(fileMap);
        }
        return serverResponse;
    }
}
