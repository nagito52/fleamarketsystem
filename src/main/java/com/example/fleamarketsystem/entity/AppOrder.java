package com.example.fleamarketsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	
	@Column(nullable = false)
	private String status = "購入済";
	
	@Column(name = "payment_intent_id", unique = true)
	private String paymentIntentId;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
}
