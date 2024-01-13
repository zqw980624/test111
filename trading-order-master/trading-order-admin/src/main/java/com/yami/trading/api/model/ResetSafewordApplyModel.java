package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ResetSafewordApplyModel {
    @ApiModelProperty("证件正面照")
    @NotBlank
    private  String idcardPathFront;

    @ApiModelProperty("证件背面照")
    @NotBlank
    private  String idcardPathBack;

    @ApiModelProperty("正面手持证件照")
    @NotBlank
    private  String idcardPathHold;

    @ApiModelProperty("资金密码")
    @NotBlank
    private  String safeword;


    @ApiModelProperty(" 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；")
    @Min(0)
    private  int operate;

    @ApiModelProperty("留言")
    private  String remark;


}
