package com.yami.trading.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yami.trading.security.common.model.YamiSysUser;
import com.yami.trading.security.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 填充器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        YamiSysUser sysUser = null;
        try {
            sysUser = SecurityUtils.getSysUser();
        }catch (Exception e){
            log.debug("前端场景");
        }
        if (sysUser != null) {
            if (metaObject.hasGetter("createBy")) {
                this.strictInsertFill(metaObject, "createBy", String.class, sysUser.getUserId().toString());
            }
            if (metaObject.hasGetter("updateBy")) {
                this.strictInsertFill(metaObject, "updateBy", String.class, sysUser.getUserId().toString());
            }
        }
        if (metaObject.hasGetter("delFlag")) {
            this.strictInsertFill(metaObject, "delFlag", Integer.class, 0); //新增数据时，默认为0  采用了注入器
        }
        long timestamp = System.currentTimeMillis()/ 1000;
        Date now = new Date();
        if (metaObject.hasGetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", Date.class, now);
        }
        if (metaObject.hasGetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", Date.class, now);
        }
        if (metaObject.hasGetter("createTimeTs")) {
            this.strictInsertFill(metaObject, "createTimeTs", Long.class, timestamp);
        }
        if (metaObject.hasGetter("updateTimeTs")) {
            this.strictInsertFill(metaObject, "updateTimeTs", Long.class, timestamp);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        YamiSysUser sysUser = null;
        try {
            sysUser = SecurityUtils.getSysUser();
        }catch (Exception e){
            log.debug("前端场景");
        }

        if (sysUser != null) {
            if (metaObject.hasGetter("updateBy")) {
                this.strictInsertFill(metaObject, "updateBy", String.class, sysUser.getUserId().toString());
            }
        }
        Date now = new Date();
        long timestamp = System.currentTimeMillis()/ 1000;
        if (metaObject.hasGetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", Date.class, now);
        }
        if (metaObject.hasGetter("updateTimeTs")) {
            this.strictInsertFill(metaObject, "updateTimeTs", Long.class, timestamp);
        }
    }
}
