package com.yami.trading.huobi.data.internal;


import com.yami.trading.bean.data.domain.Realtime;

import java.util.List;

public class RealtimeTimeObject extends TimeObject{

	private static final long serialVersionUID = -597193064229646966L;
	
	List<Realtime> list;

	public List<Realtime> getList() {
		return list;
	}

	public void setList(List<Realtime> list) {
		this.list = list;
	}
	
	
}
