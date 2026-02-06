package com.example.fleamarketsystem.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;
import com.example.fleamarketsystem.service.AdminUserService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService service;
	private final UserRepository users;

	public AdminUserController(AdminUserService service, UserRepository users) {
		this.service = service;
		this.users = users;
	}

	@GetMapping
	public String list(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "sort", required = false, defaultValue = "id") String sort, Model model) {
		List<User> list = service.listAllUsers();

		if (StringUtils.hasText(q)) {
			String qq = q.toLowerCase();
			list = list.stream().filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(qq)) ||
					(u.getEmail() != null && u.getEmail().toLowerCase().contains(qq)))
					.toList();
		}

		list = switch (sort) {
		case "name" -> list.stream().sorted(Comparator.comparing(User::getName,
				Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		case "email" -> list.stream().sorted(Comparator.comparing(User::getEmail,
				Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		// User::isBanned が使えるようになります
		case "banned" -> list.stream()
				.sorted(Comparator.comparing(User::isBanned).reversed()).toList();
		default -> list;
		};

		model.addAttribute("users", list);
		model.addAttribute("q", q);
		model.addAttribute("sort", sort);
		return "admin_users";
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		// 指定 ID のユーザー情報をサービスから取得
		User user = service.findUser(id);
		// 指定ユーザーの平均評価値を取得
		Double avg = service.averageRating(id);
		// 指定ユーザーに対するクレーム件数を取得
		long complaints = service.complaintCount(id);
		// ユーザー情報を画面表示用に Model に格納
		model.addAttribute("user", user);
		// 平均評価を Model に格納
		model.addAttribute("avgRating", avg);
		// クレーム件数を Model に格納
		model.addAttribute("complaintCount", complaints);
		// クレーム詳細一覧を Model に格納
		model.addAttribute("complaints", service.complaints(id));
		// ユーザー詳細画面に対応するテンプレート名を返却
		return "admin/users/detail";
	}

	// ユーザーを BAN（利用停止）する処理を担当するハンドラー（POST /admin/users/{id}/ban）
	@PostMapping("/{id}/ban")
	public String ban(@PathVariable Long id,
			@RequestParam("reason") String reason,
			@RequestParam(value = "disableLogin", defaultValue = "true") boolean disableLogin,
			Authentication auth) {
		// 認証情報から現在ログイン中の管理者のメールアドレスを取得し、
		// 対応する管理者ユーザーID を取得
		Long adminId = users.findByEmailIgnoreCase(auth.getName()).map(User::getId).orElse(null);
		// 対象ユーザーを BAN し、その操作を行った管理者 ID・理由・ログイン停止フラグを渡す
		service.banUser(id, adminId, reason, disableLogin);
		// 対象ユーザー詳細画面へリダイレクトし、クエリパラメータで BAN 済みであることを通知
		return "redirect:/admin/users/" + id + "?banned";
	}

	// ユーザーの BAN を解除する処理を担当するハンドラー（POST /admin/users/{id}/unban）
	@PostMapping("/{id}/unban")
	public String unban(@PathVariable Long id) {
		// 指定ユーザーの BAN 状態を解除するようサービスに依頼
		service.unbanUser(id);
		// 対象ユーザー詳細画面へリダイレクトし、クエリパラメータで解除済みであることを通知
		return "redirect:/admin/users/" + id + "?unbanned";
	}

	// AdminUserController.java

	// クラスの上部に @RequestMapping("/admin/users") があることを前提とします
	@PostMapping("/{id}/toggle-enabled")
	public String toggleEnabled(@PathVariable("id") Long id) {
		// ユーザーの有効・無効状態を反転させる
		service.toggleUserEnabled(id);
		// 処理が終わったら元のユーザー一覧画面に戻る
		return "redirect:/admin/users";
	}
}