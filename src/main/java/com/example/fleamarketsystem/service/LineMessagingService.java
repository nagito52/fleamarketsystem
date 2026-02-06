package com.example.fleamarketsystem.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LineMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(LineMessagingService.class);
    private final RestTemplate restTemplate;

    // application.propertiesから宛先とトークンを読み込む
    @Value("${line.messaging.token}")
    private String channelToken;

    @Value("${line.messaging.user-id}")
    private String adminUserId;

    private static final String LINE_API_URL = "https://api.line.me/v2/bot/message/push";

    public LineMessagingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * メッセージを送信する
     * 引数から accessToken を消し、クラス内のフィールドを使用する形に整理しました
     */
    public void sendMessage(String messageText) {
        try {
            // ヘッダーの設定 (JSON形式)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(channelToken);

            // 送信ボディの作成 (Messaging API 指定のJSON構造)
            Map<String, Object> body = new HashMap<>();
            body.put("to", adminUserId);
            body.put("messages", List.of(
                Map.of(
                    "type", "text",
                    "text", messageText
                )
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // API実行
            restTemplate.postForEntity(LINE_API_URL, entity, String.class);
            logger.info("LINE Messaging API で通知を送信しました。");

        } catch (Exception e) {
            // 失敗してもアプリを止めないよう WARN ログを出力
            logger.warn("LINEメッセージの送信に失敗しました: {}", e.getMessage());
        }
    }
}