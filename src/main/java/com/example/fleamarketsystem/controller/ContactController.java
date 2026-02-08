package com.example.fleamarketsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.ContactService;
import com.example.fleamarketsystem.service.LineMessagingService;
import com.example.fleamarketsystem.service.UserService;

@Controller
public class ContactController {

	private final UserService userService;
	private final LineMessagingService lineMessagingService;
	private final ContactService contactService;

	public ContactController(UserService userService, LineMessagingService lineMessagingService,
			ContactService contactService) {
		this.userService = userService;
		this.lineMessagingService = lineMessagingService;
		this.contactService = contactService;
	}

	@GetMapping("/contact")
	public String showContactForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails == null) {
			return "redirect:/login";
		}
		User user = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("user", user);
		return "contact";
	}

	@PostMapping("/contact")
	public String submitContact(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("subject") String subject,
			@RequestParam("message") String message,
			RedirectAttributes redirectAttributes) {
		if (userDetails == null) {
			return "redirect:/login";
		}
		if (subject == null || subject.isBlank() || message == null || message.isBlank()) {
			redirectAttributes.addFlashAttribute("errorMessage", "件名と内容は必須です。");
			return "redirect:/contact";
		}
		User user = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		contactService.saveContact(user, subject, message);

		String text = String.format("【お問い合わせ】\nユーザー: %s (%s)\n件名: %s\n内容:\n%s",
				user.getName(), user.getEmail(), subject, message);
		lineMessagingService.sendMessage(text);

		redirectAttributes.addFlashAttribute("successMessage", "お問い合わせを送信しました。");
		return "redirect:/contact";
	}
}
