package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class BannerModel {


    @ApiModelProperty("修改传id  新增不传")
    private String id;

    @ApiModelProperty("资金密码")
    @NotBlank
    private String loginSafeword;

    @ApiModelProperty("语言")
    private String language;

    /**
     * 业务代码， 同种内容 不同语言下的code相同
     */
    @ApiModelProperty("业务代码， 同种内容 不同语言下的code相同")
    private String contentCode;
    /**
     * 展示图片
     */
    @ApiModelProperty("展示图片")
    private String image;
    /**
     * 访问路径
     */
    @ApiModelProperty("访问路径")
    private String url;
    /**
     * 是否展示
     */
    @ApiModelProperty("是否展示")
    private int onShow;
    /**
     * 排列顺序（数字相同按时间排，越小排越前）
     */
    @ApiModelProperty("排列顺序（数字相同按时间排，越小排越前）")
    private int sortIndex;
    /**
     * 类型，top:顶部展示，other:其他地方展示,poster:弹窗海报
     */
    @ApiModelProperty("类型，top:顶部展示，other:其他地方展示,poster:弹窗海报")
    private String model;
    /**
     * 是否可以点击跳转
     */

    @ApiModelProperty("是否可以点击跳转")
    private int click;

}
