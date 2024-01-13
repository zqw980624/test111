package com.yami.trading.bean.cms;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_cms")
public class Cms  extends UUIDEntity {


    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("模块")
    private String model;

    @ApiModelProperty("时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("语言")
    private String language;
    @TableField(exist = false)
    private String languageText;

    /**
     * 业务代码， 同种内容 不同语言下的code相同
     */
    private String contentCode;
    @TableField(exist = false)
    private String createTimeStr;
}
