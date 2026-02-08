package com.example.fleamarketsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.AppOrder;
import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.AppOrderRepository;
import com.example.fleamarketsystem.repository.ItemRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public class AppOrderService {

	private final AppOrderRepository appOrderRepository;
	private final ItemRepository itemRepository;
	private final StripeService stripeService;
	private final LineMessagingService lineMessagingService;

	public AppOrderService(AppOrderRepository appOrderRepository, ItemRepository itemRepository,
			StripeService stripeService, LineMessagingService lineMessagingService) {
		this.appOrderRepository = appOrderRepository;
		this.itemRepository = itemRepository;
		this.stripeService = stripeService;
		this.lineMessagingService = lineMessagingService;
	}

	// --- 1. 一般ユーザー向け：注文・出品履歴取得 (今回のエラー修正箇所) ---

	public List<AppOrder> getOrdersByBuyer(User buyer) {
		return appOrderRepository.findByBuyer(buyer);
	}

	public List<AppOrder> getOrdersBySeller(User seller) {
		return appOrderRepository.findByItem_seller(seller);
	}

	public Optional<AppOrder> getOrderById(Long orderId) {
		return appOrderRepository.findById(orderId);
	}

	// --- 2. 管理画面ダッシュボード用：ステータス別取得 ---

	public List<AppOrder> getActiveOrders() {
		return appOrderRepository.findAll().stream()
				.filter(order -> !AppOrder.STATUS_CANCELLED.equals(order.getStatus())
						&& !AppOrder.STATUS_CANCEL_AGREED.equals(order.getStatus()))
				// STATUS_COMPLETED を除外しないことで、完了後も表示され続けます
				.collect(Collectors.toList());
	}

	public List<AppOrder> getPendingCancelOrders() {
		return appOrderRepository.findAll().stream()
				.filter(order -> AppOrder.STATUS_CANCEL_AGREED.equals(order.getStatus()))
				.collect(Collectors.toList());
	}

	public List<AppOrder> getFinalizedCancelledOrders() {
		return appOrderRepository.findAll().stream()
				.filter(order -> AppOrder.STATUS_CANCELLED.equals(order.getStatus()))
				.collect(Collectors.toList());
	}

	// --- 3. 取引アクション（購入・発送・到着・キャンセル） ---

	@Transactional
	public PaymentIntent initiatePurchase(Long itemId, User buyer) throws StripeException {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		if (!"出品中".equals(item.getStatus())) {
			throw new IllegalStateException("この商品は現在購入できません。");
		}

		PaymentIntent paymentIntent = stripeService.createPaymentIntent(item.getPrice(), "jpy",
				"購入: " + item.getName());

		AppOrder appOrder = new AppOrder();
		appOrder.setItem(item);
		appOrder.setBuyer(buyer);
		appOrder.setPrice(item.getPrice());
		appOrder.setStatus("決済待ち");
		appOrder.setPaymentIntentId(paymentIntent.getId());
		appOrder.setCreatedAt(LocalDateTime.now());

		appOrderRepository.saveAndFlush(appOrder);
		return paymentIntent;
	}

	@Transactional
	public AppOrder completePurchase(String paymentIntentId) throws StripeException {
		PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentIntentId);
		if (!"succeeded".equals(paymentIntent.getStatus())) {
			throw new IllegalStateException("決済が完了していません。");
		}

		AppOrder appOrder = appOrderRepository.findByPaymentIntentId(paymentIntentId)
				.orElseThrow(() -> new IllegalStateException("注文が見つかりません。"));

		if (AppOrder.STATUS_TRADING.equals(appOrder.getStatus())
				|| AppOrder.STATUS_COMPLETED.equals(appOrder.getStatus())) {
			return appOrder;
		}

		appOrder.setStatus(AppOrder.STATUS_TRADING);
		Item item = appOrder.getItem();
		item.setStatus("取引中");

		itemRepository.saveAndFlush(item);
		AppOrder savedOrder = appOrderRepository.saveAndFlush(appOrder);

		// 【修正】自分（管理者）へのLINE通知
		try {
			// DBからLINE IDを取得せず、固定の宛先に送る既存のメソッドを呼び出す
			String messageText = String.format(
					"【自分宛通知】商品が購入されました！\n商品名: %s\n購入者: %s\n価格: ¥%s",
					item.getName(),
					savedOrder.getBuyer().getName(),
					savedOrder.getPrice());

			// 引数を messageText だけにすることで、LineMessagingService の既存メソッドと一致させます
			lineMessagingService.sendMessage(messageText);

		} catch (Exception e) {
			// 通知の失敗で取引処理を止めないよう、エラー内容を出力
			System.err.println("LINE通知送信失敗: " + e.getMessage());
		}

		return savedOrder;
	}

	@Transactional
	public void confirmArrival(Long orderId, User buyer) {
		AppOrder order = appOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("注文が見つかりません。"));

		// 購入者本人かチェック
		if (!order.getBuyer().getId().equals(buyer.getId())) {
			throw new IllegalStateException("権限がありません。");
		}

		// 【追加】発送ステータスのチェック
		// ステータスが「発送済」でない場合はエラーを投げる
		if (!AppOrder.STATUS_SHIPPED.equals(order.getStatus())) {
			throw new IllegalStateException("出品者が発送通知を出すまで、到着報告はできません。");
		}

		// 到着報告処理（ステータス更新）
		order.setStatus(AppOrder.STATUS_COMPLETED);
		Item item = order.getItem();
		item.setStatus("売却済");

		itemRepository.saveAndFlush(item);
		appOrderRepository.saveAndFlush(order);

		try {
			String message = String.format("【受取通知】取引が完了しました！\n商品名: %s\n購入者: %s",
					item.getName(), order.getBuyer().getName());
			lineMessagingService.sendMessage(message);
		} catch (Exception e) {
			System.err.println("LINE通知失敗: " + e.getMessage());
		}
	}

	@Transactional
	public void markOrderAsShipped(Long orderId) {
		AppOrder appOrder = appOrderRepository.findById(orderId).orElseThrow();
		appOrder.setStatus(AppOrder.STATUS_SHIPPED);
		appOrderRepository.saveAndFlush(appOrder);

		try {
			String message = String.format("【発送通知】商品が発送されました。\n商品名: %s\n出品者: %s",
					appOrder.getItem().getName(), appOrder.getItem().getSeller().getName());
			lineMessagingService.sendMessage(message);
		} catch (Exception e) {
			System.err.println("LINE通知失敗: " + e.getMessage());
		}
	}

	@Transactional
	public void requestCancel(Long orderId, User buyer) {
		AppOrder order = appOrderRepository.findById(orderId).orElseThrow();
		if (!order.getBuyer().getId().equals(buyer.getId()))
			throw new IllegalStateException("権限がありません。");
		if (AppOrder.STATUS_SHIPPED.equals(order.getStatus())
				|| AppOrder.STATUS_COMPLETED.equals(order.getStatus())) {
			throw new IllegalStateException("発送通知後はキャンセルできません。");
		}
		order.setBuyerCancelRequested(true);
		order.setStatus(AppOrder.STATUS_CANCEL_REQUESTED);
		appOrderRepository.saveAndFlush(order);
	}

	@Transactional
	public void approveCancel(Long orderId, User seller) {
		AppOrder order = appOrderRepository.findById(orderId).orElseThrow();
		if (!order.getItem().getSeller().getId().equals(seller.getId()))
			throw new IllegalStateException("権限がありません。");
		order.setSellerCancelApproved(true);
		order.setStatus(AppOrder.STATUS_CANCEL_AGREED);
		appOrderRepository.saveAndFlush(order);
	}

	@Transactional
	public void finalCancel(Long orderId) throws StripeException {
		AppOrder order = appOrderRepository.findById(orderId).orElseThrow();
		stripeService.refund(order.getPaymentIntentId());
		order.setStatus(AppOrder.STATUS_CANCELLED);
		Item item = order.getItem();
		item.setStatus("出品中");
		itemRepository.saveAndFlush(item);
		appOrderRepository.saveAndFlush(order);

		try {
			String message = String.format("【キャンセル確定】返金処理が完了しました。\n商品名: %s\n価格: ¥%s",
					item.getName(), order.getPrice());
			lineMessagingService.sendMessage(message);
		} catch (Exception e) {
			System.err.println("LINE通知失敗: " + e.getMessage());
		}
	}

	@Transactional
	public void forceCancelByAdmin(Long orderId, String reason) throws StripeException {
		AppOrder order = appOrderRepository.findById(orderId).orElseThrow();
		if (!AppOrder.STATUS_TRADING.equals(order.getStatus())) {
			throw new IllegalStateException("取引中の商品のみ強制キャンセルできます。");
		}
		if (order.getPaymentIntentId() != null && !order.getPaymentIntentId().isBlank()) {
			stripeService.refund(order.getPaymentIntentId());
		}

		order.setStatus(AppOrder.STATUS_CANCELLED);
		Item item = order.getItem();
		item.setStatus("出品中");

		itemRepository.saveAndFlush(item);
		appOrderRepository.saveAndFlush(order);

		try {
			String reasonText = (reason == null || reason.isBlank()) ? "理由: (未入力)" : "理由: " + reason;
			String message = String.format("【強制キャンセル】運営側で取引をキャンセルしました。\n商品名: %s\n価格: ¥%s\n%s",
					item.getName(), order.getPrice(), reasonText);
			lineMessagingService.sendMessage(message);
		} catch (Exception e) {
			System.err.println("LINE通知失敗: " + e.getMessage());
		}
	}

	// --- 4. 統計画面用 ---

	public Map<String, Long> getOrderCountByStatus(LocalDate startDate, LocalDate endDate) {
		return appOrderRepository.findAll().stream()
				.filter(o -> !o.getCreatedAt().toLocalDate().isBefore(startDate)
						&& !o.getCreatedAt().toLocalDate().isAfter(endDate))
				.collect(Collectors.groupingBy(AppOrder::getStatus, Collectors.counting()));
	}

	public BigDecimal getTotalSales(LocalDate startDate, LocalDate endDate) {
		return appOrderRepository.findAll().stream()
				.filter(o -> AppOrder.STATUS_COMPLETED.equals(o.getStatus()))
				.filter(o -> !o.getCreatedAt().toLocalDate().isBefore(startDate)
						&& !o.getCreatedAt().toLocalDate().isAfter(endDate))
				.map(AppOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}