/**
 * Copyright © 2018
 *
 * @Package: OrderUtils.java
 * @author: Administrator
 * @date: 2018年4月22日 下午4:14:03
 */
package com.kenhome.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:订单工具类
 * @author: cmk
 * @date: 2018年4月22日 下午4:14:03
 */

public class OrderUtils {

    /**
     * 锁对象，可以为任意对象
     */
    private static Object lockObj = "lockerOrder";
    /**
     * 订单号生成计数器
     */
    private static long orderNumCount = 0L;
    /**
     * 每毫秒生成订单号数量最大值
     */
    private static int maxPerMSECSize = 100;

    /**
     * 生成非重复订单号，理论上限1毫秒1000个，可扩展
     *
     */
    public static Long makeOrderNo() {
        try {
            // 最终生成的订单号
            String finOrderNum = "";
            synchronized (lockObj) {
                // 取系统当前时间作为订单号变量前半部分，精确到毫秒
                long nowLong = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                // 计数器到最大值归零，可扩展更大，目前1毫秒处理峰值1000个，1秒100万
                if (orderNumCount >= maxPerMSECSize) {
                    orderNumCount = 0L;
                }
                // 组装订单号
                String countStr = maxPerMSECSize + orderNumCount + "";
                finOrderNum = nowLong + countStr.substring(1);
                orderNumCount++;
            }
            return Long.parseLong(finOrderNum);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
