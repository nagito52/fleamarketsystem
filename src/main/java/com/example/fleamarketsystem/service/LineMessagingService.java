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
import org.springframework.web.client.HttpClientErrorException;
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
        if (channelToken == null || channelToken.isBlank()) {
            logger.warn("LINEメッセージの送信に失敗しました: token が未設定です。");
            return;
        }
        if (adminUserId == null || adminUserId.isBlank()) {
            logger.warn("LINEメッセージの送信に失敗しました: user-id が未設定です。");
            return;
        }

        try {
            sendPush(messageText);
            logger.info("LINE Messaging API で通知を送信しました。");
        } catch (HttpClientErrorException e) {
            logger.warn("LINEメッセージの送信に失敗しました: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            // 失敗してもアプリを止めないよう WARN ログを出力
            logger.warn("LINEメッセージの送信に失敗しました: {}", e.getMessage());
        }
    }

    private void sendPush(String messageText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(channelToken);

        Map<String, Object> body = new HashMap<>();
        body.put("to", adminUserId);
        body.put("messages", List.of(
            Map.of(
                "type", "text",
                "text", messageText
            )
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(LINE_API_URL, entity, String.class);
    }
}