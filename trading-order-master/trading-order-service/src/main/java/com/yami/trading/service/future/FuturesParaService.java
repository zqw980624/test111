package com.yami.trading.service.future;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.dao.future.FuturesParaMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 交割合约管理Service
 *
 * @author lucas
 * @version 2023-04-08
 */
@Service
@Transactional
// todo cache
public class FuturesParaService extends ServiceImpl<FuturesParaMapper, FuturesPara> {
    public List<FuturesPara> getBySymbolSort(String symbol) {
        QueryWrapper<FuturesPara> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("symbol", symbol);
        List<FuturesPara> list = list(queryWrapper);
        return sortPara(list);

    }

    private List<FuturesPara> sortPara(List<FuturesPara> list) {
        if (CollectionUtils.isEmpty(list))
            return list;
        // 列表按 s，m，h，d 排序
        String[] regulation = {FuturesPara.TIMENUM_SECOND, FuturesPara.TIMENUM_MINUTE, FuturesPara.TIMENUM_HOUR,
                FuturesPara.TIMENUM_DAY};
        final List<String> regulationOrder = Arrays.asList(regulation);
        Collections.sort(list, new Comparator<FuturesPara>() {
            @Override
            public int compare(FuturesPara o1, FuturesPara o2) {
                // TODO Auto-generated method stub
                int unitSort = regulationOrder.indexOf(o1.getTimeunit()) - regulationOrder.indexOf(o2.getTimeunit());
                int timeSort = (int) (o1.getTimenum() - o2.getTimenum());
                return unitSort == 0 ? timeSort : unitSort;
            }
        });
        return list;
    }


    public FuturesPara bulidOne(FuturesPara futuresPara) {
        BigDecimal profitRatio = futuresPara.getProfitRatio();
        BigDecimal profitRatioMax = futuresPara.getProfitRatioMax();
        BigDecimal val = new BigDecimal("0.01");
        int scale = 0;
        if (profitRatio.compareTo(val) < 0 || profitRatioMax.compareTo(val) < 0) {
            scale = 1;
        }
        String profitRatioFront = profitRatio.setScale(scale, RoundingMode.HALF_UP) + "-" + profitRatioMax.setScale(scale, RoundingMode.HALF_UP);
        futuresPara.setProfitRatioFront(profitRatioFront);
        BigDecimal unitMaxAmount =  futuresPara.getUnitMaxAmount().compareTo(BigDecimal.ZERO)<= 0?null: futuresPara.getUnitMaxAmount();
        futuresPara.setBuyMin(new BigDecimal(futuresPara.getUnitAmount()));
        futuresPara.setBuyMax(unitMaxAmount);
        return futuresPara;
    }
}
