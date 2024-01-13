package com.yami.trading.bean.chat.query.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 聊天用户DTO
 * @author lucas
 * @version 2023-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OtcMessageUserQuery{

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * PARTY_ID
     */
    @Query(type = QueryType.EQ)
	private String partyId;
	/**
     * TOTAL_MSG
     */
	@NotNull(message="TOTAL_MSG不能为空")
    @Query(type = QueryType.EQ)
	private Long totalMsg;
	/**
     * USER_UNREADMSG
     */
	@NotNull(message="USER_UNREADMSG不能为空")
    @Query(type = QueryType.EQ)
	private Long userUnreadmsg;
	/**
     * CUSTOMER_UNREADMSG
     */
    @Query(type = QueryType.EQ)
	private Long customerUnreadmsg;
	/**
     * UPDATETIME
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="UPDATETIME不能为空")
    @Query(type = QueryType.BETWEEN)
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
    @Query(type = QueryType.EQ)
	private String orderNo;
	/**
     * create_time_ts
     */
    @Query(type = QueryType.EQ)
	private Long createTimeTs;
   

}
