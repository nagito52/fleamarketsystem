package com.example.fleamarketsystem.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {
	
	//private final ItemService itemService;
	
	//private final AppOrderService appOrderService;
	
	public DashboardController() {
		//this.itemService = itemService;
		//this.appOrderService = appOrderService;
	}
	
	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		return "redirect:/items";
		
		/*if (currentUser.getRole().equals("ADMIN")) {
			model.addAttribute("recentItems", itemService.getAllItems());
			model.addAttribute("recentOrders", appOrderService.getAllOrders());
			return "admin_dashboard";
		} else {
			return "redirect:/items";
		}*/
	}
}
