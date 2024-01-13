package com.yami.trading.bean.chat.domain;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 聊天用户Entity
 * @author lucas
 * @version 2023-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_message_user")
public class MessageUser extends UUIDEntity implements Comparable<MessageUser> {

	private static final long serialVersionUID = 1L;
	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * TOTAL_MSG
     */
	private Long totalMsg;
	/**
     * USER_UNREADMSG
     */
	private Integer userUnreadmsg;
	/**
     * CUSTOMER_UNREADMSG
     */
	private Integer customerUnreadmsg;
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
     * TARGET_USERNAME
     */
	private String targetUsername;

	/**
	 * 创建日期
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
	public int compareTo(MessageUser messageUser) {
		if (this.updatetime.after(messageUser.getUpdatetime())) {
			return -1;
		} else if (this.updatetime.before(messageUser.getUpdatetime())) {
			return 1;
		}
		return 0;
	}


}
