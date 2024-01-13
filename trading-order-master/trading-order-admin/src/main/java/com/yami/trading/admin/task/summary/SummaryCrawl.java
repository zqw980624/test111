package com.yami.trading.admin.task.summary;

import com.alibaba.fastjson.JSONObject;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.common.http.HttpHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.item.ItemSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Component
@Slf4j
public class SummaryCrawl {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemSummaryService itemSummaryService;
    public final static String A_stock_url_latest = "https://emweb.securities.eastmoney.com/PC_HSF10/OperationsRequired/PageAjax?code=";
    public final static String A_stock_url_company = "https://emweb.securities.eastmoney.com/PC_HSF10/CompanySurvey/PageAjax?code=";

    public final static String hk_stock_url_latest = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_HKF10_FN_MAININDICATOR&columns=HKF10_FN_MAININDICATOR_NEW&quoteColumns=&filter=(SECUCODE%3D%22{}.HK%22)&pageNumber=1&pageSize=1&sortTypes=-1&sortColumns=STD_REPORT_DATE&source=F10";
    public final static String getHk_stock_url_company = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_HKF10_INFO_ORGPROFILE&columns=SECUCODE%2CSECURITY_CODE%2CORG_NAME%2CORG_EN_ABBR%2CBELONG_INDUSTRY%2CFOUND_DATE%2CCHAIRMAN%2CSECRETARY%2CACCOUNT_FIRM%2CREG_ADDRESS%2CADDRESS%2CYEAR_SETTLE_DAY%2CEMP_NUM%2CORG_TEL%2CORG_FAX%2CORG_EMAIL%2CORG_WEB%2CORG_PROFILE%2CREG_PLACE&quoteColumns=&filter=(SECUCODE%3D%22{}.HK%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=F10";
    public final static String getHk_stock_url_se = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_HKF10_INFO_SECURITYINFO&columns=SECUCODE%2CSECURITY_CODE%2CSECURITY_NAME_ABBR%2CSECURITY_TYPE%2CLISTING_DATE%2CISIN_CODE%2CBOARD%2CTRADE_UNIT%2CTRADE_MARKET%2CGANGGUTONGBIAODISHEN%2CGANGGUTONGBIAODIHU&quoteColumns=&filter=(SECUCODE%3D%22{}.HK%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=F10";

    public static final String uk_stock_url_latest = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_DATA_MAININDICATOR&columns=SECUCODE%2CSECURITY_CODE%2CSECURITY_NAME_ABBR%2CREPORT_DATE%2CCURRENCY%2CPE_TTM%2CRATIO_EPS_TTM%2CDPS_USD%2CSALE_GPR%2CTURNOVER%2CHOLDER_PROFIT%2CISSUED_COMMON_SHARES%2CPB%2CBVPS%2CDIVIDEND_RATE%2CSALE_NPR%2CTURNOVER_YOY%2CHOLDER_PROFIT_YOY%2CTOTAL_MARKET_CAP%2CORG_TYPE%2CSECURITY_TYPE&quoteColumns=&filter=(SECUCODE%3D%22{}.O%22)&pageNumber=1&page";
   public static final String uk_stock_url_latest1 = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_DATA_MAININDICATOR&columns=ALL&quoteColumns=&filter=(SECUCODE%3D%22{}.N%22)&pageNumber=1&pageSize=200&sortTypes=-1&sortColumns=REPORT_DATE&source=INTLSECURITIE";

    public final static String uk_stock_url_company = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_INFO_ORGPROFILE&columns=SECUCODE%2CSECURITY_CODE%2CORG_CODE%2CSECURITY_INNER_CODE%2CORG_NAME%2CORG_EN_ABBR%2CBELONG_INDUSTRY%2CFOUND_DATE%2CCHAIRMAN%2CREG_PLACE%2CADDRESS%2CEMP_NUM%2CORG_TEL%2CORG_FAX%2CORG_EMAIL%2CORG_WEB%2CORG_PROFILE&quoteColumns=&filter=(SECURITY_CODE%3D%22{}%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=SECURITIES";
    public static final String uk_stock_url_se = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_INFO_SECURITYINFO&columns=SECUCODE%2CSECURITY_CODE%2CORG_CODE%2CSECURITY_INNER_CODE%2CSECURITY_TYPE%2CLISTING_DATE%2CISIN_CODE%2CTRADE_MARKET%2CYEAR_SETTLE_DAY%2CPAR_VALUE%2CCONVERT_RATIO%2CISSUE_PRICE%2CISSUE_NUM&quoteColumns=&filter=(SECUCODE%3D%22{}.O%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=SECURITIES&client=PC&v=0780594019016134";

    public static final String uk_stock_url_se1 = "https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_INFO_SECURITYINFO%3BRPT_USF10_INFO_ORGPROFILE&columns=SECUCODE%2CSECURITY_CODE%2CSECURITY_TYPE%2CLISTING_DATE%2CTRADE_MARKET%2CISSUE_PRICE%2CISSUE_NUM%2C%40SECUCODE%3B%40SECUCODE%2CORG_NAME%2CORG_EN_ABBR%2CBELONG_INDUSTRY%2CFOUND_DATE%2CCHAIRMAN%2CADDRESS%2CORG_WEB&quoteColumns=&filter=(SECUCODE%3D%22{}.N%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=SECURITIES";


    //@Scheduled(cron = "0 30 2 * * 1")
    public void crawl(){
        log.info("开始初始化股票简况数据");
        List<Item> list = itemService.list();

        for (Item item1 : list) {
            if (item1.getType().equalsIgnoreCase(Item.A_STOCKS)) {
                String symbol = item1.getSymbol();
                try {
                    crawlAStock(symbol);
                }catch (Exception e){
                    log.error("采集 {} 简况异常", symbol, e);
                }
            }
        }
        for (Item item1 : list) {
            if (item1.getType().equalsIgnoreCase(Item.HK_STOCKS)) {
                String symbol = item1.getSymbol();
                try {
                    crawlHkStock(symbol);
                }catch (Exception e){
                    log.error("采集 {} 简况异常", symbol, e);
                }
            }
        }
        for (Item item : list) {
            if (item.getType().equalsIgnoreCase(Item.US_STOCKS)) {
                String symbol = item.getSymbol();
                try {
                    crawlUsStock(symbol);
                }catch (Exception e){
                    log.error("采集 {} 简况异常", symbol, e);
                }
            }
        }
    }
    public void crawlAStock(String symbol) {

        String content = HttpHelper.sendGetHttp(A_stock_url_latest + symbol, "");
        JSONObject data = JSONObject.parseObject(content);
        ItemSummary itemSummary = itemSummaryService.getOrNewOne(symbol);
        itemSummary.setSymbol(symbol);
        // 基本每股收益
        String EPSJB = data.getJSONArray("zxzb").getJSONObject(0).getString("EPSJB");
        itemSummary.setEps(toFixed(EPSJB, 4));
        log.info("基本每股收益-- " + EPSJB);
        // 每股净资产
        String BPS = data.getJSONArray("zxzb").getJSONObject(0).getString("BPS");
        itemSummary.setBps(toFixed(BPS, 4));
        log.info("每股净资产-- " + BPS);

        // 市净率
        String PB_NEW_NOTICE = data.getJSONArray("zxzbOther").getJSONObject(0).getString("PB_NEW_NOTICE");
        itemSummary.setPbTtm(PB_NEW_NOTICE);
        log.info("市净率-- " + PB_NEW_NOTICE);

       // 总营同比
        String TOTALOPERATEREVETZ = data.getJSONArray("zyzb").getJSONObject(0).getString("TOTALOPERATEREVETZ");
        itemSummary.setYoyTotalOperatingRevenue(toFixed(TOTALOPERATEREVETZ, 2)+"%");
        log.info("总营同比-- " + TOTALOPERATEREVETZ);

        // 营业总收入
        String TOTAL_OPERATEINCOME = data.getJSONArray("zxzb").getJSONObject(0).getString("TOTAL_OPERATEINCOME");
        itemSummary.setYoyTotalOperatingRevenue(formatMoney(TOTAL_OPERATEINCOME));
        log.info("营业总收入-- " + TOTAL_OPERATEINCOME);

        //净利润同比
        String PARENTNETPROFITTZ = data.getJSONArray("zxzb").getJSONObject(0).getString("PARENTNETPROFITTZ");
        itemSummary.setYoyNetProfit(toFixed(PARENTNETPROFITTZ, 2)+"%");
        log.info("净利润同比-- " + PARENTNETPROFITTZ);

        // 净利率 没有

        // 资产负载率
        String ZCFZL = data.getJSONArray("zxzb").getJSONObject(0).getString("ZCFZL");
        itemSummary.setDebtRatio(toFixed(ZCFZL, 2)+"%");
        log.info("资产负载率-- " + ZCFZL);

        //总市值
        String TOTAL_MARKET_CAP = data.getJSONObject("zxzbhq").getString("f116");
        itemSummary.setSkMarketCap(formatMoney(TOTAL_MARKET_CAP));
        log.info("总市值-- " + TOTAL_MARKET_CAP);

        // 流通股本
        String FREE_SHARE =  data.getJSONArray("zxzb").getJSONObject(0).getString("FREE_SHARE");
        itemSummary.setCirculatingAShares(FREE_SHARE);
        log.info("流通A市值-- " + FREE_SHARE);

        // 毛利率
        String  XSMLL =  data.getJSONArray("zxzb").getJSONObject(0).getString("XSMLL");
        itemSummary.setGrossProfitMargin(toFixed(XSMLL, 2)+"%");
        log.info("毛利率-- " + XSMLL);

        // 流通股本
        String TOTAL_SHARE =  data.getJSONArray("zxzb").getJSONObject(0).getString("TOTAL_SHARE");
        itemSummary.setCommonAcs(TOTAL_SHARE);
        log.info("流通股本-- " + TOTAL_SHARE);

        //净资产收益率
        String  ROEJQ =  data.getJSONArray("zxzb").getJSONObject(0).getString("ROEJQ");
        itemSummary.setGrossProfitMargin(toFixed(ROEJQ, 2)+"%");
        log.info("净资产收益率-- " + ROEJQ);

        // 净利润
        String PARENTNETPROFIT =  data.getJSONArray("zyzb").getJSONObject(0).getString("PARENTNETPROFIT");
        itemSummary.setNetProfit(formatMoney(PARENTNETPROFIT));
        log.info("净利润-- " + PARENTNETPROFIT);

        // 市盈率
        String peTtm =  data.getJSONArray("zxzbOther").getJSONObject(0).getString("PE_STATIC");
        itemSummary.setPeTtm(toFixed(peTtm, 2));
        log.info("市盈率-- " + peTtm);


        String companyContent = HttpHelper.sendGetHttp(A_stock_url_company + symbol, "");
        JSONObject companyContentData = JSONObject.parseObject(companyContent);
        // 公司名称
        String orgName = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_NAME");
        itemSummary.setOrgName(orgName);
        log.info("公司名称-- " + orgName);


        // 英文名称
        String ORG_NAME_EN = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_NAME_EN");
        itemSummary.setEnName(ORG_NAME_EN);
        log.info("英文名称-- " + ORG_NAME_EN);

        // A股代码
        String STR_CODEA = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("STR_CODEA");
        itemSummary.setStrCodeA(STR_CODEA);
        log.info("A股代码-- " + STR_CODEA);
        //  A股简称
        String STR_NAMEA = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("STR_NAMEA");
        itemSummary.setStrNameA(STR_NAMEA);
        log.info("A股简称-- " + STR_NAMEA);

        // 所属区域
        String PROVINCE = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("PROVINCE");
        itemSummary.setRegion(PROVINCE);
        log.info("所属区域-- " + PROVINCE);


        // 所属行业
        String EM2016 = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("EM2016");
        itemSummary.setBelongIndustry(EM2016);
        log.info("所属行业-- " + EM2016);

        // 所属概念

        //董事长
        String CHAIRMAN = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("CHAIRMAN");
        itemSummary.setChairman(CHAIRMAN);
        log.info("董事长-- " + CHAIRMAN);

        //总经理
        String PRESIDENT = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("PRESIDENT");
        itemSummary.setManagingDirector(PRESIDENT);
        log.info("总经理-- " + PRESIDENT);

        //法人代表
        String LEGAL_PERSON = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("LEGAL_PERSON");
        itemSummary.setLegalRepresentative(LEGAL_PERSON);
        log.info("法人代表-- " + LEGAL_PERSON);

        //董秘
        String SECRETARY = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("SECRETARY");
        itemSummary.setLegalRepresentative(SECRETARY);
        log.info("董秘-- " + SECRETARY);

        //成立日期
        String FOUND_DATE = companyContentData.getJSONArray("fxxg").getJSONObject(0).getString("FOUND_DATE");
        itemSummary.setFoundDate(FOUND_DATE);
        log.info("成立日期-- " + FOUND_DATE);

        //注册资本
        String REG_CAPITAL = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("REG_CAPITAL");
        itemSummary.setRegisteredCapital(formatMoney(REG_CAPITAL));
        log.info("注册资本-- " + REG_CAPITAL);

        //员工人数
        String EMP_NUM = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("EMP_NUM");
        itemSummary.setEmpNum((EMP_NUM));
        log.info("员工人数-- " + EMP_NUM);

        //管理层人数
        String TATOLNUMBER = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("TATOLNUMBER");
        itemSummary.setManagementTeamSize((TATOLNUMBER));
        log.info("管理层人数-- " + TATOLNUMBER);



        //审计机构
        String ACCOUNTFIRM_NAME = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ACCOUNTFIRM_NAME");
        itemSummary.setAuditOrganization((ACCOUNTFIRM_NAME));
        log.info("审计机构-- " + ACCOUNTFIRM_NAME);

        //法律顾问
        String LAW_FIRM = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("LAW_FIRM");
        itemSummary.setLegalAdvisor((LAW_FIRM));
        log.info("法律顾问-- " + LAW_FIRM);

        //联系电话
        String ORG_TEL = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_TEL");
        itemSummary.setOrgTel((ORG_TEL));
        log.info("联系电话-- " + ORG_TEL);

        //公司邮箱
        String ORG_EMAIL = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_EMAIL");
        itemSummary.setOrgEmail(ORG_EMAIL);
        log.info("公司邮箱-- " + ORG_EMAIL);

        //公司网址
        String ORG_WEB = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_WEB");
        itemSummary.setOrgWeb((ORG_WEB));
        log.info("公司网址-- " + ORG_WEB);

        //办公地址
        String ADDRESS = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ADDRESS");
        itemSummary.setAddress((ADDRESS));
        log.info("办公地址-- " + ADDRESS);


        //注册地址
        String REG_ADDRESS = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("REG_ADDRESS");
        itemSummary.setRegAddress(REG_ADDRESS);
        log.info("注册地址-- " + REG_ADDRESS);

        //公司简介
        String ORG_PROFILE = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("ORG_PROFILE");
        itemSummary.setOrgProfile(ORG_PROFILE);
        log.info("公司简介-- " + ORG_PROFILE);
       //BUSINESS_SCOPE

        //公司简介
        String BUSINESS_SCOPE = companyContentData.getJSONArray("jbzl").getJSONObject(0).getString("BUSINESS_SCOPE");
        itemSummary.setBusinessScope(BUSINESS_SCOPE);
        log.info("主营业务-- " + BUSINESS_SCOPE);
        itemSummaryService.saveOrUpdate(itemSummary);

    }

    /**
     *
     * @param symbol
     */
    public void crawlHkStock(String symbol) {
        String content = HttpHelper.sendGetHttp(hk_stock_url_latest.replace("{}", symbol), "");
        JSONObject data = JSONObject.parseObject(content);
        ItemSummary itemSummary = itemSummaryService.getOrNewOne(symbol);
        itemSummary.setSymbol(symbol);
        // 基本每股收益
        JSONObject jsonObject = data.getJSONObject("result").getJSONArray("data").getJSONObject(0);
        String EPSJB = jsonObject.getString("BASIC_EPS");
        itemSummary.setEps(toFixed(EPSJB, 4));
        log.info("基本每股收益-- " + EPSJB);

        // 每股股息
        String DPS_HKD = jsonObject.getString("DPS_HKD");
        itemSummary.setDpsHkd(toFixed(DPS_HKD, 4));
        log.info("每股股息-- " + DPS_HKD);

        //港股股本
        String HK_COMMON_SHARES = jsonObject.getString("HK_COMMON_SHARES");
        itemSummary.setHongKongStockCapital(toFixed(HK_COMMON_SHARES, 4));
        log.info("港股股本-- " + HK_COMMON_SHARES);


        //总股本
        String ISSUED_COMMON_SHARES = jsonObject.getString("ISSUED_COMMON_SHARES");
        itemSummary.setCommonAcs(toFixed(ISSUED_COMMON_SHARES, 4));
        log.info("港股股本-- " + ISSUED_COMMON_SHARES);

        // 每股净资产
        String BPS = jsonObject.getString("BPS");
        itemSummary.setBps(toFixed(BPS, 4));
        log.info("每股净资产-- " + BPS);

        // 市净率
        String PB_NEW_NOTICE =jsonObject.getString("PB_TTM");
        itemSummary.setPbTtm(PB_NEW_NOTICE);
        log.info("市净率-- " + PB_NEW_NOTICE);

        // 总营同比
        String TOTALOPERATEREVETZ = jsonObject.getString("OPERATE_INCOME_QOQ");
        itemSummary.setYoyTotalOperatingRevenue(toFixed(TOTALOPERATEREVETZ, 2)+"%");
        log.info("总营同比-- " + TOTALOPERATEREVETZ);

        // 营业总收入
        String TOTAL_OPERATEINCOME = jsonObject.getString("OPERATE_INCOME");
        itemSummary.setYoyTotalOperatingRevenue(formatMoney(TOTAL_OPERATEINCOME));
        log.info("营业总收入-- " + TOTAL_OPERATEINCOME);

        //净利润同比
        String PARENTNETPROFITTZ = jsonObject.getString("HOLDER_PROFIT_QOQ");
        itemSummary.setYoyNetProfit(toFixed(PARENTNETPROFITTZ, 2)+"%");
        log.info("净利润同比-- " + PARENTNETPROFITTZ);

        // 净利率
        String NET_PROFIT_RATIO = jsonObject.getString("NET_PROFIT_RATIO");
        itemSummary.setYoyNetProfit(toFixed(NET_PROFIT_RATIO, 2)+"%");
        log.info("净利率-- " + NET_PROFIT_RATIO);

        // 股息率
        String DIVIDEND_RATE = jsonObject.getString("DIVIDEND_RATE");
        itemSummary.setDividendRate(toFixed(DIVIDEND_RATE, 2)+"%");
        log.info("股息率-- " + DIVIDEND_RATE);


        // 资产负载率
//        String ZCFZL = data.getJSONArray("zxzb").getJSONObject(0).getString("ZCFZL");
//        itemSummary.setDebtRatio(toFixed(ZCFZL, 2)+"%");
//        log.info("资产负载率-- " + ZCFZL);

        //总市值
        String TOTAL_MARKET_CAP = jsonObject.getString("TOTAL_MARKET_CAP");
        itemSummary.setSkMarketCap(formatMoney(TOTAL_MARKET_CAP));
        log.info("总市值-- " + TOTAL_MARKET_CAP);

        //港股市值
        String HKSK_MARKET_CAP = jsonObject.getString("HKSK_MARKET_CAP");
        itemSummary.setHkskMarketCap(formatMoney(HKSK_MARKET_CAP));
        log.info("总市值-- " + HKSK_MARKET_CAP);



        // 净利润
        String PARENTNETPROFIT =  jsonObject.getString("HOLDER_PROFIT");
        itemSummary.setNetProfit(formatMoney(PARENTNETPROFIT));
        log.info("净利润-- " + PARENTNETPROFIT);

        // 市盈率
        String peTtm =  jsonObject.getString("PE_TTM");
        itemSummary.setPeTtm(toFixed(peTtm, 2));
        log.info("市盈率-- " + peTtm);



        String companyContent = HttpHelper.sendGetHttp(getHk_stock_url_company.replace("{}", symbol), "");
        // 公司名称
        JSONObject companyData = JSONObject.parseObject(companyContent).getJSONObject("result").getJSONArray("data").getJSONObject(0);
        String orgName = companyData.getString("ORG_NAME");
        itemSummary.setOrgName(orgName);
        log.info("公司名称-- " + orgName);

        // 所属行业
        String EM2016 = companyData.getString("BELONG_INDUSTRY");
        itemSummary.setBelongIndustry(EM2016);
        log.info("所属行业-- " + EM2016);

        // 英文名称
        String ORG_NAME_EN = companyData.getString("ORG_EN_ABBR");
        itemSummary.setEnName(ORG_NAME_EN);
        log.info("英文名称-- " + ORG_NAME_EN);

        //董事长
        String CHAIRMAN = companyData.getString("CHAIRMAN");
        itemSummary.setChairman(CHAIRMAN);
        log.info("董事长-- " + CHAIRMAN);

        //董秘
        String SECRETARY = companyData.getString("SECRETARY");
        itemSummary.setLegalRepresentative(SECRETARY);
        log.info("董秘-- " + SECRETARY);

        //成立日期
        String FOUND_DATE = companyData.getString("FOUND_DATE");
        itemSummary.setFoundDate(FOUND_DATE);
        log.info("成立日期-- " + FOUND_DATE);

        //员工人数
        String EMP_NUM = companyData.getString("EMP_NUM");
        itemSummary.setEmpNum((EMP_NUM));
        log.info("员工人数-- " + EMP_NUM);


        //联系电话
        String ORG_TEL = companyData.getString("ORG_TEL");
        itemSummary.setOrgTel((ORG_TEL));
        log.info("联系电话-- " + ORG_TEL);

        //公司邮箱
        String ORG_EMAIL = companyData.getString("ORG_EMAIL");
        itemSummary.setOrgEmail(ORG_EMAIL);
        log.info("公司邮箱-- " + ORG_EMAIL);

        //公司网址
        String ORG_WEB = companyData.getString("ORG_WEB");
        itemSummary.setOrgWeb((ORG_WEB));
        log.info("公司网址-- " + ORG_WEB);

        //办公地址
        String ADDRESS = companyData.getString("ADDRESS");
        itemSummary.setAddress((ADDRESS));
        log.info("办公地址-- " + ADDRESS);

        // 传真
        String ORG_FAX = companyData.getString("ORG_FAX");
        itemSummary.setOrgFax((ORG_FAX));
        log.info("传真-- " + ORG_FAX);

        //注册地址
        String REG_ADDRESS =companyData.getString("REG_ADDRESS");
        itemSummary.setRegAddress(REG_ADDRESS);
        log.info("注册地址-- " + REG_ADDRESS);

        //公司简介
        String ORG_PROFILE = companyData.getString("ORG_PROFILE");
        itemSummary.setOrgProfile(ORG_PROFILE);
        log.info("公司简介-- " + ORG_PROFILE);

        //核数师
        String ACCOUNT_FIRM = companyData.getString("ACCOUNT_FIRM");
        itemSummary.setAccountFirm(ACCOUNT_FIRM);
        log.info("核数师-- " + ACCOUNT_FIRM);

        // 证券资料部分
        /**
         *
         *
         *
         *
         *
         * 每股面值
         */

        String seContent = HttpHelper.sendGetHttp(getHk_stock_url_se.replace("{}", symbol), "");
        JSONObject seData = JSONObject.parseObject(seContent).getJSONObject("result").getJSONArray("data").getJSONObject(0);

        //证券代码
        String SECUCODE = seData.getString("SECUCODE");
        itemSummary.setSecucode(SECUCODE);
        log.info("证券代码-- " + SECUCODE);

        //证券类型
        String SECURITY_TYPE = seData.getString("SECURITY_TYPE");
        itemSummary.setSecurityType(SECURITY_TYPE);
        log.info("证券类型-- " + SECURITY_TYPE);

        //ISIN
        String ISIN = seData.getString("ISIN_CODE");
        itemSummary.setIsinCode(ISIN);
        log.info("ISIN-- " + ISIN);

        //上市时间
        String listingDate = seData.getString("LISTING_DATE");
        itemSummary.setListingDate(listingDate);
        log.info("上市时间-- " + listingDate);


        //板块
        String board = seData.getString("BOARD");
        itemSummary.setBoard(board);
        log.info("板块-- " + board);

        //年结日
        String yearSettleDay = companyData.getString("YEAR_SETTLE_DAY");
        itemSummary.setYearSettleDay(yearSettleDay);
        log.info("板块-- " + yearSettleDay);

        //每手股数
        String TRADE_UNIT = seData.getString("TRADE_UNIT");
        itemSummary.setTradeUnit(TRADE_UNIT);
        log.info("每手股数-- " + TRADE_UNIT);
        itemSummaryService.saveOrUpdate(itemSummary);

    }
    /**
     *
     * @param symbol
     */
    public void crawlUsStock(String symbol) {
        /**
         *

         */
        String content = HttpHelper.sendGetHttp(uk_stock_url_latest.replace("{}", symbol), "");
        if(content.contains("返回数据为空")){
            content = HttpHelper.sendGetHttp(uk_stock_url_latest1.replace("{}", symbol), "");
        }
        if (content.contains("返回数据为空")) {
            log.error("{} 简况数据不存在", symbol);
            return;
        }
        JSONObject data = JSONObject.parseObject(content);
        ItemSummary itemSummary = itemSummaryService.getOrNewOne(symbol);
        itemSummary.setSymbol(symbol);

        JSONObject jsonObject = data.getJSONObject("result").getJSONArray("data").getJSONObject(0);
        // 市盈率
        String peTtm =  jsonObject.getString("PE_TTM");
        itemSummary.setPeTtm(toFixed(peTtm, 2));
        log.info("市盈率-- " + peTtm);

        // 基本每股收益
        String EPSJB = jsonObject.getString("RATIO_EPS_TTM");
        itemSummary.setEps(toFixed(EPSJB, 4));
        log.info("基本每股收益-- " + EPSJB);

        // 每股股息
        String DPS_HKD = jsonObject.getString("DPS_USD");
        itemSummary.setDpsHkd(toFixed(DPS_HKD, 4));
        log.info("每股股息-- " + DPS_HKD);

        //总股本
        String ISSUED_COMMON_SHARES = jsonObject.getString("ISSUED_COMMON_SHARES");
        itemSummary.setCommonAcs(toFixed(ISSUED_COMMON_SHARES, 4));
        log.info("总股本-- " + ISSUED_COMMON_SHARES);

        // 每股净资产
        String BPS = jsonObject.getString("BVPS");
        itemSummary.setBps(toFixed(BPS, 4));
        log.info("每股净资产-- " + BPS);

        // 市净率
        String PB_NEW_NOTICE =jsonObject.getString("PB");
        itemSummary.setPbTtm(PB_NEW_NOTICE);
        log.info("市净率-- " + PB_NEW_NOTICE);

        // 毛利率
        String SALE_GPR =jsonObject.getString("SALE_GPR");
        itemSummary.setGrossProfitMargin( toFixed(SALE_GPR, 2) +"%");
        log.info("毛利率-- " + SALE_GPR);

        // 归母净利润
        String HOLDER_PROFIT = jsonObject.getString("HOLDER_PROFIT");
        itemSummary.setProfitParentCompany(formatMoney(HOLDER_PROFIT));
        log.info("归母净利润-- " + HOLDER_PROFIT);

        // 归母净利润同比
        String HOLDER_PROFIT_YOY = jsonObject.getString("HOLDER_PROFIT_YOY");
        itemSummary.setYoyTotalOperatingRevenue(toFixed(HOLDER_PROFIT_YOY, 2)+"%");
        log.info("归母净利润同比-- " + HOLDER_PROFIT_YOY);

        // 总营同比
        String TOTALOPERATEREVETZ = jsonObject.getString("TURNOVER_YOY");
        itemSummary.setYoyTotalOperatingRevenue(toFixed(TOTALOPERATEREVETZ, 2)+"%");
        log.info("总营同比-- " + TOTALOPERATEREVETZ);

        // 营业总收入
        String TOTAL_OPERATEINCOME = jsonObject.getString("TURNOVER");
        itemSummary.setYoyTotalOperatingRevenue(formatMoney(TOTAL_OPERATEINCOME));
        log.info("营业总收入-- " + TOTAL_OPERATEINCOME);

        //净利润同比
        String PARENTNETPROFITTZ = jsonObject.getString("HOLDER_PROFIT_QOQ");
        itemSummary.setYoyNetProfit(toFixed(PARENTNETPROFITTZ, 2)+"%");
        log.info("净利润同比-- " + PARENTNETPROFITTZ);

        // 净利率
        String NET_PROFIT_RATIO = jsonObject.getString("SALE_NPR");
        itemSummary.setYoyNetProfit(toFixed(NET_PROFIT_RATIO, 2)+"%");
        log.info("净利率-- " + NET_PROFIT_RATIO);

        // 股息率
        String weeklyInterestRate = jsonObject.getString("DIVIDEND_RATE");
        itemSummary.setDividendRate(toFixed(weeklyInterestRate, 2)+"%");
        log.info("股息率-- " + weeklyInterestRate);



        //总市值
        String TOTAL_MARKET_CAP = jsonObject.getString("TOTAL_MARKET_CAP");
        itemSummary.setSkMarketCap(formatMoney(TOTAL_MARKET_CAP));
        log.info("总市值-- " + TOTAL_MARKET_CAP);


        // 净利润
        String PARENTNETPROFIT =  jsonObject.getString("HOLDER_PROFIT");
        itemSummary.setNetProfit(formatMoney(PARENTNETPROFIT));
        log.info("净利润-- " + PARENTNETPROFIT);





        String companyContent = HttpHelper.sendGetHttp(uk_stock_url_company.replace("{}", symbol), "");
        // 公司名称
        JSONObject companyData = JSONObject.parseObject(companyContent).getJSONObject("result").getJSONArray("data").getJSONObject(0);
        String orgName = companyData.getString("ORG_NAME");
        itemSummary.setOrgName(orgName);
        log.info("公司名称-- " + orgName);

        // 所属行业
        String EM2016 = companyData.getString("BELONG_INDUSTRY");
        itemSummary.setBelongIndustry(EM2016);
        log.info("所属行业-- " + EM2016);

        // 英文名称
        String ORG_NAME_EN = companyData.getString("ORG_EN_ABBR");
        itemSummary.setEnName(ORG_NAME_EN);
        log.info("英文名称-- " + ORG_NAME_EN);

        //董事长
        String CHAIRMAN = companyData.getString("CHAIRMAN");
        itemSummary.setChairman(CHAIRMAN);
        log.info("董事长-- " + CHAIRMAN);

        //董秘
        String SECRETARY = companyData.getString("SECRETARY");
        itemSummary.setLegalRepresentative(SECRETARY);
        log.info("董秘-- " + SECRETARY);

        //成立日期
        String FOUND_DATE = companyData.getString("FOUND_DATE");
        itemSummary.setFoundDate(FOUND_DATE);
        log.info("成立日期-- " + FOUND_DATE);

        //员工人数
        String EMP_NUM = companyData.getString("EMP_NUM");
        itemSummary.setEmpNum((EMP_NUM));
        log.info("员工人数-- " + EMP_NUM);


        //联系电话
        String ORG_TEL = companyData.getString("ORG_TEL");
        itemSummary.setOrgTel((ORG_TEL));
        log.info("联系电话-- " + ORG_TEL);

        //公司邮箱
        String ORG_EMAIL = companyData.getString("ORG_EMAIL");
        itemSummary.setOrgEmail(ORG_EMAIL);
        log.info("公司邮箱-- " + ORG_EMAIL);

        //公司网址
        String ORG_WEB = companyData.getString("ORG_WEB");
        itemSummary.setOrgWeb((ORG_WEB));
        log.info("公司网址-- " + ORG_WEB);

        //办公地址
        String ADDRESS = companyData.getString("ADDRESS");
        itemSummary.setAddress((ADDRESS));
        log.info("办公地址-- " + ADDRESS);

        // 传真
        String ORG_FAX = companyData.getString("ORG_FAX");
        itemSummary.setOrgFax((ORG_FAX));
        log.info("传真-- " + ORG_FAX);

        //注册地址
        String REG_ADDRESS =companyData.getString("REG_PLACE");
        itemSummary.setRegAddress(REG_ADDRESS);
        log.info("注册地址-- " + REG_ADDRESS);

        //公司简介
        String ORG_PROFILE = companyData.getString("ORG_PROFILE");
        itemSummary.setOrgProfile(ORG_PROFILE);
        log.info("公司简介-- " + ORG_PROFILE);

        //核数师
        String ACCOUNT_FIRM = companyData.getString("ACCOUNT_FIRM");
        itemSummary.setAccountFirm(ACCOUNT_FIRM);
        log.info("核数师-- " + ACCOUNT_FIRM);

        // 证券资料部分
        /**
         * 年结日
         */

        String seContent = HttpHelper.sendGetHttp(uk_stock_url_se.replace("{}", symbol), "");
        if(seContent.contains("返回数据为空")){
            seContent =  HttpHelper.sendGetHttp(uk_stock_url_se1.replace("{}", symbol), "");
        }
        JSONObject seData = JSONObject.parseObject(seContent).getJSONObject("result").getJSONArray("data").getJSONObject(0);

        //证券代码
        String SECUCODE = seData.getString("SECUCODE");
        itemSummary.setSecucode(SECUCODE);
        log.info("证券代码-- " + SECUCODE);

        //证券类型
        String SECURITY_TYPE = seData.getString("SECURITY_TYPE");
        itemSummary.setSecurityType(SECURITY_TYPE);
        log.info("证券类型-- " + SECURITY_TYPE);

        //ISIN
        String ISIN = seData.getString("ISIN_CODE");
        itemSummary.setIsinCode(ISIN);
        log.info("ISIN-- " + ISIN);

        //上市时间
        String listingDate = seData.getString("LISTING_DATE");
        itemSummary.setListingDate(listingDate);
        log.info("上市时间-- " + listingDate);


        //上市场所
        String TRADE_MARKET = seData.getString("TRADE_MARKET");
        itemSummary.setStockExchange(TRADE_MARKET);
        log.info("上市场所-- " + TRADE_MARKET);

        //年结日
        String yearSettleDay = companyData.getString("YEAR_SETTLE_DAY");
        itemSummary.setYearSettleDay(yearSettleDay);
        log.info("年结日-- " + yearSettleDay);

        //每股面值
        String TRADE_UNIT = seData.getString("PAR_VALUE");
        itemSummary.setParValuePerShare(TRADE_UNIT);
        log.info("每股面值-- " + TRADE_UNIT);
      itemSummaryService.saveOrUpdate(itemSummary);

    }

    public static String toFixed(String content, int scale) {
        if (StringUtils.isEmptyString(content) ) {
            return "-";
        }
        if(content.contains("null")){
            return "-";
        }
        return new BigDecimal(content).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    public static String formatMoney(String moneyStr) {
        if(StringUtils.isEmptyString(moneyStr)){
            return "--";
        }
        BigDecimal money = new BigDecimal(moneyStr);

        BigDecimal wan = new BigDecimal("10000");
        BigDecimal yi = new BigDecimal("100000000");
        BigDecimal wanYi = new BigDecimal("1000000000000");

        String result = "";

        if (money.compareTo(wanYi) >= 0) {
            BigDecimal value = money.divide(wanYi, 3, BigDecimal.ROUND_HALF_UP);
            result =value.toPlainString()+ "万亿";
        } else if (money.compareTo(yi) >= 0) {
            BigDecimal value = money.divide(yi, 3, BigDecimal.ROUND_HALF_UP);
            result =value.toPlainString()+ "亿";
        } else if (money.compareTo(wan) >= 0) {
            BigDecimal value = money.divide(wan, 3, BigDecimal.ROUND_HALF_UP);
            result = value.toPlainString() + "万";
        } else {
            result = money.setScale(3, BigDecimal.ROUND_HALF_UP).toPlainString();
        }

        return result;
    }


    public static void main(String[] args) {
      //  new SummaryCrawl().crawlAStock("SZ300750");
      //  new SummaryCrawl().crawlHkStock("00700");
     new SummaryCrawl().crawlUsStock("YMAB");
    }
}
