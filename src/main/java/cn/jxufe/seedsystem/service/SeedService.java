package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.Seed;
import cn.jxufe.seedsystem.entity.GrowthStage;
import cn.jxufe.seedsystem.repository.SeedRepository;
import cn.jxufe.seedsystem.repository.GrowthStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SeedService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SeedRepository seedRepository;

    @Autowired
    private GrowthStageRepository growthStageRepository;

    // ====================== 【修复】新增：根据 seedId 查询 ======================
    public List<Seed> searchBySeedId(String seedId) {
        return seedRepository.findBySeedId(seedId);
    }

    public List<Seed> querySeeds(String seedName, int page, int rows) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Seed> query = cb.createQuery(Seed.class);
        Root<Seed> root = query.from(Seed.class);

        List<Predicate> predicates = new ArrayList<>();
        if (seedName != null && !seedName.trim().isEmpty()) {
            predicates.add(cb.like(root.get("seedName"), "%" + seedName + "%"));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("id")));

        int firstResult = (page - 1) * rows;
        return entityManager.createQuery(query)
                .setFirstResult(firstResult)
                .setMaxResults(rows)
                .getResultList();
    }

    public long countSeeds(String seedName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Seed> root = query.from(Seed.class);
        query.select(cb.count(root));

        if (seedName != null && !seedName.trim().isEmpty()) {
            query.where(cb.like(root.get("seedName"), "%" + seedName + "%"));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    public void saveSeed(Seed seed) {
        if (seed.getCreatedAt() == null) {
            seed.setCreatedAt(java.time.LocalDateTime.now());
        }
        seed.setUpdatedAt(java.time.LocalDateTime.now());
        seedRepository.save(seed);
    }

    public void deleteSeed(Integer id) {
        // 先删除关联的成长阶段
        growthStageRepository.deleteBySeedId(id);
        seedRepository.deleteById(id);
    }

    public List<GrowthStage> queryGrowthStages(Integer seedId) {
        return growthStageRepository.findBySeedIdOrderByStageOrder(seedId);
    }

    public void saveGrowthStage(GrowthStage stage) {
        if (stage.getCreatedAt() == null) {
            stage.setCreatedAt(java.time.LocalDateTime.now());
        }
        stage.setUpdatedAt(java.time.LocalDateTime.now());
        growthStageRepository.save(stage);
    }

    public void deleteGrowthStage(Integer id) {
        growthStageRepository.deleteById(id);
    }
}