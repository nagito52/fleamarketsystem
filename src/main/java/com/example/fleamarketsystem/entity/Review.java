package com.example.fleamarketsystem.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private AppOrder order;
	
	@ManyToOne
	@JoinColumn(name = "reviewer_id", nullable = false)
	private User reviewer;
	
	@ManyToOne
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;
	
	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;
	
	@Column(nullable = false)
	private Integer rating;
	
	@Column(columnDefinition = "TEXT")
	private String comment;
	
	private LocalDateTime createdAt = LocalDateTime.now();
}
