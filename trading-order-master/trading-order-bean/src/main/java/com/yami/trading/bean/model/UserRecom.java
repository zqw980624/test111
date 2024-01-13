package com.yami.trading.bean.model;



import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;

import java.util.*;

/**
 * 用户推荐人
 * 
 */

@Data
@TableName("tz_user_recom")
public class UserRecom extends BaseEntity {

	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 推荐人userid
	 */
	private  String recomUserId;
}
