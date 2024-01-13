package com.yami.trading.bean.item.domain;

import java.math.BigDecimal;
import java.util.Date;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import com.yami.trading.common.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 简况Entity
 *
 * @author lucas
 * @version 2023-05-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_item_summary")
public class ItemSummary extends UUIDEntity {

    private static final long serialVersionUID = 1L;


    @TableField(exist = false)
    private String symbolName;
    /**
     * 市净率
     */
    @ApiModelProperty("市净率")
    @JSONField(name = "PB_NEW_NOTICE")
    private String pbTtm;
    /**
     * 每股净资产
     */
    @ApiModelProperty("每股净资产")
    @JSONField(name = "BPS")
    private String bps;
    /**
     * 股息率
     */
    @ApiModelProperty("股息率")
    private String dividendRate;
    /**
     * 净利率
     */
    @ApiModelProperty("净利率")
    private String netProfitRatio;


    /**
     * 营业额同比
     */
    @ApiModelProperty("营业额同比")
    private String operateIncomeQoq;
    /**
     * 占溢利同比
     */
    @ApiModelProperty("占溢利同比")
    private String yoyProfitShare;
    /**
     * 港股市值
     */
    @ApiModelProperty("港股市值")
    private String hkskMarketCap;
    /**
     * 总市值
     */
    @ApiModelProperty("总市值")
    @JSONField(name = "TOTAL_MARKET_CAP")
    private String skMarketCap;
    /**
     * 市盈率
     */
    @ApiModelProperty("市盈率")
    private String peTtm;
    /**
     * 每股股息
     */
    @ApiModelProperty("每股股息")
    private String dpsHkd;
    /**
     * 毛利率
     */
    @ApiModelProperty("毛利率")
    @JSONField(name = "XSMLL")
    private String grossProfitMargin;
    /**
     * 营业额
     */
    @ApiModelProperty("营业额")
    private String operateIncome;
    /**
     * 股东应占溢利
     */
    @ApiModelProperty("股东应占溢利")
    private String shhareholdersEarnings;
    /**
     * 港股股本
     */
    @ApiModelProperty("港股股本")
    private String hongKongStockCapital;
    /**
     * 总股本
     */
    @ApiModelProperty("总股本")
    @JSONField(name = "TOTAL_SHARE")
    private String commonAcs;
    /**
     * 代码
     */
    @ApiModelProperty("代码")
    private String symbol;
    /**
     * 证券代码
     */
    @ApiModelProperty("证券代码")
    private String secucode;
    /**
     * 证券类型
     */
    @ApiModelProperty("证券类型/基金类型")
    private String securityType;
    /**
     * isin
     */
    @ApiModelProperty("isin")
    private String isinCode;
    /**
     * 上市时间
     */
    @ApiModelProperty("上市时间")
    private String listingDate;
    /**
     * 发行价格
     */
    @ApiModelProperty("发行价格")
    private String issuePrice;
    /**
     * 板块
     */
    @ApiModelProperty("板块")
    private String board;
    /**
     * 年结日
     */
    @ApiModelProperty("年结日")
    private String yearSettleDay;
    /**
     * 发行量
     */
    @ApiModelProperty("发行量")
    private String issuedCommonShares;
    /**
     * 每手股数
     */
    @ApiModelProperty("每手股数")
    private String tradeUnit;
    /**
     * 每股面值
     */
    @ApiModelProperty("每股面值")
    private String parValuePerShare;
    /**
     * 公司名称
     */
    @ApiModelProperty("公司名称/基金公司")
    private String orgName;
    /**
     * A股代码
     */
    @ApiModelProperty("A股代码")
    private String strCodeA;

    /**
     * A股代码
     */
    @ApiModelProperty("A股简称")
    private String strNameA;

    /**
     * 英文名称
     */
    @ApiModelProperty("英文名称")
    private String enName;
    /**
     * 所属行业
     */
    @ApiModelProperty("所属行业")
    private String belongIndustry;
    /**
     * 港股股份
     */
    @ApiModelProperty("港股股份")
    private String hongKongStockShares;
    /**
     * 注册资本
     */
    @ApiModelProperty("注册资本")
    private String registeredCapital;
    /**
     * 主席
     */
    @ApiModelProperty("主席")
    private String chairman;
    /**
     * 公司秘书
     */
    @ApiModelProperty("公司秘书")
    private String secretary;
    /**
     * 成立日期
     */
    @ApiModelProperty("成立日期")
    private String foundDate;


    /**
     * 员工人数
     */
    @ApiModelProperty("员工人数")
    private String empNum;
    /**
     * 公司业务
     */
    @ApiModelProperty("公司简介")
    private String orgProfile;

