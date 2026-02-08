package com.example.fleamarketsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

	List<Contact> findAllByOrderByCreatedAtDesc();

	List<Contact> findByReadFalseOrderByCreatedAtDesc();

	long countByReadFalse();
}
