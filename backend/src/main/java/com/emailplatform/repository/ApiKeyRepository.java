package com.emailplatform.repository;

import com.emailplatform.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    List<ApiKey> findByUserId(String userId);
    Optional<ApiKey> findByKeyValue(String keyValue);
}
