package com.emailplatform.repository;

import com.emailplatform.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    List<Campaign> findByUserId(String userId);
}
