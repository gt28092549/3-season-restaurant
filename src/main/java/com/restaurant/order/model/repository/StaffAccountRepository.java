package com.restaurant.order.model.repository;

import com.restaurant.order.model.StaffAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {
    Optional<StaffAccount> findByUsernameAndPassword(String username, String password);
}