package com.yami.trading.bean.cms;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.Date;

import com.yami.trading.common.domain.UUIDEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author lucas
 * @since 2023-06-19 23:44:55
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_infomation")
public class Infomation extends UUIDEntity {


    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    @TableField("`description`")
    private String description;

    /**
     * 带标签正文
     */
    private String content;

    /**
     * 来源
     */
    @TableField("`source`")
    private String source;

    private String createdAt;

    private String img;

    /**
     * 元数据id
     */
    private String dataId;

    /**
     * 1是7*24,2是论坛热点
     */
    @TableField("`type`")
    private String type;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField("`lang`")
    private String lang;
    private String translate;

}
