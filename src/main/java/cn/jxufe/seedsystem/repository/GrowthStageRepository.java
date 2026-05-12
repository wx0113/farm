package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.GrowthStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthStageRepository extends JpaRepository<GrowthStage, Integer> {

    List<GrowthStage> findBySeedIdOrderByStageOrder(Integer seedId);

    void deleteBySeedId(Integer seedId);
}
