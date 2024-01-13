package com.yami.trading.bean.cms.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class NewsDto {

    private  String uuid;

    /**
     * 标题
     */
    @ApiModelProperty("标题")
    private String title;
    /**
     * 内容
     */

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("语言")
    private String language;


    @TableField(exist = false)
    private String languageText;
    /**
     * 首页弹出新闻，如果为true弹出
     */
    private boolean indexTop = false;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 玩家ID/代理ID/空（空是全局配置）
     */
    @ApiModelProperty("玩家ID/代理ID/空（空是全局配置）")
    private String userId;

    /**
     * 图片地址
     */
    @ApiModelProperty("图片地址")
    private String imgUrl;

    private String httpImgUrl;
    /**
     * 图片跳转链接
     */
    @ApiModelProperty("图片跳转链接")
    private String imgJumpUrl;

    /**
     * 可否点击
     */
    @ApiModelProperty("可否点击")
    private boolean click;

    /**
     * 是否弹出
     */
    @ApiModelProperty("是否弹出")
    private boolean popUp;


    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("usercode")
    private  String userCode;

    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("推荐人用户名")
    private  String recomUserName;
    @ApiModelProperty("推荐人code")
    private  String recomUserCode;

    private  String roleName;

}
