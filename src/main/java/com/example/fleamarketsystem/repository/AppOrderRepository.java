package com.example.fleamarketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.AppOrder;
import com.example.fleamarketsystem.entity.User;

@Repository
public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {

	Optional<AppOrder> findByPaymentIntentId(String paymentIntentId);

	List<AppOrder> findByBuyer(User buyer);

	List<AppOrder> findByItem_seller(User seller);
}