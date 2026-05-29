package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.PlayerSeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerSeedRepository extends JpaRepository<PlayerSeed, Long> {
    List<PlayerSeed> findByPlayerId(Long playerId);
    PlayerSeed findByPlayerIdAndSeedId(Long playerId, Integer seedId);
}
