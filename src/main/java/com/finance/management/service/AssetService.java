package com.finance.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.management.dto.AssetDto;
import com.finance.management.model.Asset;
import com.finance.management.model.AssetType;
import com.finance.management.model.Transaction;
import com.finance.management.model.TransactionType;
import com.finance.management.model.User;
import com.finance.management.repository.AssetRepository;
import com.finance.management.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private static final String DEFAULT_CASH_ASSET_NAME = "Cash";

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private TransactionService transactionService;

    public List<Asset> getAssetsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return assetRepository.findByUserId(user.getId());
    }

    public List<Asset> getAssetsByType(String username, AssetType type) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return assetRepository.findByUserId(user.getId()).stream()
                .filter(asset -> asset.getType() == type)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Asset getAssetByIdAndUsername(Long id, String username) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aset tidak ditemukan dengan ID: " + id));
        if (!asset.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Anda tidak memiliki izin untuk mengakses aset ini.");
        }
        return asset;
    }

    public BigDecimal getTotalAssetValue(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return assetRepository.findByUserId(user.getId()).stream()
                .map(Asset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Asset createAsset(AssetDto assetDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Asset asset = new Asset();
        asset.setUser(user);
        asset.setType(assetDto.getType());
        asset.setName(assetDto.getName());
        asset.setCurrentValue(assetDto.getCurrentValue());
        asset.setAcquisitionDate(assetDto.getAcquisitionDate());

        return assetRepository.save(asset);
    }

    @Transactional
    public Asset addOrUpdateAsset(Asset assetData, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        Optional<Asset> existingAssetOpt = assetRepository.findByNameAndUser(assetData.getName(), user);

        BigDecimal amountSpent = assetData.getCurrentValue();

        if (existingAssetOpt.isPresent()) {
            Asset existingAsset = existingAssetOpt.get();
            BigDecimal difference = assetData.getCurrentValue().subtract(existingAsset.getCurrentValue());

            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                amountSpent = difference;
                existingAsset.setCurrentValue(assetData.getCurrentValue());
                assetRepository.save(existingAsset);
            } else {
                amountSpent = BigDecimal.ZERO;
            }

        } else {
            assetData.setUser(user);
            assetRepository.save(assetData);
        }

        if (amountSpent.compareTo(BigDecimal.ZERO) > 0) {
            Transaction expense = new Transaction();
            expense.setAmount(amountSpent);
            expense.setCategory("Pembelian Aset");
            expense.setDescription("Investasi/pembelian untuk: " + assetData.getName());
            expense.setType(TransactionType.EXPENSE);
            expense.setTransactionDate(LocalDate.now());
            transactionService.createTransaction(expense, username);
        }

        return assetData;
    }

    @Transactional
    public void updateAsset(Long id, Asset updatedAssetData, String username) {
        Asset existingAsset = getAssetByIdAndUsername(id, username);

        existingAsset.setName(updatedAssetData.getName());
        existingAsset.setType(updatedAssetData.getType());
        existingAsset.setCurrentValue(updatedAssetData.getCurrentValue());
        existingAsset.setAcquisitionDate(updatedAssetData.getAcquisitionDate());

        assetRepository.save(existingAsset);
    }

    @Transactional
    public void depositToCash(BigDecimal amount, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        Asset cashAsset = assetRepository.findByNameAndUser(DEFAULT_CASH_ASSET_NAME, user)
                .orElseGet(() -> {
                    Asset newCashAsset = new Asset();
                    newCashAsset.setName(DEFAULT_CASH_ASSET_NAME);
                    newCashAsset.setType(AssetType.CASH);
                    newCashAsset.setUser(user);
                    newCashAsset.setCurrentValue(BigDecimal.ZERO);
                    return newCashAsset;
                });

        cashAsset.setCurrentValue(cashAsset.getCurrentValue().add(amount));
        assetRepository.save(cashAsset);
    }

    @Transactional
    public void withdrawFromCash(BigDecimal amount, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        Asset cashAsset = assetRepository.findByNameAndUser(DEFAULT_CASH_ASSET_NAME, user)
                .orElseGet(() -> {
                    // Jika belum ada, buat baru (meskipun aneh jika pengeluaran terjadi tanpa kas)
                    Asset newCashAsset = new Asset();
                    newCashAsset.setName(DEFAULT_CASH_ASSET_NAME);
                    newCashAsset.setType(AssetType.CASH);
                    newCashAsset.setUser(user);
                    newCashAsset.setCurrentValue(BigDecimal.ZERO);
                    return newCashAsset;
                });

        cashAsset.setCurrentValue(cashAsset.getCurrentValue().subtract(amount));
        assetRepository.save(cashAsset);
    }

    public void deleteAsset(Long id, String username) {
        Asset asset = assetRepository.findById(id).orElseThrow();
        if (!asset.getUser().getUsername().equals(username))
            throw new AccessDeniedException("Access denied");
        assetRepository.delete(asset);
    }
}
