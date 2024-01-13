package com.yami.trading.common.util;

import java.util.HashSet;
import java.util.Set;

public class C2cLock {

	private static final Set<String> filter = new HashSet<String>();

	public static boolean add(String partyId) {
		if (!filter.add(partyId)) {
			return false;
		} else {
			return true;
		}
	}

	public static void remove(String partyId) {
		filter.remove(partyId);
	}

}
