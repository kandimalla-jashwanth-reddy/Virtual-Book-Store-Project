package com.example.demo.repository;

import com.example.demo.entites.SellerEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerEarningRepository extends JpaRepository<SellerEarning, Long> {
    List<SellerEarning> findBySellerId(Long sellerId);
}
