package com.yami.trading.admin.task.future;


import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.future.FuturesOrderService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FuturesOrderCreateRecomService {
    @Autowired
    protected FuturesOrderService futuresOrderService;
    @Autowired
    protected SysparaService sysparaService;
    @Autowired
    protected UserRecomService userRecomService;
    @Autowired
    protected UserService partyService;
    @Autowired
    protected WalletService walletService;

    /**
     * 计算推荐人收益
     */
    protected Map<String, BigDecimal> cacheRecomProfit = new ConcurrentHashMap<String, BigDecimal>();

    /**
     * 计算前一日购买产品的订单
     */
    public void computeRecom() {
        String futures_bonus_parameters = sysparaService.find("futures_bonus_parameters").getSvalue();
        if (StrUtil.isEmpty(futures_bonus_parameters)) {
            return;
        }
        cacheRecomProfit.clear();
        int pageSize = 1000;
        int pageNo = 1;
        String date = DateUtils.getDateStr(DateUtils.addDate(new Date(), -1));
        while (true) {
            List<FuturesOrder> list = futuresOrderService.queryByDate(pageNo, pageSize, date);
            if (list.isEmpty()) {
                break;
            }
            for (FuturesOrder order : list) {
                handleCacheRecom(order, futures_bonus_parameters);
            }
            pageNo++;
        }
        saveRecomProfit();
    }

    /**
     * 购买推荐奖励
     *
     * @param entity
     */
    public void handleCacheRecom(FuturesOrder entity, String futures_bonus_parameters) {
        List<UserRecom> list_parents = this.userRecomService.getParents(entity.getPartyId());

        if (CollectionUtils.isNotEmpty(list_parents)) {
            String[] futures_bonus_array = futures_bonus_parameters.split(",");
            int loop = 0;
            int loopMax = futures_bonus_array.length;
            for (int i = 0; i < list_parents.size(); i++) {
                if (loop >= loopMax) {
                    break;
                }
                User party_parent = this.partyService.getById(list_parents.get(i).getRecomUserId());
                if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRoleName())) {
                    continue;
                }
                loop++;
                BigDecimal pip_amount = new BigDecimal(futures_bonus_array[loop - 1]);
                BigDecimal get_money = new BigDecimal(entity.getVolume() * pip_amount.doubleValue());

                if (cacheRecomProfit.containsKey(party_parent.getUserId())) {
                    cacheRecomProfit.put(party_parent.getUserId(),
                            cacheRecomProfit.get(party_parent.getUserId()).add(get_money));
                } else {
                    cacheRecomProfit.put(party_parent.getUserId().toString(), get_money);
                }
            }

        }
    }

    public void saveRecomProfit() {
        if (cacheRecomProfit.isEmpty()) {
            return;
        }
        for (Entry<String, BigDecimal> entry : cacheRecomProfit.entrySet()) {
            walletService.updateMoney("",entry.getKey(), entry.getValue(), BigDecimal.ZERO,
                    Constants.MONEYLOG_CONTENT_REWARD, Constants.WALLET, Constants.MONEYLOG_CONTENT_REWARD, "下级购买交割，佣金奖励");
            ThreadUtils.sleep(200);
        }
    }


}
