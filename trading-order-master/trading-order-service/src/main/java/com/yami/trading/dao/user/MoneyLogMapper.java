package com.yami.trading.dao.user;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.ChannelBlockchain;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.user.dto.SumBSalanceDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.Map;

public interface MoneyLogMapper extends BaseMapper<MoneyLog> {


   Page pageMoneyLog(Page page, @Param("userCode") String userId,
                     @Param("rolename")  String rolename, @Param("startTime") Date startTime,
                     @Param("endTime") Date endTime,
                     @Param("userName") String userName,
                     @Param("log") String log,@Param("category") String category);

   SumBSalanceDto sumBSalance(@Param("category") String category,
                              @Param("contentType") String contentType,
                              @Param("userId") String userId);
}
