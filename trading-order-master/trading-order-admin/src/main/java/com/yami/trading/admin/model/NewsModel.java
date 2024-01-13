package com.yami.trading.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ApiModel
public class NewsModel {
    @ApiModelProperty("修改传id  新增不传")
    private String id;
    @ApiModelProperty("资金密码")
    @NotBlank
    private String loginSafeword;
    @ApiModelProperty("标题")
    @NotBlank
    private String title;
    @ApiModelProperty("图片地址")
    private String imgUrl;
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
    @ApiModelProperty("是否弹出")
    private boolean popUp;
    @ApiModelProperty("滚动新闻")
    private boolean index;
    /**
     * 内容
     */
    @ApiModelProperty("内容")
    @NotBlank
    private String content;
    @ApiModelProperty("语言")
    @NotBlank
    private String language;
    @ApiModelProperty("开始时间  2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @ApiModelProperty("结束时间  2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @ApiModelProperty("玩家/代理UID（空是全局）")
    private String userCode;
}
