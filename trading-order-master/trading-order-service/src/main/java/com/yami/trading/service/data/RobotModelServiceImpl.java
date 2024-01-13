package com.yami.trading.service.data;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.data.domain.RobotModel;
import com.yami.trading.dao.data.RobotModelMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 机器人k线图模型表 服务实现类
 * </p>
 *
 * @author HT
 * @since 2023-05-09 20:52:32
 */
@Service
public class RobotModelServiceImpl extends ServiceImpl<RobotModelMapper, RobotModel> implements RobotModelService {

}
