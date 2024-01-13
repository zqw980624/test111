package com.yami.trading.service.contract;

import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class ContractOrderCalculationServiceImpl implements ContractOrderCalculationService {
    @Autowired
    private ItemService itemService;
    /**
     * 平仓线 110%（订金价值 /收益=110%）
     */
    public BigDecimal order_close_line = new BigDecimal("1.1");
    /**
     * 平仓方式 1全仓 2单个持仓
     */
    public int order_close_line_type = 1;
    @Autowired
    private ContractOrderService contractOrderService;
    @Qualifier("dataService")
    @Autowired
    @Lazy
    private DataService dataService;
    @Autowired
    private WalletService walletService;
    private SysparaService sysparaService;

    @Transactional
    public void saveCalculation(String order_no, List<ContractOrder> partyContractOrders) {

        try {
            ContractOrder order = contractOrderService.findByOrderNoRedis(order_no);
            if (order == null || !ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
                /**
                 * 状态已改变，退出处理
                 */
                return;
            }
            List<Realtime> list = this.dataService.realtime(order.getSymbol());
            if (list.size() == 0) {
                return;
            }
            Realtime realtime = list.get(0);

            BigDecimal close = realtime.getClose();

            BigDecimal add = order.getTradeAvgPrice().add(order.getPips());
            BigDecimal subtract = order.getTradeAvgPrice().subtract(order.getPips());
            if (ContractOrder.DIRECTION_BUY.equals(order.getDirection())) {

                /*
                 * 0 买涨
                 */
                if (close.compareTo(add) >= 0) {
                    settle(order, "profit", close, partyContractOrders);
                }

                if (close.compareTo(subtract) <= 0) {
                    settle(order, "loss", close, partyContractOrders);
                }

            } else {
                /*
                 * 1 买跌
                 */
                if (close.compareTo(subtract) <= 0) {
                    settle(order, "profit", close, partyContractOrders);
                }
                if (close.compareTo(add) >= 0) {
                    settle(order, "loss", close, partyContractOrders);
                }
            }
        } catch (Throwable e) {
            log.error("ContractOrderCalculatio run fail", e);
        }

    }

    /**
     * 盈亏计算
     *
     * @param profit_loss  profit 盈 loss亏
     * @param currentPrice 当前点位
     */
    public void settle(ContractOrder order, String profit_loss, BigDecimal currentPrice, List<ContractOrder> partyContractOrders) {
        /**
         * 偏差点位
         */
        BigDecimal point = currentPrice.subtract(order.getTradeAvgPrice()).abs().divide(order.getPips(), 10, RoundingMode.HALF_UP);
        /*
         * 根据偏 差点数和手数算出盈亏金额
         */
        BigDecimal amount = order.getPipsAmount().multiply(point).multiply(order.getVolume());

        if ("profit".equals(profit_loss)) {
            /**
             * 盈 正数
             */
            order.setProfit(amount);
        } else if ("loss".equals(profit_loss)) {
            order.setProfit(amount.negate());
        }
        /**
         * 多次平仓价格不对，后续修
         */
        order.setCloseAvgPrice(currentPrice);
        this.contractOrderService.updateByIdBuffer(order);

        /**
         * 止盈价
         */
        BigDecimal profitStop = order.getStopPriceProfit();
        if (profitStop != null && profitStop.compareTo(BigDecimal.ZERO) > 0 && ContractOrder.DIRECTION_BUY.equals(order.getDirection())) {
            /*
             * 买涨
             */
            if (currentPrice.compareTo(profitStop) >= 0) {
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                return;
            }
        } else if (profitStop != null && profitStop.compareTo(BigDecimal.ZERO) > 0
                && ContractOrder.DIRECTION_SELL.equals(order.getDirection())) {
            /**
             * 买跌
             */
            if (currentPrice.compareTo(profitStop) <= 0) {
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                return;
            }
        }

        /**
         * 止亏线
         */
        BigDecimal loss_stop = order.getStopPriceLoss();

        if (loss_stop != null && loss_stop.compareTo(BigDecimal.ZERO) > 0 && ContractOrder.DIRECTION_BUY.equals(order.getDirection())) {
            /*
             * 买涨
             */
            if (currentPrice.compareTo(loss_stop) <= 0) {
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                return;

            }
        } else if (loss_stop != null && loss_stop.compareTo(BigDecimal.ZERO) > 0 && ContractOrder.DIRECTION_SELL.equals(order.getDirection())) {
            /**
             * 买跌
             */

            if (currentPrice.compareTo(loss_stop) >= 0) {
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                return;
            }
        }
        if (order_close_line_type == 1) {
            /**
             * 收益
             */
            BigDecimal profit = BigDecimal.ZERO;

            List<ContractOrder> list = partyContractOrders;
            for (int i = 0; i < list.size(); i++) {
                ContractOrder close_line = list.get(i);
                profit = profit.add(close_line.getProfit()).add(close_line.getDeposit());
            }


            Wallet wallet = this.walletService.findByUserId(order.getPartyId().toString());
            // 计算所有除自己以外的profit
            BigDecimal profitExptThis = profit.subtract(order.getProfit()).subtract(order.getDeposit());
            /**
             * profitAll+wallet<=0
             * profitAll<=wallet 强平
             * p1 +E (p2~pn) <=wallet
             * (currentPrice-tradavg)*pipAmount*volume/pips + depost1 <=wallet-E(p2~pn)
             */
            BigDecimal left = wallet.getMoney().negate().subtract(profitExptThis).subtract(order.getDeposit());
            BigDecimal overLine = (left.multiply(order.getPips()).divide(order.getPipsAmount(), 10, RoundingMode.HALF_UP)
                    .divide(order.getVolume(), 10, RoundingMode.HALF_UP));
            Integer decimal = itemService.getDecimal(order.getSymbol());
            BigDecimal forceClose = BigDecimal.ZERO;
            // 买多，从买价跌多少
            if (order.getDirection().equalsIgnoreCase(ContractOrder.DIRECTION_BUY)) {
                forceClose = order.getTradeAvgPrice().add(overLine).setScale(decimal, RoundingMode.HALF_UP);
                //买跌，涨到多少
            } else {
                forceClose = order.getTradeAvgPrice().subtract(overLine).setScale(decimal, RoundingMode.HALF_UP);
            }
            if (forceClose.compareTo(BigDecimal.ZERO) < 0) {
                forceClose = BigDecimal.ZERO;
            }
            order.setForceClosePrice(forceClose.toPlainString());
            this.contractOrderService.updateByIdBuffer(order);

            if (profit.add(wallet.getMoney()).compareTo(BigDecimal.ZERO) <= 0) {
                /**
                 * 触发全仓强平
                 */
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                ThreadUtils.sleep(100);
                for (int i = 0; i < list.size(); i++) {
                    ContractOrder close_line = list.get(i);
                    if (!order.getOrderNo().equals(close_line.getOrderNo())) {
                        try {
                            if (ContractLock.add(close_line.getOrderNo())) {
                                this.contractOrderService.saveClose(close_line.getPartyId().toString(),
                                        close_line.getOrderNo());
                            }
                        } catch (Exception e) {
                            log.error("error:", e);
                        } finally {
                            ContractLock.remove(close_line.getOrderNo());
                        }

                    }
                }

            }
        } else {
            BigDecimal divide = order.getDeposit().divide(order.getProfit().abs(), 10, RoundingMode.HALF_UP);
            if (order.getProfit().compareTo(BigDecimal.ZERO) < 0 && divide.compareTo(order_close_line.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP)) <= 0) {
                /**
                 * 低于系统默认平仓线，进行强平
                 */
                this.contractOrderService.saveClose(order.getPartyId().toString(), order.getOrderNo());
                return;
            }
        }

    }

//	@Override
//	public void afterPropertiesSet() throws Exception {
//		order_close_line = this.sysparaService.find("order_close_line").getDouble();
//		order_close_line_type = this.sysparaService.find("order_close_line_type").getInteger();
//
//	}

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public void setSysparaService(SysparaService sysparaService) {
        this.sysparaService = sysparaService;
    }

    public void setContractOrderService(ContractOrderService contractOrderService) {
        this.contractOrderService = contractOrderService;
    }

    public void setWalletService(WalletService walletService) {
        this.walletService = walletService;
    }

    public void setOrder_close_line(BigDecimal order_close_line) {
        this.order_close_line = order_close_line;
    }

    public void setOrder_close_line_type(int order_close_line_type) {
        this.order_close_line_type = order_close_line_type;
    }

}
