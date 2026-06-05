package org.example.common.repository;

import org.example.common.entity.FinancialOperation;
import org.example.common.enums.FinancialOperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FinancialOperationRepository extends JpaRepository<FinancialOperation, Long> {
    List<FinancialOperation> findByStatusIn(Collection<FinancialOperationStatus> statuses);
}
