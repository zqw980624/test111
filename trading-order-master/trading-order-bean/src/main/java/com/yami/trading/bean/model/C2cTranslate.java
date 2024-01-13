package com.yami.trading.bean.model;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_c2c_translate")
public class C2cTranslate  extends UUIDEntity {

	private static final long serialVersionUID = -3956153479501477647L;

	/**
	 * 语言
	 */
	private String language;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 翻译
	 */
	private String translate;
	
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;



}
