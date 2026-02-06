package com.example.fleamarketsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.Chat;
import com.example.fleamarketsystem.entity.Item;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

	List<Chat> findByItemOrderByCreatedAtAsc(Item item);
}
