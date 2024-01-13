package com.yami.trading.bean.chat.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 聊天用户DTO
 * @author lucas
 * @version 2023-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OtcMessageUserDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * TOTAL_MSG
     */
	@NotNull(message="TOTAL_MSG不能为空")
	private Long totalMsg;
	/**
     * USER_UNREADMSG
     */
	@NotNull(message="USER_UNREADMSG不能为空")
	private Long userUnreadmsg;
	/**
     * CUSTOMER_UNREADMSG
     */
	private Long customerUnreadmsg;
	/**
     * UPDATETIME
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="UPDATETIME不能为空")
	private Date updatetime;
	/**
     * DELETE_STATUS
     */
	private Long deleteStatus;
	/**
     * REMARKS
     */
	private String remarks;
	/**
     * IP
     */
	private String ip;
	/**
     * ORDER_NO
     */
	private String orderNo;
	/**
     * create_time_ts
     */
	private Long createTimeTs;

}
