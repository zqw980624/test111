package com.yami.trading.bean.chat.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 聊天用户Entity
 * @author lucas
 * @version 2023-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_otc_message_user")
public class OtcMessageUser extends UUIDEntity implements Comparable<OtcMessageUser> {

	private static final long serialVersionUID = 1L;
	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * TOTAL_MSG
     */
	private int totalMsg;
	/**
     * USER_UNREADMSG
     */
	private int userUnreadmsg;
	/**
     * CUSTOMER_UNREADMSG
     */
	private int customerUnreadmsg;
	/**
     * UPDATETIME
     */
	private Date updatetime;
	/**
     * DELETE_STATUS
     */
	private Integer deleteStatus;
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
	@TableField(fill = FieldFill.INSERT)
	private Long createTimeTs;

	/**
	 * 发给指定用户
	 * 2023-07-20,启动报错,表中没有这个字段,注释掉
	 */
	@TableField(exist = false)
	private String targetUsername;
	@Override
	public int compareTo(OtcMessageUser messageUser) {
		if (this.updatetime.after(messageUser.getUpdatetime())) {
			return -1;
		} else if (this.updatetime.before(messageUser.getUpdatetime())) {
			return 1;
		}
		return 0;
	}

}
