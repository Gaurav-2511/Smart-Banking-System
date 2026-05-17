package com.smartbanking.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartbanking.user.entity.Role;
import com.smartbanking.user.entity.User;
import java.util.Optional;
import java.util.List;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	List<User> findByRole(Role role);

}
