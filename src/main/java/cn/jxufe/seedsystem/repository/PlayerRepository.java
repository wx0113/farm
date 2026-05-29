package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByNickname(String nickname);
    Player findByUsername(String username);

    @Query("SELECT p FROM Player p WHERE p.username LIKE %:username%")
    List<Player> findByUsernameLike(@Param("username") String username, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Player p WHERE p.username LIKE %:username%")
    long countByUsernameLike(@Param("username") String username);
}
