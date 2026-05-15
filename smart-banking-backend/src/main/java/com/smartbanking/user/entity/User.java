package com.smartbanking.user.entity;

import com.smartbanking.account.entity.BankAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "uk_user_email", columnNames = "email") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/*
	 * User full name
	 */
	@Column(nullable = false, length = 100)
	private String name;

	/*
	 * Email should be unique because login email var honar aahe.
	 */
	@Column(nullable = false, unique = true, length = 150)
	private String email;

	/*
	 * Password later BCrypt format madhe save honar.
	 */
	@Column(nullable = false)
	private String password;

	/*
	 * USER or ADMIN
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	/*
	 * One User can have many Bank Accounts.
	 */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<BankAccount> bankAccounts = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	/*
	 * Record database madhe first time save hotana automatic createdAt set honar.
	 */
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();

		if (this.role == null) {
			this.role = Role.USER;
		}
	}

	/*
	 * Record update zala ki updatedAt automatic update honar.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}