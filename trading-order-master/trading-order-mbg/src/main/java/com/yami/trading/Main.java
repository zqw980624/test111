package com.yami.trading;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Splitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String content =
                "{\n" +
                        "    \"code\": 0,\n" +
                        "    \"message\": \"成功\",\n" +
                        "    \"data\": {\n" +
                        "        \"list\": [\n" +
                        "            {\n" +
                        "                \"name\": \"Shell PLC\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"SHEL\",\n" +
                        "                \"stockId\": \"81703163098184\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179588876,\n" +
                        "                \"priceNominal\": \"61.825\",\n" +
                        "                \"priceLastClose\": \"60.870\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179588876\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179588967\",\n" +
                        "                \"priceOpen\": \"62.070\",\n" +
                        "                \"priceHighest\": \"62.300\",\n" +
                        "                \"priceLowest\": \"61.750\",\n" +
                        "                \"volume\": \"3.18M\",\n" +
                        "                \"turnover\": \"197.23M\",\n" +
                        "                \"ratioVolume\": \"1.58\",\n" +
                        "                \"ratioTurnover\": \"0.10%\",\n" +
                        "                \"amplitudePrice\": \"0.90%\",\n" +
                        "                \"priceAverage\": \"62.083\",\n" +
                        "                \"changeSpeedPrice\": \"-8\",\n" +
                        "                \"ratioBidAsk\": \"-28.57%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"61.820\",\n" +
                        "                \"priceAsk\": \"61.830\",\n" +
                        "                \"volumeBid\": \"500\",\n" +
                        "                \"volumeAsk\": \"900\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"61.825\",\n" +
                        "                \"change\": \"+0.955\",\n" +
                        "                \"changeRatio\": \"+1.57%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"61.825\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Cummins\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"CMI\",\n" +
                        "                \"stockId\": \"201856\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179544079,\n" +
                        "                \"priceNominal\": \"256.920\",\n" +
                        "                \"priceLastClose\": \"256.590\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179544079\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179544171\",\n" +
                        "                \"priceOpen\": \"259.650\",\n" +
                        "                \"priceHighest\": \"260.330\",\n" +
                        "                \"priceLowest\": \"256.640\",\n" +
                        "                \"volume\": \"307.48K\",\n" +
                        "                \"turnover\": \"79.60M\",\n" +
                        "                \"ratioVolume\": \"0.67\",\n" +
                        "                \"ratioTurnover\": \"0.22%\",\n" +
                        "                \"amplitudePrice\": \"1.44%\",\n" +
                        "                \"priceAverage\": \"258.881\",\n" +
                        "                \"changeSpeedPrice\": \"11\",\n" +
                        "                \"ratioBidAsk\": \"-33.33%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"256.890\",\n" +
                        "                \"priceAsk\": \"257.100\",\n" +
                        "                \"volumeBid\": \"200\",\n" +
                        "                \"volumeAsk\": \"400\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"256.920\",\n" +
                        "                \"change\": \"+0.330\",\n" +
                        "                \"changeRatio\": \"+0.13%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"256.995\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Valero Energy\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"VLO\",\n" +
                        "                \"stockId\": \"201864\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179580481,\n" +
                        "                \"priceNominal\": \"115.390\",\n" +
                        "                \"priceLastClose\": \"115.950\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179580481\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179580724\",\n" +
                        "                \"priceOpen\": \"116.990\",\n" +
                        "                \"priceHighest\": \"117.900\",\n" +
                        "                \"priceLowest\": \"115.160\",\n" +
                        "                \"volume\": \"1.33M\",\n" +
                        "                \"turnover\": \"154.21M\",\n" +
                        "                \"ratioVolume\": \"0.73\",\n" +
                        "                \"ratioTurnover\": \"0.37%\",\n" +
                        "                \"amplitudePrice\": \"2.36%\",\n" +
                        "                \"priceAverage\": \"116.316\",\n" +
                        "                \"changeSpeedPrice\": \"-43\",\n" +
                        "                \"ratioBidAsk\": \"-50.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"115.330\",\n" +
                        "                \"priceAsk\": \"115.410\",\n" +
                        "                \"volumeBid\": \"100\",\n" +
                        "                \"volumeAsk\": \"300\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"115.390\",\n" +
                        "                \"change\": \"-0.560\",\n" +
                        "                \"changeRatio\": \"-0.48%\",\n" +
                        "                \"priceDirect\": \"down\",\n" +
                        "                \"priceMiddle\": \"115.370\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Entergy\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"ETR\",\n" +
                        "                \"stockId\": \"203463\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179533397,\n" +
                        "                \"priceNominal\": \"98.750\",\n" +
                        "                \"priceLastClose\": \"97.600\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179533397\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179533853\",\n" +
                        "                \"priceOpen\": \"98.030\",\n" +
                        "                \"priceHighest\": \"98.890\",\n" +
                        "                \"priceLowest\": \"97.320\",\n" +
                        "                \"volume\": \"379.60K\",\n" +
                        "                \"turnover\": \"37.20M\",\n" +
                        "                \"ratioVolume\": \"0.41\",\n" +
                        "                \"ratioTurnover\": \"0.18%\",\n" +
                        "                \"amplitudePrice\": \"1.61%\",\n" +
                        "                \"priceAverage\": \"97.998\",\n" +
                        "                \"changeSpeedPrice\": \"10\",\n" +
                        "                \"ratioBidAsk\": \"0.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"98.730\",\n" +
                        "                \"priceAsk\": \"98.790\",\n" +
                        "                \"volumeBid\": \"100\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"98.750\",\n" +
                        "                \"change\": \"+1.150\",\n" +
                        "                \"changeRatio\": \"+1.18%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"98.760\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Ferrari\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"RACE\",\n" +
                        "                \"stockId\": \"71846213138911\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179524918,\n" +
                        "                \"priceNominal\": \"321.510\",\n" +
                        "                \"priceLastClose\": \"318.390\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179524918\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179525009\",\n" +
                        "                \"priceOpen\": \"320.190\",\n" +
                        "                \"priceHighest\": \"322.640\",\n" +
                        "                \"priceLowest\": \"318.990\",\n" +
                        "                \"volume\": \"230.28K\",\n" +
                        "                \"turnover\": \"73.96M\",\n" +
                        "                \"ratioVolume\": \"1.30\",\n" +
                        "                \"ratioTurnover\": \"0.20%\",\n" +
                        "                \"amplitudePrice\": \"1.15%\",\n" +
                        "                \"priceAverage\": \"321.186\",\n" +
                        "                \"changeSpeedPrice\": \"-51\",\n" +
                        "                \"ratioBidAsk\": \"60.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"321.490\",\n" +
                        "                \"priceAsk\": \"321.650\",\n" +
                        "                \"volumeBid\": \"400\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"321.510\",\n" +
                        "                \"change\": \"+3.120\",\n" +
                        "                \"changeRatio\": \"+0.98%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"321.570\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Honda Motor\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"HMC\",\n" +
                        "                \"stockId\": \"202313\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179570363,\n" +
                        "                \"priceNominal\": \"30.360\",\n" +
                        "                \"priceLastClose\": \"29.940\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179570363\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179570455\",\n" +
                        "                \"priceOpen\": \"30.280\",\n" +
                        "                \"priceHighest\": \"30.500\",\n" +
                        "                \"priceLowest\": \"30.230\",\n" +
                        "                \"volume\": \"496.32K\",\n" +
                        "                \"turnover\": \"15.07M\",\n" +
                        "                \"ratioVolume\": \"0.94\",\n" +
                        "                \"ratioTurnover\": \"0.03%\",\n" +
                        "                \"amplitudePrice\": \"0.90%\",\n" +
                        "                \"priceAverage\": \"30.366\",\n" +
                        "                \"changeSpeedPrice\": \"-16\",\n" +
                        "                \"ratioBidAsk\": \"92.59%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"30.350\",\n" +
                        "                \"priceAsk\": \"30.360\",\n" +
                        "                \"volumeBid\": \"2.60K\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"30.360\",\n" +
                        "                \"change\": \"+0.420\",\n" +
                        "                \"changeRatio\": \"+1.40%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"30.355\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Toyota Motor\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"TM\",\n" +
                        "                \"stockId\": \"202032\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179557648,\n" +
                        "                \"priceNominal\": \"159.780\",\n" +
                        "                \"priceLastClose\": \"158.420\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179557648\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179557739\",\n" +
                        "                \"priceOpen\": \"159.470\",\n" +
                        "                \"priceHighest\": \"160.310\",\n" +
                        "                \"priceLowest\": \"159.120\",\n" +
                        "                \"volume\": \"120.65K\",\n" +
                        "                \"turnover\": \"19.27M\",\n" +
                        "                \"ratioVolume\": \"0.83\",\n" +
                        "                \"ratioTurnover\": \"0.01%\",\n" +
                        "                \"amplitudePrice\": \"0.75%\",\n" +
                        "                \"priceAverage\": \"159.719\",\n" +
                        "                \"changeSpeedPrice\": \"-43\",\n" +
                        "                \"ratioBidAsk\": \"-50.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"159.740\",\n" +
                        "                \"priceAsk\": \"159.840\",\n" +
                        "                \"volumeBid\": \"100\",\n" +
                        "                \"volumeAsk\": \"300\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"159.780\",\n" +
                        "                \"change\": \"+1.360\",\n" +
                        "                \"changeRatio\": \"+0.86%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"159.790\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Oceaneering International\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"OII\",\n" +
                        "                \"stockId\": \"201518\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179476996,\n" +
                        "                \"priceNominal\": \"22.155\",\n" +
                        "                \"priceLastClose\": \"22.440\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179476996\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179477087\",\n" +
                        "                \"priceOpen\": \"22.750\",\n" +
                        "                \"priceHighest\": \"22.900\",\n" +
                        "                \"priceLowest\": \"22.155\",\n" +
                        "                \"volume\": \"488.30K\",\n" +
                        "                \"turnover\": \"10.99M\",\n" +
                        "                \"ratioVolume\": \"0.72\",\n" +
                        "                \"ratioTurnover\": \"0.49%\",\n" +
                        "                \"amplitudePrice\": \"3.32%\",\n" +
                        "                \"priceAverage\": \"22.497\",\n" +
                        "                \"changeSpeedPrice\": \"-67\",\n" +
                        "                \"ratioBidAsk\": \"33.33%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"22.150\",\n" +
                        "                \"priceAsk\": \"22.170\",\n" +
                        "                \"volumeBid\": \"400\",\n" +
                        "                \"volumeAsk\": \"200\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"22.155\",\n" +
                        "                \"change\": \"-0.285\",\n" +
                        "                \"changeRatio\": \"-1.27%\",\n" +
                        "                \"priceDirect\": \"down\",\n" +
                        "                \"priceMiddle\": \"22.160\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Atmos Energy\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"ATO\",\n" +
                        "                \"stockId\": \"203143\",\n" +
                        "                \"marketCode\": 10,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179589793,\n" +
                        "                \"priceNominal\": \"120.230\",\n" +
                        "                \"priceLastClose\": \"118.480\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179589793\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179589884\",\n" +
                        "                \"priceOpen\": \"118.990\",\n" +
                        "                \"priceHighest\": \"120.390\",\n" +
                        "                \"priceLowest\": \"118.925\",\n" +
                        "                \"volume\": \"152.66K\",\n" +
                        "                \"turnover\": \"18.24M\",\n" +
                        "                \"ratioVolume\": \"0.47\",\n" +
                        "                \"ratioTurnover\": \"0.11%\",\n" +
                        "                \"amplitudePrice\": \"1.24%\",\n" +
                        "                \"priceAverage\": \"119.494\",\n" +
                        "                \"changeSpeedPrice\": \"16\",\n" +
                        "                \"ratioBidAsk\": \"71.43%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"120.250\",\n" +
                        "                \"priceAsk\": \"120.290\",\n" +
                        "                \"volumeBid\": \"600\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"120.230\",\n" +
                        "                \"change\": \"+1.750\",\n" +
                        "                \"changeRatio\": \"+1.48%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"120.270\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Microsoft\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"MSFT\",\n" +
                        "                \"stockId\": \"201345\",\n" +
                        "                \"marketCode\": 11,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179589808,\n" +
                        "                \"priceNominal\": \"337.295\",\n" +
                        "                \"priceLastClose\": \"332.470\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179589808\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179589899\",\n" +
                        "                \"priceOpen\": \"336.600\",\n" +
                        "                \"priceHighest\": \"341.652\",\n" +
                        "                \"priceLowest\": \"335.670\",\n" +
                        "                \"volume\": \"15.79M\",\n" +
                        "                \"turnover\": \"5.35B\",\n" +
                        "                \"ratioVolume\": \"1.32\",\n" +
                        "                \"ratioTurnover\": \"0.21%\",\n" +
                        "                \"amplitudePrice\": \"1.80%\",\n" +
                        "                \"priceAverage\": \"338.410\",\n" +
                        "                \"changeSpeedPrice\": \"-68\",\n" +
                        "                \"ratioBidAsk\": \"0.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"337.260\",\n" +
                        "                \"priceAsk\": \"337.290\",\n" +
                        "                \"volumeBid\": \"100\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"337.295\",\n" +
                        "                \"change\": \"+4.825\",\n" +
                        "                \"changeRatio\": \"+1.45%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"337.275\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Apple\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"AAPL\",\n" +
                        "                \"stockId\": \"205189\",\n" +
                        "                \"marketCode\": 11,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179589528,\n" +
                        "                \"priceNominal\": \"189.380\",\n" +
                        "                \"priceLastClose\": \"188.080\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179589528\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179589621\",\n" +
                        "                \"priceOpen\": \"189.680\",\n" +
                        "                \"priceHighest\": \"191.700\",\n" +
                        "                \"priceLowest\": \"188.470\",\n" +
                        "                \"volume\": \"31.81M\",\n" +
                        "                \"turnover\": \"6.05B\",\n" +
                        "                \"ratioVolume\": \"1.38\",\n" +
                        "                \"ratioTurnover\": \"0.20%\",\n" +
                        "                \"amplitudePrice\": \"1.72%\",\n" +
                        "                \"priceAverage\": \"190.077\",\n" +
                        "                \"changeSpeedPrice\": \"-34\",\n" +
                        "                \"ratioBidAsk\": \"66.67%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"189.380\",\n" +
                        "                \"priceAsk\": \"189.390\",\n" +
                        "                \"volumeBid\": \"500\",\n" +
                        "                \"volumeAsk\": \"100\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"189.380\",\n" +
                        "                \"change\": \"+1.300\",\n" +
                        "                \"changeRatio\": \"+0.69%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"189.385\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"Futu Holdings Ltd\",\n" +
                        "                \"marketType\": 2,\n" +
                        "                \"marketLabel\": \"US\",\n" +
                        "                \"stockCode\": \"FUTU\",\n" +
                        "                \"stockId\": \"78103980495165\",\n" +
                        "                \"marketCode\": 11,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689179586720,\n" +
                        "                \"priceNominal\": \"45.214\",\n" +
                        "                \"priceLastClose\": \"43.420\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689179586720\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"1689179586811\",\n" +
                        "                \"priceOpen\": \"44.360\",\n" +
                        "                \"priceHighest\": \"45.520\",\n" +
                        "                \"priceLowest\": \"43.650\",\n" +
                        "                \"volume\": \"1.13M\",\n" +
                        "                \"turnover\": \"50.52M\",\n" +
                        "                \"ratioVolume\": \"1.25\",\n" +
                        "                \"ratioTurnover\": \"1.99%\",\n" +
                        "                \"amplitudePrice\": \"4.31%\",\n" +
                        "                \"priceAverage\": \"44.589\",\n" +
                        "                \"changeSpeedPrice\": \"-44\",\n" +
                        "                \"ratioBidAsk\": \"-33.33%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"45.210\",\n" +
                        "                \"priceAsk\": \"45.250\",\n" +
                        "                \"volumeBid\": \"100\",\n" +
                        "                \"volumeAsk\": \"200\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"45.214\",\n" +
                        "                \"change\": \"+1.794\",\n" +
                        "                \"changeRatio\": \"+4.13%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"45.230\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"TENCENT\",\n" +
                        "                \"marketType\": 1,\n" +
                        "                \"marketLabel\": \"HK\",\n" +
                        "                \"stockCode\": \"00700\",\n" +
                        "                \"stockId\": \"54047868453564\",\n" +
                        "                \"marketCode\": 1,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 100,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689149301000,\n" +
                        "                \"priceNominal\": \"340.000\",\n" +
                        "                \"priceLastClose\": \"333.800\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590183\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689149301000\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"0\",\n" +
                        "                \"priceOpen\": \"338.600\",\n" +
                        "                \"priceHighest\": \"341.000\",\n" +
                        "                \"priceLowest\": \"335.800\",\n" +
                        "                \"volume\": \"16.85M\",\n" +
                        "                \"turnover\": \"5.72B\",\n" +
                        "                \"ratioVolume\": \"1.04\",\n" +
                        "                \"ratioTurnover\": \"0.18%\",\n" +
                        "                \"amplitudePrice\": \"1.56%\",\n" +
                        "                \"priceAverage\": \"339.285\",\n" +
                        "                \"changeSpeedPrice\": \"117\",\n" +
                        "                \"ratioBidAsk\": \"0.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"0.000\",\n" +
                        "                \"priceAsk\": \"0.000\",\n" +
                        "                \"volumeBid\": \"0\",\n" +
                        "                \"volumeAsk\": \"0\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"340.000\",\n" +
                        "                \"change\": \"+6.200\",\n" +
                        "                \"changeRatio\": \"+1.86%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"0.000\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"BHP Group Ltd\",\n" +
                        "                \"marketType\": 22,\n" +
                        "                \"marketLabel\": \"AU\",\n" +
                        "                \"stockCode\": \"BHP\",\n" +
                        "                \"stockId\": \"81046046875878\",\n" +
                        "                \"marketCode\": 210,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689141600000,\n" +
                        "                \"priceNominal\": \"44.070\",\n" +
                        "                \"priceLastClose\": \"43.330\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689141600000\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"0\",\n" +
                        "                \"priceOpen\": \"44.050\",\n" +
                        "                \"priceHighest\": \"44.100\",\n" +
                        "                \"priceLowest\": \"43.780\",\n" +
                        "                \"volume\": \"6.19M\",\n" +
                        "                \"turnover\": \"274.05M\",\n" +
                        "                \"ratioVolume\": \"0.82\",\n" +
                        "                \"ratioTurnover\": \"0.13%\",\n" +
                        "                \"amplitudePrice\": \"0.74%\",\n" +
                        "                \"priceAverage\": \"44.288\",\n" +
                        "                \"changeSpeedPrice\": \"-68\",\n" +
                        "                \"ratioBidAsk\": \"0.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"0.000\",\n" +
                        "                \"priceAsk\": \"0.000\",\n" +
                        "                \"volumeBid\": \"0\",\n" +
                        "                \"volumeAsk\": \"0\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"44.070\",\n" +
                        "                \"change\": \"+0.740\",\n" +
                        "                \"changeRatio\": \"+1.71%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"0.000\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"name\": \"AGL Energy Ltd\",\n" +
                        "                \"marketType\": 22,\n" +
                        "                \"marketLabel\": \"AU\",\n" +
                        "                \"stockCode\": \"AGL\",\n" +
                        "                \"stockId\": \"81046046875627\",\n" +
                        "                \"marketCode\": 210,\n" +
                        "                \"instrumentType\": 3,\n" +
                        "                \"lotSize\": 1,\n" +
                        "                \"priceAccuracy\": 3,\n" +
                        "                \"isPlate\": false,\n" +
                        "                \"isFutures\": false,\n" +
                        "                \"isOption\": false,\n" +
                        "                \"subInstrumentType\": 0,\n" +
                        "                \"strikePrice\": \"NaN\",\n" +
                        "                \"underlyingStock\": {},\n" +
                        "                \"time\": 1689141600000,\n" +
                        "                \"priceNominal\": \"11.150\",\n" +
                        "                \"priceLastClose\": \"11.140\",\n" +
                        "                \"serverSendToClientTimeMs\": \"1689179590184\",\n" +
                        "                \"exchangeDataTimeMs\": \"1689141600000\",\n" +
                        "                \"serverRecvFromExchangeTimeMs\": \"0\",\n" +
                        "                \"priceOpen\": \"11.180\",\n" +
                        "                \"priceHighest\": \"11.180\",\n" +
                        "                \"priceLowest\": \"11.080\",\n" +
                        "                \"volume\": \"2.65M\",\n" +
                        "                \"turnover\": \"29.48M\",\n" +
                        "                \"ratioVolume\": \"1.01\",\n" +
                        "                \"ratioTurnover\": \"0.45%\",\n" +
                        "                \"amplitudePrice\": \"0.90%\",\n" +
                        "                \"priceAverage\": \"11.140\",\n" +
                        "                \"changeSpeedPrice\": \"-268\",\n" +
                        "                \"ratioBidAsk\": \"0.00%\",\n" +
                        "                \"volumePrecision\": 0,\n" +
                        "                \"priceBid\": \"0.000\",\n" +
                        "                \"priceAsk\": \"0.000\",\n" +
                        "                \"volumeBid\": \"0\",\n" +
                        "                \"volumeAsk\": \"0\",\n" +
                        "                \"orderVolumePrecision\": 0,\n" +
                        "                \"price\": \"11.150\",\n" +
                        "                \"change\": \"+0.010\",\n" +
                        "                \"changeRatio\": \"+0.09%\",\n" +
                        "                \"priceDirect\": \"up\",\n" +
                        "                \"priceMiddle\": \"0.000\",\n" +
                        "                \"before_open_stock_info\": null,\n" +
                        "                \"sparkInfo\": {}\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"page\": 1\n" +
                        "    }\n" +
                        "}";

        JSONArray jsonArray = JSONObject.parseObject(content).getJSONObject("data").getJSONArray("list");
        for(Object o : jsonArray){
            JSONObject data = (JSONObject) o;
            String code = data.getString("stockCode");
            String name = data.getString("name");

            String sql = "update t_item set en_name ='{0}' where symbol = '{1}';";
            System.out.println(sql.replace("{0}",name).replace("{1}", code));

        }
//        List<String> strings = Splitter.on("\n").splitToList(content);
//        String collect = strings.stream().collect(Collectors.joining(","));
//        System.out.println(collect);
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("a", new BigDecimal(2.1).setScale(3, RoundingMode.HALF_UP));
//        System.out.println(JSONObject.toJSONString(data));

    }
}
