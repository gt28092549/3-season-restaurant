package com.restaurant.order.model.repository;

import com.restaurant.order.model.StaffCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffCallRepository extends JpaRepository<StaffCall, Long> {
    List<StaffCall> findByStatus(String status);
}