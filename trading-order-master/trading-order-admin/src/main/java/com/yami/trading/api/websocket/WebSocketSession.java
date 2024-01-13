package com.yami.trading.api.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.websocket.Session;
import javax.websocket.server.PathParam;
import java.io.IOException;

@Data
public class WebSocketSession {
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
	private Session session;
	private long timeStr;
	private String setKey;
	private String type;
	private String param;

	public WebSocketSession(Session session, long timeStr, String type, String param) {
		this.session = session;
		this.timeStr = timeStr;
		this.type = type;
		this.param = param;
		this.setKey = session.getId() + "_" + type + "_" + param;
	}

	/**
	 * 单发消息
	 */
	public void sendMessage(String message) throws IOException {
		// 阻塞式（同步）
		// this.session.getBasicRemote().sendText(message);
		// 非阻塞式（异步）
		this.session.getAsyncRemote().sendText(message);
	}

}
