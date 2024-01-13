package com.yami.trading.bean.item.query;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用户自选DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemUserOptionalQuery{

	private static final long serialVersionUID = 1L;


	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * SYMBOL
     */
	private String symbol;
	/**
     * 创建时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**
     * 更新时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**
     * 备注信息
     */
	private String remarks;
   

}
