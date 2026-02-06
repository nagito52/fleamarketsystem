package com.example.fleamarketsystem.service;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.AppOrderRepository;
import com.example.fleamarketsystem.repository.UserRepository;

@Service
public class AdminUserService {

	private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOG");
	private final UserRepository userRepository;
	private final AppOrderRepository appOrderRepository;

	public AdminUserService(UserRepository userRepository, AppOrderRepository appOrderRepository) {
		this.userRepository = userRepository;
		this.appOrderRepository = appOrderRepository;
	}

	@Transactional
	public void banUser(Long userId, Long adminId, String reason, boolean disableLogin) {
		if (reason == null || reason.trim().isEmpty()) {
			throw new IllegalArgumentException("BANの理由は必須です。");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("対象ユーザーが見つかりません。"));

		user.setBanned(true); // User.javaに追加したフィールド
		user.setBanReason(reason); // User.javaに追加したフィールド
		user.setEnabled(!disableLogin);
		userRepository.save(user);

		auditLogger.info("USER_BAN: Admin(ID:{}) banned User(ID:{})", adminId, userId);
	}

	@Transactional
	public void unbanUser(Long id) {
		userRepository.findById(id).ifPresent(u -> {
			u.setBanned(false);
			u.setBanReason(null);
			userRepository.save(u);
			auditLogger.info("USER_UNBAN: User(ID:{}) was unbanned.", id);
		});
	}

	// AdminUserService.java (末尾付近に追加)

	@Transactional
	public void toggleUserEnabled(Long id) {
		userRepository.findById(id).ifPresent(user -> {
			// 現在の状態を反転させる (trueならfalse、falseならtrue)
			user.setEnabled(!user.isEnabled());
			userRepository.save(user);
			auditLogger.info("USER_TOGGLE: User(ID:{}) enabled status changed to {}", id, user.isEnabled());
		});
	}

	// 他、listAllUsers(), writeStatisticsCsv() 等の既存メソッドはそのまま
	public List<User> listAllUsers() {
		return userRepository.findAll();
	}

	public User findUser(Long id) {
		return userRepository.findById(id).orElse(null);
	}

	public Double averageRating(Long id) {
		return 0.0;
	}

	public long complaintCount(Long id) {
		return 0;
	}

	public List<String> complaints(Long id) {
		return List.of();
	}

	public void writeStatisticsCsv(PrintWriter writer, LocalDate start, LocalDate end) {
		// ... (以前作成したCSV出力ロジック)
	}
}