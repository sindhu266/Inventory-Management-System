package com.sindhu.Inventory.Management.System.service;

import com.sindhu.Inventory.Management.System.entity.ChangeType;
import com.sindhu.Inventory.Management.System.entity.StockLog;
import com.sindhu.Inventory.Management.System.repository.StockLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockLogService {

    private static final String SYSTEM_ACTOR = "System";

    private final StockLogRepository stockLogRepository;

    public StockLog saveLog(StockLog log) {
        if (log.getPerformedBy() == null || log.getPerformedBy().isBlank()) {
            log.setPerformedBy(getCurrentUsername());
        }
        if (log.getChangeDate() == null) {
            log.setChangeDate(LocalDateTime.now());
        }
        return stockLogRepository.save(log);
    }

    public StockLog createLog(Long productId, ChangeType changeType, Integer quantityChanged) {
        StockLog stockLog = StockLog.builder()
                .productId(productId)
                .changeType(changeType)
                .quantityChanged(quantityChanged)
                .performedBy(getCurrentUsername())
                .changeDate(LocalDateTime.now())
                .build();

        return stockLogRepository.save(stockLog);
    }

    public List<StockLog> getAllLogs() {
        return stockLogRepository.findAll();
    }

    public List<StockLog> getRecentLogs() {
        return stockLogRepository.findTop10ByOrderByChangeDateDesc();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SYSTEM_ACTOR;
        }

        String username = authentication.getName();
        return (username == null || username.isBlank()) ? SYSTEM_ACTOR : username;
    }
}