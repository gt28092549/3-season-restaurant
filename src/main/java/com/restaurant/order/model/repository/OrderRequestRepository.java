package com.restaurant.order.model.repository;

import com.restaurant.order.model.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
    List<OrderRequest> findByTableNumber(Integer tableNumber);
    List<OrderRequest> findByStatus(String status);
}