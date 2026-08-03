package com.emailplatform.repository;

import com.emailplatform.model.Campaign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CampaignRepositoryTest {

    @Autowired
    private CampaignRepository campaignRepository;

    @Test
    void testSaveAndFind() {
        Campaign c = new Campaign();
        c.setName("Test Campaign");
        c.setUserId("user-1");
        c.setStatus("draft");
        Campaign saved = campaignRepository.save(c);

        assertNotNull(saved.getId());
        assertTrue(campaignRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByUserId() {
        Campaign c1 = new Campaign();
        c1.setName("Campaign 1");
        c1.setUserId("user-1");
        c1.setStatus("draft");
        campaignRepository.save(c1);

        Campaign c2 = new Campaign();
        c2.setName("Campaign 2");
        c2.setUserId("user-1");
        c2.setStatus("sent");
        campaignRepository.save(c2);

        Campaign c3 = new Campaign();
        c3.setName("Campaign 3");
        c3.setUserId("user-2");
        c3.setStatus("draft");
        campaignRepository.save(c3);

        List<Campaign> result = campaignRepository.findByUserId("user-1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdEmpty() {
        List<Campaign> result = campaignRepository.findByUserId("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveMultipleCampaigns() {
        Campaign c1 = new Campaign();
        c1.setName("C1");
        c1.setUserId("user-1");
        c1.setStatus("draft");
        campaignRepository.save(c1);

        Campaign c2 = new Campaign();
        c2.setName("C2");
        c2.setUserId("user-1");
        c2.setStatus("sent");
        campaignRepository.save(c2);

        assertEquals(2, campaignRepository.count());
    }

    @Test
    void testDeleteCampaign() {
        Campaign c = new Campaign();
        c.setName("To Delete");
        c.setUserId("user-1");
        c.setStatus("draft");
        Campaign saved = campaignRepository.save(c);

        campaignRepository.deleteById(saved.getId());
        assertFalse(campaignRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCampaignDefaultValues() {
        Campaign c = new Campaign();
        c.setName("Defaults Test");
        c.setUserId("user-1");
        Campaign saved = campaignRepository.save(c);

        Campaign found = campaignRepository.findById(saved.getId()).get();
        assertEquals("draft", found.getStatus());
        assertEquals(0, found.getSentCount());
        assertEquals(0, found.getOpenedCount());
        assertEquals(0, found.getClickedCount());
    }

    @Test
    void testCampaignStatusTransitions() {
        Campaign c = new Campaign();
        c.setName("Status Test");
        c.setUserId("user-1");
        c.setStatus("draft");
        Campaign saved = campaignRepository.save(c);

        saved.setStatus("sending");
        campaignRepository.save(saved);

        Campaign found = campaignRepository.findById(saved.getId()).get();
        assertEquals("sending", found.getStatus());
    }

    @Test
    void testCount() {
        assertEquals(0, campaignRepository.count());

        Campaign c = new Campaign();
        c.setName("Count Test");
        c.setUserId("user-1");
        c.setStatus("draft");
        campaignRepository.save(c);

        assertEquals(1, campaignRepository.count());
    }
}
