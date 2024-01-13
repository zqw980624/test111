package com.yami.trading.bean.chat.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * im消息Entity
 * @author lucas
 * @version 2023-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_otc_onlinechat_message")
public class OtcOnlineChatMessage extends UUIDEntity implements Comparable<OtcOnlineChatMessage>{

	private static final long serialVersionUID = 1L;

	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * SEND_RECEIVE
     */
	private String sendReceive;
	/**
     * CONTENT_TYPE
     */
	private String contentType;
	/**
     * CONTENT
     */
	private String content;
	/**
     * CREATE_TIME
     */
	@TableField(fill = FieldFill.INSERT)
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
	private Integer deleteStatus;
	/**
     * create_time_ts
     */
	@TableField(fill = FieldFill.INSERT)
	private Long createTimeTs;


	/**
	 * 逻辑删除标记
	 */
	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	private Integer delFlag;

	@Override
	public int compareTo(OtcOnlineChatMessage onlineChatMessage) {

		if (this.createTime.after(onlineChatMessage.getCreateTime())) {
			return 1;
		} else if (this.createTime.before(onlineChatMessage.getCreateTime())) {
			return -1;
		}
		return 0;
	}

}
