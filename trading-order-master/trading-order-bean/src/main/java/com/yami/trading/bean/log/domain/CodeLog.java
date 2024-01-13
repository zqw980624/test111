package com.yami.trading.bean.log.domain;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@TableName("t_code_log")
@Data
public class CodeLog extends UUIDEntity {

	private static final long serialVersionUID = 7008440232393696319L;

	/**
	 * 日志归属
	 */
	private String userId;
	/**
	 * 日志归属
	 */
	private String userName;

	/**
	 * 日志
	 */
	private String log;

	/**
	 * TARGET
	 */
	private String target;
	

	// 创建时间
	private Date createTime;




}


