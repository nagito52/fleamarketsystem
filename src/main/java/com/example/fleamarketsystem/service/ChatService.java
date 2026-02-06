package com.example.fleamarketsystem.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.Chat;
import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.ChatRepository;
import com.example.fleamarketsystem.repository.ItemRepository;

@Service
public class ChatService {

	private final ChatRepository chatRepository;
	private final ItemRepository itemRepository;
	private final LineMessagingService lineMessagingService; // 名前を統一

	public ChatService(ChatRepository chatRepository, ItemRepository itemRepository,
			LineMessagingService lineMessagingService) {
		this.chatRepository = chatRepository;
		this.itemRepository = itemRepository;
		this.lineMessagingService = lineMessagingService;
	}

	public List<Chat> getChatMessagesByItem(Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		return chatRepository.findByItemOrderByCreatedAtAsc(item);
	}

	@Transactional
	public Chat sendMessage(Long itemId, User sender, String message) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));

		Chat chat = new Chat();
		chat.setItem(item);
		chat.setSender(sender);
		chat.setMessage(message);
		chat.setCreatedAt(LocalDateTime.now());

		Chat savedChat = chatRepository.save(chat);

		// LINE通知の送信（Messaging API 形式）
		// 誰がログインしていても、propertiesで設定した管理者のLINEに通知が届く設定です
		String notificationMessage = String.format("\n商品「%s」に関する新しいメッセージが届きました！\n送信者: %s\nメッセージ: %s",
				item.getName(),
				sender.getName(),
				message);

		// 新しい sendMessage(String message) を呼び出す
		lineMessagingService.sendMessage(notificationMessage);

		return savedChat;
	}
}