package com.example.fleamarketsystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.UserService;
import com.stripe.exception.StripeException;

@Controller
@RequestMapping("/orders")
public class AppOrderController {

	private final AppOrderService appOrderService;
	private final UserService userService;

	@Value("${stripe.public.key}")
	private String stripePublicKey;

	public AppOrderController(AppOrderService appOrderService, UserService userService) {
		this.appOrderService = appOrderService;
		this.userService = userService;
	}

	@PostMapping("/initiate-purchase")
	public String initiatePurchase(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("itemId") Long itemId,
			RedirectAttributes redirectAttributes) {
		User buyer = userService.getUserByEmail(userDetails.getUsername()).orElseThrow();
		try {
			var paymentIntent = appOrderService.initiatePurchase(itemId, buyer);
			redirectAttributes.addFlashAttribute("clientSecret", paymentIntent.getClientSecret());
			redirectAttributes.addFlashAttribute("itemId", itemId);
			return "redirect:/orders/confirm-payment";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/items/" + itemId;
		}
	}

	@GetMapping("/confirm-payment")
	public String confirmPayment(@ModelAttribute("clientSecret") String clientSecret,
			@ModelAttribute("itemId") Long itemId, Model model) {
		if (clientSecret == null || itemId == null)
			return "redirect:/items";
		model.addAttribute("clientSecret", clientSecret);
		model.addAttribute("itemId", itemId);
		model.addAttribute("stripePublicKey", stripePublicKey);
		return "payment_confirmation";
	}

	@GetMapping("/complete-purchase")
	public String completePurchase(@RequestParam("paymentIntentId") String paymentIntentId,
			RedirectAttributes redirectAttributes) {
		try {
			appOrderService.completePurchase(paymentIntentId);
			redirectAttributes.addFlashAttribute("successMessage", "決済完了！取引を開始しました。");
			return "redirect:/my_page/orders"; // 購入済ではなく取引履歴へ
		} catch (StripeException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
			return "redirect:/items";
		}
	}

	@PostMapping("/{id}/confirm-arrival")
	public String confirmArrival(@PathVariable("id") Long orderId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User user = userService.getUserByEmail(userDetails.getUsername()).get();
			appOrderService.confirmArrival(orderId, user);
			redirectAttributes.addFlashAttribute("successMessage", "到着確認完了！レビューをお願いします。");
			return "redirect:/reviews/new/" + orderId; // ここでレビューへ
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/my_page/orders";
		}
	}

	@PostMapping("/{id}/cancel-request")
	public String requestCancel(@PathVariable("id") Long orderId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User user = userService.getUserByEmail(userDetails.getUsername()).get();
			appOrderService.requestCancel(orderId, user);
			redirectAttributes.addFlashAttribute("successMessage", "キャンセル要請を送信しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/my_page/orders";
	}

	@PostMapping("/{id}/ship")
	public String shipOrder(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
		try {
			appOrderService.markOrderAsShipped(orderId);
			redirectAttributes.addFlashAttribute("successMessage", "発送済みにしました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/my_page/sales";
	}

	// AppOrderController.java に追加

	// 出品者の同意
	@PostMapping("/{id}/approve-cancel")
	public String approveCancel(@PathVariable("id") Long orderId,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User seller = userService.getUserByEmail(userDetails.getUsername()).get();
			appOrderService.approveCancel(orderId, seller);
			redirectAttributes.addFlashAttribute("successMessage", "キャンセルに同意しました。管理者の返金処理を待ちます。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/my_page/sales"; // 出品一覧へ
	}

	// 管理者の最終確定
	@PostMapping("/{id}/final-cancel")
	public String finalCancel(@PathVariable("id") Long orderId, RedirectAttributes redirectAttributes) {
		try {
			appOrderService.finalCancel(orderId);
			redirectAttributes.addFlashAttribute("successMessage", "返金処理とキャンセルが完了しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "返金失敗: " + e.getMessage());
		}
		return "redirect:/admin/dashboard"; // 管理者用注文一覧へ
	}
}