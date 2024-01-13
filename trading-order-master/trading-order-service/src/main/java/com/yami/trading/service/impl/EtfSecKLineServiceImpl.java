package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.yami.trading.dao.etf.mapper.EtfSecKLineMapper;
import com.yami.trading.service.etf.EtfSecKLineService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * Etf秒级k线图 服务实现类
 * </p>
 *
 * @author HT
 * @since 2023-05-18 17:27:13
 */
@Service
public class EtfSecKLineServiceImpl extends ServiceImpl<EtfSecKLineMapper, EtfSecKLine> implements EtfSecKLineService {

    @Override
    public void deleteOverdueEtfKLine() {
        Calendar calendar = Calendar.getInstance();
        // 将时间设置为前一天
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date previousDate = calendar.getTime();


        QueryWrapper<EtfSecKLine> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.lt("ts", previousDate.getTime());
        boolean remove = this.remove(objectQueryWrapper);
    }

}
