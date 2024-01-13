package com.yami.trading.bean.cms;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * 系统公告
 *
 */

@Data
@TableName("t_news")
public class News  extends UUIDEntity {
	private static final long serialVersionUID = -4670490376092518726L;


	/**
	 * 标题
	 */
	@ApiModelProperty("标题")
	private String title;
	/**
	 * 内容
	 */

	@ApiModelProperty("内容")
	private String content;

	@ApiModelProperty("语言")
	private String language;


	@TableField(exist = false)
	private String languageText;
	/**
	 * 首页弹出新闻，如果为true弹出
	 */
	private boolean indexTop = false;

	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty("创建时间")
	private Date createTime;

	/**
	 * 玩家ID/代理ID/空（空是全局配置）
	 */
	@ApiModelProperty("玩家ID/代理ID/空（空是全局配置）")
	private String userId;
	
	/**
	 * 图片地址
	 */
	@ApiModelProperty("图片地址")
	private String imgUrl;
	
	/**
	 * 图片跳转链接
	 */
	@ApiModelProperty("图片跳转链接")
	private String imgJumpUrl;
	
	/**
	 * 可否点击
	 */
	@ApiModelProperty("可否点击")
	private boolean click;
	
	/**
	 * 是否弹出
	 */
	@ApiModelProperty("是否弹出")
	private boolean popUp;


	@ApiModelProperty("开始时间")
	private Date startTime;

	@ApiModelProperty("结束时间")
	private Date endTime;

	@TableField(exist = false)
	private String createTimeStr;

	@TableField(exist = false)
	private boolean show;

}
