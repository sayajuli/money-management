package com.finance.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.management.model.Asset;
import com.finance.management.model.AssetType;
import com.finance.management.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByUserId(Long userId);
    Optional<Asset> findByNameAndUser(String name, User user);

    Page<Asset> findByUserId(Long userId, Pageable pageable);
    Page<Asset> findByUserIdAndType(Long userId, AssetType type, Pageable pageable);
    Page<Asset> findByUserIdAndAcquisitionDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Asset> findByUserIdAndTypeAndAcquisitionDateBetween(Long userId, AssetType type, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
