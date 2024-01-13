package com.yami.trading.admin.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ThinksWebSocketClient extends WebSocketClient {
    @Autowired
    RedisTemplate redisTemplate;
    interface Command {
        void execute(Map<String, Object> data);
    }

    private Map<String, Command> commands;

    public ThinksWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
        commands = new HashMap<>();
        commands.put("ping", (d) -> this.pingCommand(d));
        commands.put("connect", (d) -> this.connectCommand(d));
    }

    private void pingCommand(Map<String, Object> data) {

        log.info("心跳检测={}", JSONUtil.toJsonStr(data));
    }

    private void connectCommand(Map<String, Object> data) {

        log.info("连接成功={}", JSONUtil.toJsonStr(data));
    }

    Thread pingPong;

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("[websocket] 连接成功");
        this.send("key:zHPU8uWYMY7eWx78kbC0:14");
        this.send("heartbeat");
        final ThinksWebSocketClient that = this;
        int heartbeatInterval = 7000; // 每3秒发送一次心跳消息
        pingPong = new Thread(() -> {
            while (pingPong != null) {
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.send("key:zHPU8uWYMY7eWx78kbC0:14");
                this.send("heartbeat");
                log.info("[websocket] /heartbeat");
            }
        });
        log.info("[websocket] /heartbeats");
        pingPong.setDaemon(true);
        pingPong.start();
    }

    @Override
    public void onMessage(String message) {
        if(message.equals("pong")){
            return;
        }
        JSONObject msgObject = JSONUtil.parseObj(message);
        if (!JSONUtil.isTypeJSON(message)) {
            log.error("[websocket] 收到非JSON消息={}", message);
        }
        else if (msgObject!=null) {
            String pid = msgObject.getStr("pid");
            redisTemplate.opsForValue().set("ydTask" + pid, msgObject);
            log.info("收到实时数据={}", JSONUtil.toJsonStr(message));
        }else{
            log.error("[websocket] 收到未识别到的消息类型={}", message);
        }
    }

    private void stopHeartbeat() {
        if (pingPong == null) {
            return;
        }
        log.info("[websocketpingPong] /heartbeats");

        pingPong.stop();
        pingPong = null;
    }

    private void reConnect() {
        this.stopHeartbeat();
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("正在重连...");
        if(pingPong!=null){
            pingPong=null;
        }
        int heartbeatInterval = 6000; // 每5秒发送一次心跳消息
        pingPong = new Thread(() -> {
            while (pingPong != null) {
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.send("key:zHPU8uWYMY7eWx78kbC0:14");
                this.send("heartbeat");
                log.info("[websocket] /heartbeat");
            }
        });
        log.info("[websocket] /heartbeats");
        reConnect();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("[websocket] 退出连接");
        this.stopHeartbeat();
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.reConnect();
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        log.info("[websocket] 连接错误={}", ex.getMessage());
        this.stopHeartbeat();
        this.reConnect();
    }
}
