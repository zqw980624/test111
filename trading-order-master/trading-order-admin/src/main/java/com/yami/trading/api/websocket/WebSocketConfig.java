package com.yami.trading.api.websocket;

import com.yami.trading.admin.config.ThinksWebSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.net.URI;

/**
 * 给spring容器注入这个ServerEndpointExporter对象
 *
 * 检测所有带有@serverEndpoint注解的bean并注册他们。
 */
@EnableWebSocket
@Configuration
public class WebSocketConfig {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	/*@Bean
	public WebSocketClient webSocketClient() {
		try {
			URI uri = new URI("ws://api-in-ws.js-stock.top");//  test-ws.js-stock.top
			Draft draft = new Draft_6455();
			ThinksWebSocketClient webSocketClient = new ThinksWebSocketClient(uri, draft);
			webSocketClient.connect();
			return webSocketClient;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/
}
