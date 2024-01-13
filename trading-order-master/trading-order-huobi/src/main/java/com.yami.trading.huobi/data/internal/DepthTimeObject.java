package com.yami.trading.huobi.data.internal;


import com.yami.trading.bean.data.domain.Depth;

public class DepthTimeObject extends TimeObject  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6508793344391115053L;


	private Depth depth;


	public Depth getDepth() {
		return depth;
	}

	public void setDepth(Depth depth) {
		this.depth = depth;
	}

}
