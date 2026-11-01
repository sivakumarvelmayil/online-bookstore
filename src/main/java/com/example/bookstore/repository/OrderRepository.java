
package com.example.bookstore.repository;

import com.example.bookstore.entity.CustomerOrder;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"items", "items.book"})
    List<CustomerOrder> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.book"})
    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);
}