    @ApiModelProperty("经营范围")
    private String businessScope;
    /**
     * 注册办事处
     */
    @ApiModelProperty("地址")
    private String address;

    
    @ApiModelProperty("注册地址")
    private String regAddress;
    /**
     * 公司总部
     */
    @ApiModelProperty("公司总部")
    private String companyHeadquarters;
    /**
     * 股份过户登记处
     */
    @ApiModelProperty("股份过户登记处")
    private String shareRegistrar;
    /**
     * 核数师
     */
    @ApiModelProperty("核数师")
    private String accountFirm;
    /**
     * 主要往来银行
     */
    @ApiModelProperty("主要往来银行")
    private String mainCorrespondentBank;
    /**
     * 法律顾问
     */
    @ApiModelProperty("法律顾问")
    private String legalAdvisor;
    /**
     * 公司网址
     */
    @ApiModelProperty("公司网址")
    private String orgWeb;
    /**
     * 电邮地址
     */
    @ApiModelProperty("电邮地址")
    private String orgEmail;
    /**
     * 电话号码
     */
    @ApiModelProperty("电话号码")
    private String orgTel;
    /**
     * 传真号码
     */
    @ApiModelProperty("传真号码")
    private String orgFax;
    /**
     * 每股收益
     */
    @JSONField(name = "EPSJB")
    @ApiModelProperty("每股收益")
    private String eps;
//    /**
//     * 每股净资产
//     */
//    @ApiModelProperty("每股净资产")
//    private String navPerShare;
    /**
     * 营业总收入
     */
    @ApiModelProperty("营业总收入")
    @JSONField(name = "TOTALOPERATEREVE")
    private String totalOperatingRevenue;
    /**
     * 总营同比
     */
    @ApiModelProperty("总营同比")
    @JSONField(name = "TOTALOPERATEREVETZ")
    private String yoyTotalOperatingRevenue;
    /**
     * 净利润
     */
    @ApiModelProperty("净利润")
    @JSONField(name = "PARENTNETPROFIT")
    private String netProfit;
    /**
     * 净利润同比
     */
    @ApiModelProperty("净利润同比")
    @JSONField(name = "PARENTNETPROFITTZ")
    private String yoyNetProfit;
    /**
     * 净资产收益率
     */
    @ApiModelProperty("净资产收益率")
    @JSONField(name = "ROEJQ")
    private String roe;
    /**
     * 负债率
     */
    @ApiModelProperty("负债率")
    @JSONField(name = "ZCFZL")
    private String debtRatio;
    /**
     * 流通A股
     */
    @ApiModelProperty("流通A股")
    @JSONField(name = "FREE_SHARE")
    private String circulatingAShares;
    /**
     * 流通A市值
     */
    @ApiModelProperty("流通A市值")
    private String circulatingAShareMarketCapitalization;


    /**
     * 质押比例
     */
    @ApiModelProperty("质押比例")
    private String loanToValueRatio;
    /**
     * 商誉规模
     */
    @ApiModelProperty("商誉规模")
    private String goodwillScale;
    /**
     * 所属区域
     */
    @ApiModelProperty("所属区域")
    private String region;
    /**
     * 所属概念
     */
    @ApiModelProperty("所属概念/行业概念")
    private String belongingConcept;
    /**
     * 法人代表
     */
    @ApiModelProperty("法人代表")
    private String legalRepresentative;
    /**
     * 总经理
     */
    @ApiModelProperty("总经理/基金经理")
    private String managingDirector;
    /**
     * 管理层人数
     */
    @ApiModelProperty("管理层人数")
    private String managementTeamSize;
    /**
     * 审计机构
     */
    @ApiModelProperty("审计机构 ")
    private String auditOrganization;
    /**
     * 募资净额
     */
    @ApiModelProperty("募资净额")
    private String netFundsRaised;
    /**
     * 发行市盈率
     */
    @ApiModelProperty("发行市盈率")
    private String peRatioAtIpo;
    /**
     * 网上中签率
     */
    @ApiModelProperty("网上中签率")
    private String onlineDrawingRate;
    /**
     * 收入总额
     */
    @ApiModelProperty("收入总额")
    private String totalIncome;
    /**
     * 收入总额同比
     */
    @ApiModelProperty("收入总额同比")
    private String yoyTotalIncome;
    /**
     * 归母净利润同比
     */
    @ApiModelProperty("归母净利润同比")
    private String profitParentCompany;
    /**
     * 归母净利润
     */
    @ApiModelProperty("归母净利润")
    private String yoyProfitParentCompany;
    /**
     * 周息率
     */
    @ApiModelProperty("周息率")
    private String weeklyInterestRate;
    /**
     * 上市场所
     */
    @ApiModelProperty("上市场所 ")
    private String stockExchange;
    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @ApiModelProperty("基金规模")
    private String fundSize;

    @ApiModelProperty("基金份额")
    private String fundShares;

    @ApiModelProperty("跟踪指数")
    private String indexTracking;

    @ApiModelProperty("风险等级")
    private String riskLevel;

    @ApiModelProperty("业绩比较基准")
    private String performanceBenchmark;

    @ApiModelProperty("投资类型")
    private String investmentType;

    @ApiModelProperty("交易方式")
    private String tradingMethod;

    @ApiModelProperty("交易费用")
    private String transactionFee;

    @ApiModelProperty("交易佣金")
    private String tradingCommission;

    @ApiModelProperty("印花税")
    private String stampDuty;

    @ApiModelProperty("管理费用率")
    private String managementFeeRate;

    @ApiModelProperty("托管费用率")
    private String custodianFeeRate;

    @ApiModelProperty("投资理念")
    private String investmentPhilosophy;

    @ApiModelProperty("风险特征")
    private String riskCharacteristics;
    @TableField("`lang`")
    private String lang;
    private String translate;
}
