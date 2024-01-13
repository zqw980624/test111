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
 * im消息DTO
 * @author lucas
 * @version 2023-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TOtcOnlinechatMessageDTO extends BaseDTO {

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
     * SEND_RECEIVE
     */
	@NotNull(message="SEND_RECEIVE不能为空")
	private String sendReceive;
	/**
     * CONTENT_TYPE
     */
	@NotNull(message="CONTENT_TYPE不能为空")
	private String contentType;
	/**
     * CONTENT
     */
	private String content;
	/**
     * CREATE_TIME
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="CREATE_TIME不能为空")
	private Date createTime;
	/**
     * USERNAME
     */
	private String username;
	/**
     * IP
     */
	private String ip;
	/**
     * ORDER_NO
     */
	private String orderNo;
	/**
     * DELETE_STATUS
     */
	private Long deleteStatus;
	/**
     * create_time_ts
     */
	private Long createTimeTs;

}
