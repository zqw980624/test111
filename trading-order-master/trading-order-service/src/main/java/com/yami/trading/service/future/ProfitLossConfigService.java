package com.yami.trading.service.future;
import com.yami.trading.bean.model.User;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.future.dto.FuturesParaDTO;
import com.yami.trading.bean.future.dto.ProfitLossConfigDTO;
import com.yami.trading.bean.future.query.FuturesParaQuery;
import com.yami.trading.bean.future.query.ProfitLossConfigQuery;
import com.yami.trading.dao.future.ProfitLossConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.future.domain.ProfitLossConfig;

import java.util.List;

/**
 * 交割合约Service
 * @author lucas
 * @version 2023-04-08
 */
@Service
@Transactional
public class ProfitLossConfigService extends ServiceImpl<ProfitLossConfigMapper, ProfitLossConfig> {


    public IPage<ProfitLossConfigDTO> listRecord(Page page, ProfitLossConfigQuery query) {
        return baseMapper.listRecord(page, query);
    }

    public ProfitLossConfig findByPartyId(String partyId) {
        QueryWrapper<ProfitLossConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("party_id", partyId);
        List<ProfitLossConfig> list = list(queryWrapper);
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);

    }


}