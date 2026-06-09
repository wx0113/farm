package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.GrowthStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrowthStageRepository extends JpaRepository<GrowthStage, Integer> {

    // 根据种子ID查询成长阶段，并按阶段序号排序
    List<GrowthStage> findBySeedIdOrderByStageOrder(Integer seedId);

    // 根据种子ID删除所有阶段（级联删除）
    void deleteBySeedId(Integer seedId);

    // 根据种子ID和阶段序号查询单个阶段
    Optional<GrowthStage> findBySeedIdAndStageOrder(Integer seedId, Integer stageOrder);

    // 根据种子ID和作物状态查询（用于查找枯草阶段等特殊阶段）
    Optional<GrowthStage> findBySeedIdAndCropStatus(Integer seedId, String cropStatus);
}