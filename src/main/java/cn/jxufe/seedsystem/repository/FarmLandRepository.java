package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.FarmLand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FarmLandRepository extends JpaRepository<FarmLand, Long> {
    List<FarmLand> findByPlayerId(Long playerId);
    FarmLand findByPlayerIdAndLandIndex(Long playerId, int landIndex);
    List<FarmLand> findBySeedIdIsNotNull();
}
