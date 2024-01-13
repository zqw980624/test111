package com.yami.trading.bean.future.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交割合约DTO
 *
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfitLossConfigDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("用户名")
    private String userName;

    private String userCode;
    @ApiModelProperty("角色名称")
    private String roleName;
    /**
     * PARTY_ID
     */
    private String partyId;
    /**
     * REMARK
     */
    private String remark;
    /**
     * TYPE
     */
    private String type;

    private String typeName;



    public String getTypeName() {
        typeName = "-";
        if ("profit".equals(type)) {
            typeName = "盈利";
        }
        if ("loss".equals(type)) {
            typeName = "亏损";
        }
        if ("buy_profit".equals(type)) {
            typeName = "买多盈利";
        }
        if ("sell_profit".equals(type)
        ) {
            typeName = "买空盈利";
        }
        if ("buy_profit_sell_loss".equals(type)) {
            typeName = "买多盈利并且买空亏损";
        }
        if ("sell_profit_buy_loss".equals(type)) {
            typeName = "买空盈利并且买多亏损";
        }
        return typeName;
    }

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 时间戳
     */
    private Long createTimeTs;
    /**
     * 更新时间戳
     */
    private Long updateTimeTs;

}
