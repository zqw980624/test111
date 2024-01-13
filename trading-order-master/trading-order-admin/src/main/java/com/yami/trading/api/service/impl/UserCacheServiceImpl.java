package com.yami.trading.api.service.impl;

import com.google.common.collect.Lists;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.api.util.FTPUtil;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.UUID;

/**
 * 用户缓存操作类
 */
@Service
public class UserCacheServiceImpl implements UserCacheService {
    @Resource
    UserService userService;

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public boolean updateUser(User user) {
        return userService.updateById(user);
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    @Override
    public User currentUser() {
        String userId = SecurityUtils.getUser().getUserId();
        User user = userService.getById(userId);
        if (!user.isEnabled()) {
            throw new YamiShopBindException("用户已锁定");
        }
        return user;
    }

    public Result upload(MultipartFile file, String path) {
        //String fileName = file.getOriginalFilename();
        //String fileExtentionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID() + "." + "png";
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File tartgetFile = new File(path, uploadFileName);
        boolean result = false;
        try {
            file.transferTo(tartgetFile);
            result = FTPUtil.uploadFile(Lists.newArrayList(new File[]{tartgetFile}));
            tartgetFile.delete();
        } catch (Exception e) {
           // log.error("上传文件异常 , 错误信息 = {}", e);
            return null;
        }

        if (result) {
            return Result.succeed(tartgetFile.getName(), "上传成功");
        }
        return Result.failed("上传失败");
    }
}
