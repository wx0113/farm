package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.Seed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeedRepository extends JpaRepository<Seed, Integer> {
}
