package com.yami.trading.api.service;

import com.yami.trading.bean.model.User;
import com.yami.trading.common.domain.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UserCacheService {

    User currentUser();

    boolean updateUser(User user);

    Result upload(MultipartFile paramMultipartFile, String paramString);
}
