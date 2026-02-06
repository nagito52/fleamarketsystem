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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.AppOrderRepository;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ItemService;
import com.example.fleamarketsystem.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Controller
@RequestMapping("/orders")
public class AppOrderController {

	private final AppOrderService appOrderService;
	private final UserService userService;
	private final ItemService itemService;
	
	@Value("${stripe.public.key}")
	private String stripePublicKey;
	
	public AppOrderController(AppOrderService appOrderService, UserService userService, ItemService itemService, AppOrderRepository appOrderRepository) {
		this.appOrderService = appOrderService;
		this.userService = userService;
		this.itemService = itemService;
	}
	
	@PostMapping("/initiate-purchase")
	public String initiatePurchase(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("itemId") Long itemId,
			RedirectAttributes redirectAttributes) {
		User buyer = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Buyer not found"));
		try {
			PaymentIntent paymentIntent = appOrderService.initiatePurchase(itemId, buyer);
			redirectAttributes.addFlashAttribute("clientSecret", paymentIntent.getClientSecret());
			redirectAttributes.addFlashAttribute("itemId", itemId);
			
			return "redirect:/orders/confirm-payment";
		} catch (IllegalStateException | IllegalArgumentException | StripeException e) {
			
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/items/" + itemId;
		}
	}
	
	@GetMapping("/confirm-payment")
	public String confirmPayment(
			@ModelAttribute("clientSecret") String clientSecret,
			@ModelAttribute("itemId") Long itemId,
			Model model) {
		if (clientSecret == null || itemId == null) {
			return "redirect:/items";
		}
		model.addAttribute("clientSecret", clientSecret);
		model.addAttribute("itemId", itemId);
		model.addAttribute("stripePublicKey", stripePublicKey);
		return "payment_confirmation";
	}
	
	@GetMapping("/complete-purchase")
	public String completePurchase(
			@RequestParam("paymentIntentId") String paymentIntentId,
			RedirectAttributes redirectAttributes) {
	try {
		appOrderService.completePurchase(paymentIntentId);
		redirectAttributes.addFlashAttribute("successMessage", "商品を購入しました！");
		return appOrderService.getLatestCompletedOrderId().map(orderId -> "redirect:/reviews/new/" + orderId)
				.orElseGet(() -> {
					redirectAttributes.addFlashAttribute("errorMessage", "購入は完了しましたが、評価ページへのリダイレクトに失敗しました。");
					return "redirect:/my-page/orders";
				});
		} catch (StripeException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "決済処理中にエラーが発生しました:" + e.getMessage());
			return "redirect:/items";
		}
	}
	
	@PostMapping("/stripe-webhook")
	public void handleStripeWebhook(
			@RequestBody Stripe payload,
			@RequestHeader("Stripe-Signature") String sigHeader) {
		System.out.println("Received Stripe Webhook: + payload");
	}
	
	@PostMapping("/{id}/ship")
	public String shipOrder(
			@PathVariable("id") Long orderId,
			RedirectAttributes redirectAttributes) {
		try {
			appOrderService.markOrderAsShipped(orderId);
			redirectAttributes.addFlashAttribute("successMessage", "商品を発送済みにしました。");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/my-page/sales";
	}
}
