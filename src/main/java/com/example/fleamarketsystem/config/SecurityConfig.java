package com.example.fleamarketsystem.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/items/**").permitAll()
						.requestMatchers("/orders/stripe-webhook").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.formLogin(login -> login
						.loginPage("/login")
						// 自作の振り分けロジック（successHandler）を適用
						.successHandler(successHandler())
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/orders/stripe-webhook"));

		return http.build();
	}

	/**
	 * ログイン成功時の遷移先をロールごとに振り分けるハンドラー
	 */
	@Bean
	public AuthenticationSuccessHandler successHandler() {
		return (request, response, authentication) -> {
			Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

			if (roles.contains("ROLE_ADMIN")) {
				// 管理者の場合はダッシュボードへ
				response.sendRedirect("/admin/dashboard");
			} else {
				// 一般ユーザーの場合は商品一覧へ
				response.sendRedirect("/items");
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// 開発用：パスワードを暗号化せずに比較
		return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return email -> {
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

			return org.springframework.security.core.userdetails.User.builder()
					.username(user.getEmail())
					.password(user.getPassword())
					// 権限に "ROLE_" が付いていない場合に備え、正規化して渡す
					.authorities(user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole())
					.build();
		};
	}
}