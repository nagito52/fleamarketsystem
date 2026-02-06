package com.example.fleamarketsystem.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;


@Controller
public class DashboardController {

	private final UserRepository userRepository;
	
	//private final ItemService itemService;
	
	//private final AppOrderService appOrderService;
	
	public DashboardController(UserRepository userRepository) {
		
		this.userRepository = userRepository;
		//this.itemService = itemService;
		//this.appOrderService = appOrderService;
	}
	
	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
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
