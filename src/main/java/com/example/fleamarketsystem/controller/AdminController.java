package com.example.fleamarketsystem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

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
	public AdminController(ItemService itemService, AppOrderService appOrderService, AdminUserService adminUserService) {
		this.itemService = itemService;
		this.appOrderService = appOrderService;
		this.adminUserService = adminUserService;
	}

	@GetMapping("/items")
	public String manageItems(Model model) {
		model.addAttribute("items", itemService.getAllItems());
		return "admin_items";
	}

	@PostMapping("/items/{id}/delete")
	public String deleteItemByAdmin(@PathVariable("id") Long itemId) {
		// 必要に応じて：削除前に LineMessagingService で「規約違反のため削除」と通知することも可能
		itemService.deleteItem(itemId);
		return "redirect:/admin/items?success=deleted";
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

		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("totalSales", appOrderService.getTotalSales(startDate, endDate));
		model.addAttribute("orderCountByStatus", appOrderService.getOrderCountByStatus(startDate, endDate));
		// ファイル名のタイポ修正：admin_startistics -> admin_statistics
		return "admin_statistics";
	}
	
	// AdminController.java の 120行目付近（または追加したdashboardメソッド内）

	// AdminController.java (120行目付近に追加)

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
	    // 最近の出品を取得 (ItemService.java に getAllItems がある前提)
	    model.addAttribute("recentItems", itemService.getAllItems()); 
	    
	    // 注文一覧を取得 (後述する AppOrderService の修正が必要です)
	    model.addAttribute("recentOrders", appOrderService.getAllOrders()); 

	    return "admin_dashboard";
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