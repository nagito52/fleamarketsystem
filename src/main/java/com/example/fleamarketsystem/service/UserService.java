package com.example.fleamarketsystem.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}
	
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Transactional
	public User saveUser(User user) {
		return userRepository.save(user);
	}
	
	@Transactional
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}
	
	@Transactional
	public void toggleUserEnabled(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		user.setEnabled(!user.isEnabled());
		userRepository.save(user);
	}
}
