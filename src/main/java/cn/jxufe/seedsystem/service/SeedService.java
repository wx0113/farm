package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.Seed;
import cn.jxufe.seedsystem.entity.GrowthStage;
import cn.jxufe.seedsystem.repository.SeedRepository;
import cn.jxufe.seedsystem.repository.GrowthStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public List<Seed> searchById(Integer id) {
        return seedRepository.findById(id)
                .map(List::of)
                .orElse(List.of());
    }

    public List<Seed> searchByName(String name) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Seed> query = cb.createQuery(Seed.class);
        Root<Seed> root = query.from(Seed.class);
        query.where(cb.like(root.get("seedName"), "%" + name + "%"));
        query.orderBy(cb.asc(root.get("id")));
        return entityManager.createQuery(query).getResultList();
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
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (seed.getId() != null && seedRepository.existsById(seed.getId())) {
            // 编辑已有种子：只更新传入的字段，保留 createdAt
            Seed existing = seedRepository.findById(seed.getId()).get();
            if (seed.getSeedName() != null) existing.setSeedName(seed.getSeedName());
            if (seed.getXSeasonCrop() != null) existing.setXSeasonCrop(seed.getXSeasonCrop());
            if (seed.getSeedLevel() != null) existing.setSeedLevel(seed.getSeedLevel());
            if (seed.getSeedType() != null) existing.setSeedType(seed.getSeedType());
            if (seed.getExperience() != null) existing.setExperience(seed.getExperience());
            if (seed.getMaturityTime() != null) existing.setMaturityTime(seed.getMaturityTime());
            if (seed.getHarvestCount() != null) existing.setHarvestCount(seed.getHarvestCount());
            if (seed.getPurchasePrice() != null) existing.setPurchasePrice(seed.getPurchasePrice());
            if (seed.getFruitPrice() != null) existing.setFruitPrice(seed.getFruitPrice());
            if (seed.getLandRequirement() != null) existing.setLandRequirement(seed.getLandRequirement());
            if (seed.getPoints() != null) existing.setPoints(seed.getPoints());
            if (seed.getTipInfo() != null) existing.setTipInfo(seed.getTipInfo());
            existing.setUpdatedAt(now);
            seedRepository.save(existing);
        } else {
            // 新增种子
            if (seed.getId() == null) {
                Long maxId = entityManager
                    .createQuery("SELECT COALESCE(MAX(s.id), 0) FROM Seed s", Long.class)
                    .getSingleResult();
                seed.setId(maxId.intValue() + 1);
            }
            seed.setCreatedAt(now);
            seed.setSeedId(String.valueOf(seed.getId()));
            seed.setUpdatedAt(now);
            seedRepository.save(seed);
        }
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

    /** 启动时初始化：清理 stageOrder=0 + 为每个种子补上枯草阶段 */
    @PostConstruct
    public void initGrowthStages() {
        List<GrowthStage> all = growthStageRepository.findAll();

        // 1. 清理 stageOrder=0
        List<GrowthStage> toDelete = new ArrayList<>();
        for (GrowthStage gs : all) {
            if (gs.getStageOrder() != null && gs.getStageOrder() == 0) {
                toDelete.add(gs);
            }
        }
        if (!toDelete.isEmpty()) {
            growthStageRepository.deleteAll(toDelete);
            System.out.println("[Init] 已清理 " + toDelete.size() + " 个 stageOrder=0 的废弃阶段");
        }

        // 2. 为每个种子补上枯草阶段（cropStatus='枯萎'）
        List<Seed> allSeeds = seedRepository.findAll();
        int created = 0;
        for (Seed seed : allSeeds) {
            if (growthStageRepository.findBySeedIdAndCropStatus(seed.getId(), "枯萎").isPresent()) {
                continue; // 已有枯草阶段
            }
            List<GrowthStage> stages = growthStageRepository.findBySeedIdOrderByStageOrder(seed.getId());
            int maxOrder = stages.stream()
                    .mapToInt(gs -> gs.getStageOrder() != null ? gs.getStageOrder() : 0)
                    .max().orElse(0);

            GrowthStage withered = new GrowthStage();
            withered.setSeedId(seed.getId());
            withered.setStageOrder(maxOrder + 1);
            withered.setStageTitle("枯草");
            withered.setStageDuration(0);
            withered.setCropStatus("枯萎");
            withered.setImageUrl("/images/crops/basic/9.png");
            withered.setImageWidth(200);      // 默认 200px（土地区域 293×150 中约占 68%，可在定位器中调整）
            withered.setImageHeight(100);
            withered.setImageOffsetX(47);     // (293-200)/2 = 47，居中
            withered.setImageOffsetY(25);     // (150-100)/2 = 25，居中
            withered.setPestProbability(BigDecimal.ZERO);
            withered.setCreatedAt(LocalDateTime.now());
            withered.setUpdatedAt(LocalDateTime.now());
            growthStageRepository.save(withered);
            created++;
        }
        if (created > 0) {
            System.out.println("[Init] 已为 " + created + " 个种子自动创建枯草阶段");
        }

        // 3. 旧百分比 → 像素转换（值 ≤100 说明是旧百分比数据，转为土地区域像素值）
        // 土地区域：定位器中 293×150（positioning.png 底部），农场中 land-bg 显示区域
        final int LAND_W = 293;
        final int LAND_H = 150;
        int converted = 0;
        for (GrowthStage gs : growthStageRepository.findAll()) {
            Integer w = gs.getImageWidth();
            if (w != null && w > 0 && w <= 100) {
                gs.setImageWidth(Math.round(w / 100.0f * LAND_W));
                gs.setImageHeight(gs.getImageHeight() != null && gs.getImageHeight() <= 100 ? Math.round(gs.getImageHeight() / 100.0f * LAND_H) : 100);
                gs.setImageOffsetX(gs.getImageOffsetX() != null && gs.getImageOffsetX() <= 100 ? Math.round(gs.getImageOffsetX() / 100.0f * LAND_W) : 47);
                gs.setImageOffsetY(gs.getImageOffsetY() != null && gs.getImageOffsetY() <= 100 ? Math.round(gs.getImageOffsetY() / 100.0f * LAND_H) : 25);
                growthStageRepository.save(gs);
                converted++;
            }
        }
        if (converted > 0) {
            System.out.println("[Init] 已将 " + converted + " 个阶段的偏移值从百分比转为土地区域像素");
        }

        // 4. 修复异常 imageUrl：空值、缺少前导 /、指向错误的种子ID
        int fixed = 0;
        for (GrowthStage gs : growthStageRepository.findAll()) {
            String url = gs.getImageUrl();
            boolean needsFix = false;

            if (url == null || url.isEmpty()) {
                needsFix = true; // 空路径
            } else if (!url.startsWith("/")) {
                needsFix = true; // 相对路径，缺少前导 /
            } else if (gs.getSeedId() != null && !"枯萎".equals(gs.getCropStatus())) {
                // 检查 URL 中的种子ID目录是否与当前记录的 seedId 一致
                String expectedPrefix = "/images/crops/" + gs.getSeedId() + "/";
                String basicPrefix = "/images/crops/basic/";
                if (!url.startsWith(expectedPrefix) && !url.startsWith(basicPrefix)) {
                    needsFix = true; // 指向了错误的种子目录
                }
            }

            if (needsFix) {
                if ("枯萎".equals(gs.getCropStatus())) {
                    gs.setImageUrl("/images/crops/basic/9.png");
                } else if (gs.getStageOrder() != null && gs.getStageOrder() == 1) {
                    gs.setImageUrl("/images/crops/basic/0.png");
                } else if (gs.getSeedId() != null && gs.getStageOrder() != null) {
                    gs.setImageUrl("/images/crops/" + gs.getSeedId() + "/" + gs.getStageOrder() + ".png");
                }
                growthStageRepository.save(gs);
                fixed++;
            }
        }
        if (fixed > 0) {
            System.out.println("[Init] 已修复 " + fixed + " 个阶段的异常 imageUrl（空/相对路径/错误种子ID）");
        }
    }
}