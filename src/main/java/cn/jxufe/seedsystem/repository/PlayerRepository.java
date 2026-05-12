package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByNickname(String nickname);
}