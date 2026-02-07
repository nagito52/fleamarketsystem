package com.example.fleamarketsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_order")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AppOrder {
	
	public static final String STATUS_TRADING = "取引中";
	public static final String STATUS_CANCEL_REQUESTED = "キャンセル要請中";
	public static final String STATUS_CANCEL_AGREED = "キャンセル同意済";
	public static final String STATUS_CANCELLED = "キャンセル済";
	public static final String STATUS_SHIPPED = "発送済";
	public static final String STATUS_COMPLETED = "売却済";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;
	
	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;
	
	@Column(nullable = false)
	private BigDecimal price;
	
	@Column(name = "payment_intent_id", unique = true)
	private String paymentIntentId;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(nullable = false)
    private String status = STATUS_TRADING;

	@Column(nullable = false)
	private boolean buyerCancelRequested = false; // 購入者がキャンセルボタンを押したか

	@Column(nullable = false)
	private boolean sellerCancelApproved = false; // 出品者が了承ボタンを押したか

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews;
}
