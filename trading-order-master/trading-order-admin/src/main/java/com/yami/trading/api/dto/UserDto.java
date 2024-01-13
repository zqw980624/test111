package com.yami.trading.api.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class UserDto {
    private  String token;

    /**
     * ID
     */
    private String userId;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;

    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    /**
     * 用户邮箱
     */
    @ApiModelProperty("用户邮箱")
    private String userMail;


    /**
     * 角色
     * MEMBER  正式用户
     * GUEST   演示用户
     * TEST    试用用户
     */
    @ApiModelProperty("MEMBER  正式用户  GUEST   演示用户 TEST    试用用户")
    private String roleName;

    /**
     * 用户code-UID
     */

    @ApiModelProperty(" 用户code-UID")
    private String usercode;


    /**
     * 手机号码
     */
    @ApiModelProperty("手机号码")
    private String userMobile;




    /**
     * 邮箱绑定
     */
    @ApiModelProperty("邮箱绑定")
    private boolean mailBind=false;

    /**
     * 手机绑定
     */
    @ApiModelProperty("手机绑定")
    private boolean userMobileBind=false;



    /**
     * 谷歌验证器是否绑定
     */
    @ApiModelProperty("谷歌验证器是否绑定 true false")
    private boolean googleAuthBind ;


    private  boolean identityverif;

    private  boolean advancedverif;

    private  String name;

    /**
     * 国籍
     */
    private String nationality;

    private  int kyc_status;

    private  int kyc_high_level_status;
}
