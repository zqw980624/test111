package com.yami.trading.admin.controller;

import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.api.util.PropertiesUtil;
import com.yami.trading.bean.model.FileUploadParamsModel;
import com.yami.trading.bean.vo.FileInfoVo;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.AwsS3OSSFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Api(tags ="文件上传")
@RequestMapping()
public class UploadFileController {

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    UserCacheService userCacheService;

    @PostMapping(value = "/api/uploadFile")
    @ApiOperation("文件上传")
    public Result<FileInfoVo> upload(FileUploadParamsModel model, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");
        Result serverResponse = userCacheService.upload(model.getFile(), path);
        if (serverResponse.isSucceed()) {
            String targetFileName = serverResponse.getData().toString();
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            FileInfoVo file = new FileInfoVo();
            file.setPath(targetFileName);
            file.setHttpUrl(url);
            return Result.ok(file);
        }
        return Result.succeed();
    }
}
