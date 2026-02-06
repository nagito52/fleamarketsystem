package com.example.fleamarketsystem.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
	
	Page<Item> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
	Page<Item> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
	Page<Item> findByNameContainingIgnoreCaseAndCategoryIdAndStatus(String name, Long categoryId, String status, Pageable pageable);
	Page<Item> findByStatus(String status, Pageable pageable);
	List<Item> findBySeller(User seller);
}
