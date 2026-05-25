package cn.jxufe.seedsystem.repository;

import cn.jxufe.seedsystem.entity.Seed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeedRepository extends JpaRepository<Seed, Integer> {

    // 根据种子编号查询（修复搜索功能必须加这个）
    List<Seed> findBySeedId(String seedId);
}