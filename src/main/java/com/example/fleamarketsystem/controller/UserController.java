package com.example.fleamarketsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.FavoriteService;
import com.example.fleamarketsystem.service.ItemService;
import com.example.fleamarketsystem.service.ReviewService;
import com.example.fleamarketsystem.service.UserService;

@Controller
@RequestMapping("/my_page")
public class UserController {

    private final UserService userService;
    private final ItemService itemService;
    private final AppOrderService appOrderService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    public UserController(UserService userService, ItemService itemService, AppOrderService appOrderService, FavoriteService favoriteService, ReviewService reviewService) {
        this.userService = userService;
        this.itemService = itemService;
        this.appOrderService = appOrderService;
        this.favoriteService = favoriteService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", currentUser);
        return "my_page";
    }

    @GetMapping("/selling")
    public String mySellingItems(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("sellingItems", itemService.getItemsBySeller(currentUser));
        return "seller_items";
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        // Serviceに作成したメソッドを呼び出す
        model.addAttribute("myOrders", appOrderService.getOrdersByBuyer(currentUser));
        return "buyer_app_orders";
    }

    @GetMapping("/sales")
    public String mySales(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        // Serviceに作成したメソッドを呼び出す
        model.addAttribute("mySales", appOrderService.getOrdersBySeller(currentUser));
        return "seller_app_orders";
    }

    @GetMapping("/favorites")
    public String myFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("favoriteItems", favoriteService.getFavoriteItemsByUser(currentUser));
        return "my_favorites";
    }

    @GetMapping("/reviews")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("reviews", reviewService.getReviewsByReviewer(currentUser));
        return "user_reviews";
    }
}