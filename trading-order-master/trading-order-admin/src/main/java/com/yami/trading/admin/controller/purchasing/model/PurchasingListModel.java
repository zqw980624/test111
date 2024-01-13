package com.yami.trading.admin.controller.purchasing.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class PurchasingListModel  extends PageRequest {

    /**
     * 项目总类 1 全球ETF 2  能源ETF  3  黄金ETF  4.人工智能ETF
     */

    @ApiModelProperty("项目总类 1 全球ETF 2  能源ETF  3  黄金ETF  4.人工智能ETF")
    private  int projectType;

    /**
     * 申购项目名称
     */
    @ApiModelProperty("申购项目名称")
    private  String projectName;


    /**
     * 状态 0已下架 1 上架
     */
    @ApiModelProperty("状态 0 全部  1 申购中  2 已结束")
    private  int status;

}
