package com.example.fleamarketsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.UserService;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        
        // --- 1. バリデーション ---
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            model.addAttribute("errorMessage", "パスワードは8文字以上で入力してください。");
            return "signup";
        }
        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "このメールアドレスは既に登録されています。");
            return "signup";
        }

        // --- 2. DBへの保存（これが最優先！） ---
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setBanned(false);
        userService.saveUser(user);

        // --- 3. 自動ログイン処理（セッションに保存される認証） ---
        try {
            request.login(user.getEmail(), user.getPassword());
        } catch (ServletException e) {
            model.addAttribute("errorMessage", "自動ログインに失敗しました。再度ログインしてください。");
            return "login";
        }

        redirectAttributes.addFlashAttribute("successMessage", "ご登録ありがとうございます！");
        
        // ログイン済みなので、直接商品一覧へ
        return "redirect:/items";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}