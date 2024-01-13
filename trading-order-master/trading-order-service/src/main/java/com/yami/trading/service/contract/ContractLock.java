package com.yami.trading.service.contract;

import cn.hutool.core.collection.ConcurrentHashSet;

import java.util.HashSet;
import java.util.Set;

public class ContractLock {
	private static final Set<String> filter = new ConcurrentHashSet<>();

	public static synchronized boolean add(String order_no) {
		if (!filter.add(order_no)) {
			return false;
		} else {
			return true;
		}
	}

	public static void remove(String order_no) {
		filter.remove(order_no);

	}

}
