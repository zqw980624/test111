package com.yami.trading.dao.data;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.trading.bean.data.domain.RobotModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 机器人k线图模型表 Mapper 接口
 * </p>
 *
 * @author HT
 * @since 2023-05-09 20:52:32
 */
@Mapper
public interface RobotModelMapper extends BaseMapper<RobotModel> {

}
