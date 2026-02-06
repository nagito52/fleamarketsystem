package com.example.fleamarketsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

    // 管理者のメールアドレスで検索するために必要（大文字小文字を無視）
    Optional<User> findByEmailIgnoreCase(String email);
}