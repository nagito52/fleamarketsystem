package com.example.fleamarketsystem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.service.AdminUserService;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ItemService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final ItemService itemService;
	private final AppOrderService appOrderService;
	private final AdminUserService adminUserService; // 追加

	// コンストラクタに adminUserService を追加
	public AdminController(ItemService itemService, AppOrderService appOrderService,
			AdminUserService adminUserService) {
		this.itemService = itemService;
		this.appOrderService = appOrderService;
		this.adminUserService = adminUserService;
	}

	@GetMapping("/items")
	public String manageItems(Model model) {
		model.addAttribute("items", itemService.getAllItems());
		return "admin_items";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
	    // 修正前: itemService.getAvailableItems() など（出品中のものだけを取得していた）
	    // 修正後: getAllItems() を使用して、売却済みの商品も表示されるようにする
	    model.addAttribute("recentItems", itemService.getAllItems().stream()
	            .limit(10) // 最新10件などに制限すると見やすくなります
	            .collect(Collectors.toList()));

	    model.addAttribute("activeOrders", appOrderService.getActiveOrders());
	    model.addAttribute("pendingCancels", appOrderService.getPendingCancelOrders());
	    model.addAttribute("finalizedCancels", appOrderService.getFinalizedCancelledOrders());
	    
	    return "admin_dashboard";
	}

	@GetMapping("/statistics")
	public String showStatistics(
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			Model model) {

		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		if (endDate == null)
			endDate = LocalDate.now();

		// Service側の戻り値（BigDecimal/Map）を正しく受け取ってModelへ
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("totalSales", appOrderService.getTotalSales(startDate, endDate));
		model.addAttribute("orderCountByStatus", appOrderService.getOrderCountByStatus(startDate, endDate));

		return "admin_statistics";
	}

	@PostMapping("/items/{id}/delete")
	public String deleteItemByAdmin(@PathVariable("id") Long itemId) {
	    // 新しく作った方のメソッドを呼ぶ
	    Item item = itemService.getItemByIdOrThrow(itemId); 

	    if ("取引中".equals(item.getStatus())) {
	        return "redirect:/admin/items?error=trading";
	    }

	    itemService.deleteItem(itemId);
	    return "redirect:/admin/items?success=deleted";
	}

	@PostMapping("/orders/{id}/final-cancel")
	public String finalizeCancelByAdmin(@PathVariable("id") Long orderId) throws com.stripe.exception.StripeException {
		appOrderService.finalCancel(orderId);
		return "redirect:/admin/dashboard?success=refunded";
	}

	@PostMapping("/orders/{id}/force-cancel")
	public String forceCancelByAdmin(@PathVariable("id") Long orderId,
			@RequestParam(value = "reason", required = false) String reason,
			RedirectAttributes redirectAttributes) {
		try {
			appOrderService.forceCancelByAdmin(orderId, reason);
			redirectAttributes.addFlashAttribute("successMessage", "強制キャンセルを実行しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/statistics/csv")
	public void exportStatisticsCsv(
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			HttpServletResponse response) throws IOException {

		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		if (endDate == null)
			endDate = LocalDate.now();

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment;filename=\"flea_market_statistics.csv\"");

		try (PrintWriter writer = response.getWriter()) {
			// 1. Excel用BOMの書き込み
			writer.write('\ufeff');

			// 2. サービス側の詳細なCSV出力ロジックを呼び出す
			// 統計期間のヘッダー等もサービス側で統一的に管理
			writer.append("統計期間：").append(String.valueOf(startDate))
					.append("から").append(String.valueOf(endDate)).append("\n\n");

			// 注文明細やステータス集計など、AdminUserServiceに実装したロジックを利用
			adminUserService.writeStatisticsCsv(writer, startDate, endDate);

			writer.flush();
		}
	}
}