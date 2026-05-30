package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.*;
import cn.jxufe.seedsystem.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FarmGameService {

    @Autowired private FarmLandRepository landRepo;
    @Autowired private SeedRepository seedRepo;
    @Autowired private GrowthStageRepository growthStageRepo;
    @Autowired private PlayerRepository playerRepo;
    @Autowired private PlayerSeedRepository playerSeedRepo;

    // 待推送的消息队列（按玩家ID分组）
    private final Map<Long, List<Map<String, Object>>> pendingMessages = new HashMap<>();

    /** 获取当前玩家的所有土地状态 */
    public List<Map<String, Object>> getLandStates(Long playerId) {
        List<FarmLand> lands = landRepo.findByPlayerId(playerId);
        if (lands.isEmpty()) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (FarmLand land : lands) {
            result.add(toLandState(land));
        }
        return result;
    }

    /** 获取待推送消息并清空 */
    public synchronized List<Map<String, Object>> fetchMessages(Long playerId) {
        List<Map<String, Object>> msgs = pendingMessages.remove(playerId);
        return msgs != null ? msgs : new ArrayList<>();
    }

    private synchronized void pushMessage(Long playerId, Map<String, Object> msg) {
        pendingMessages.computeIfAbsent(playerId, k -> new ArrayList<>()).add(msg);
    }

    // ======================== 定时任务：每2秒 ========================
    @Scheduled(fixedRate = 2000)
    public void farmTick() {
        try {
            List<FarmLand> allLands = landRepo.findBySeedIdIsNotNull();
            for (FarmLand land : allLands) {
                if (land.isWithered()) continue;
                processGrowth(land);
            }
        } catch (Exception e) {
            // 防止异常导致调度器停止
            System.err.println("[FarmTick] 定时任务异常: " + e.getMessage());
        }
    }

    private void processGrowth(FarmLand land) {
        Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);
        if (seed == null) return;

        List<GrowthStage> stages = growthStageRepo.findBySeedIdOrderByStageOrder(seed.getId());
        if (stages.isEmpty()) return;

        int maxStages = stages.size();
        int currentStageOrder = land.getCurrentStage();

        // 已成熟
        if (currentStageOrder < 0) return;

        GrowthStage currentGs = null;
        if (currentStageOrder >= 1 && currentStageOrder <= maxStages) {
            currentGs = stages.get(currentStageOrder - 1);
        }

        if (currentGs == null) {
            // 检查是否应该进入成熟
            if (currentStageOrder > maxStages) {
                land.setCurrentStage(-1); // 成熟标志
                land.setStageStartTime(LocalDateTime.now());
                landRepo.save(land);
                Map<String, Object> msg = new HashMap<>();
                msg.put("type", "mature");
                msg.put("landIndex", land.getLandIndex());
                pushMessage(land.getPlayerId(), msg);
            }
            return;
        }

        // 检查阶段时间是否到达
        long elapsed = java.time.Duration.between(land.getStageStartTime(), LocalDateTime.now()).getSeconds();
        int duration = currentGs.getStageDuration() != null ? currentGs.getStageDuration() : 60;

        if (elapsed >= duration) {
            // 如果本阶段有虫未除，减产
            if (land.isHasWorm()) {
                int reduction = 1 + (int)(Math.random() * 2); // 1-2
                land.setYieldReduction(land.getYieldReduction() + reduction);
                land.setHasWorm(false);
            }

            // 进入下一阶段
            int nextStage = currentStageOrder + 1;
            if (nextStage > maxStages) {
                // 成熟了
                land.setCurrentStage(-1);
                land.setStageStartTime(LocalDateTime.now());
                land.setHasWorm(false);
                landRepo.save(land);
                Map<String, Object> msg = new HashMap<>();
                msg.put("type", "mature");
                msg.put("landIndex", land.getLandIndex());
                pushMessage(land.getPlayerId(), msg);
            } else {
                // 进入下一生长阶段
                GrowthStage nextGs = stages.get(nextStage - 1);
                land.setCurrentStage(nextStage);
                land.setStageStartTime(LocalDateTime.now());

                // 生虫概率判断
                BigDecimal pestProb = nextGs.getPestProbability();
                if (pestProb != null && Math.random() < pestProb.doubleValue()) {
                    land.setHasWorm(true);
                    landRepo.save(land);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("type", "worm");
                    msg.put("landIndex", land.getLandIndex());
                    pushMessage(land.getPlayerId(), msg);
                } else {
                    land.setHasWorm(false);
                    landRepo.save(land);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("type", "stageChange");
                    msg.put("landIndex", land.getLandIndex());
                    msg.put("stage", nextStage);
                    msg.put("imageUrl", nextGs.getCropImage() != null ? nextGs.getCropImage() : nextGs.getImageUrl());
                    pushMessage(land.getPlayerId(), msg);
                }
            }
        }
    }

    // ======================== 播种 ========================
    @Transactional
    public Message actionPlant(int landIndex, int seedId, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");

        // 检查土地
        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) {
            // 自动创建
            land = new FarmLand();
            land.setPlayerId(player.getId());
            land.setLandIndex(landIndex);
        }
        if (!land.isEmpty() && !land.isWithered()) {
            return new Message(-2, "该土地已有作物");
        }

        // 检查种子
        Seed seed = seedRepo.findById(seedId).orElse(null);
        if (seed == null) return new Message(-3, "种子不存在");

        // 检查土地类型
        if (seed.getLandRequirement() != null && !seed.getLandRequirement().isEmpty()
                && land.getLandType() != null && !seed.getLandRequirement().equals(land.getLandType())) {
            return new Message(-4, "该种子需要" + seed.getLandRequirement() + "，当前为" + land.getLandType());
        }

        // 检查库存
        PlayerSeed ps = playerSeedRepo.findByPlayerIdAndSeedId(player.getId(), seedId);
        if (ps == null || ps.getQuantity() <= 0) {
            return new Message(-5, "种袋中没有足够的种子");
        }

        // 消耗种子
        ps.setQuantity(ps.getQuantity() - 1);
        playerSeedRepo.save(ps);

        // 更新土地
        land.setSeedId(seedId);
        land.setCurrentStage(1);
        land.setCurrentSeason(1);
        land.setStageStartTime(LocalDateTime.now());
        land.setPlantTime(LocalDateTime.now());
        land.setHasWorm(false);
        land.setWithered(false);
        land.setYieldReduction(0);

        // 第一阶段生虫概率
        GrowthStage firstGs = growthStageRepo.findBySeedIdOrderByStageOrder(seedId)
                .stream().findFirst().orElse(null);
        if (firstGs != null && firstGs.getPestProbability() != null
                && Math.random() < firstGs.getPestProbability().doubleValue()) {
            land.setHasWorm(true);
        }

        landRepo.save(land);

        // 推送消息
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "planted");
        msg.put("landIndex", landIndex);
        msg.put("seedId", seedId);
        msg.put("seedName", seed.getSeedName());
        msg.put("imageUrl", "/images/crops/basic/0.png");
        pushMessage(player.getId(), msg);

        return new Message(0, "播种成功！");
    }

    // ======================== 除虫 ========================
    @Transactional
    public Message actionKillWorm(int landIndex, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");

        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) return new Message(-2, "土地不存在");
        if (land.isEmpty()) return new Message(-2, "土地上没有作物");
        if (!land.isHasWorm()) return new Message(-3, "该作物没有虫害");

        // 除虫
        land.setHasWorm(false);
        landRepo.save(land);

        // 奖励
        player.setExp(player.getExp() + 2);
        player.setGold(player.getGold() + 1);
        player.setPoints(player.getPoints() + 2);
        playerRepo.save(player);
        session.setAttribute("currentPlayer", player);

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "wormKilled");
        msg.put("landIndex", landIndex);
        pushMessage(player.getId(), msg);

        return new Message(0, "除虫成功！经验+2 金币+1 积分+2");
    }

    // ======================== 收获 ========================
    @Transactional
    public Message actionHarvest(int landIndex, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");

        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) return new Message(-2, "土地不存在");
        if (land.isEmpty() || land.isWithered()) return new Message(-2, "该土地没有可收获的作物");
        if (land.getCurrentStage() != -1) return new Message(-3, "作物尚未成熟");

        // 进入枯草状态
        land.setWithered(true);
        land.setHasWorm(false);
        landRepo.save(land);

        // 奖励
        Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);
        if (seed != null) {
            int harvestCount = (seed.getHarvestCount() != null ? seed.getHarvestCount() : 1) - land.getYieldReduction();
            if (harvestCount < 1) harvestCount = 1;
            int expGain = seed.getExperience() != null ? seed.getExperience() : 5;
            int goldGain = (seed.getFruitPrice() != null ? seed.getFruitPrice().intValue() : 1) * harvestCount;
            int pointGain = seed.getPoints() != null ? seed.getPoints() : 2;

            player.setExp(player.getExp() + expGain);
            player.setGold(player.getGold() + goldGain);
            player.setPoints(player.getPoints() + pointGain);
            playerRepo.save(player);
            session.setAttribute("currentPlayer", player);

            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "harvested");
            msg.put("landIndex", landIndex);
            msg.put("harvestCount", harvestCount);
            msg.put("expGain", expGain);
            msg.put("goldGain", goldGain);
            msg.put("pointGain", pointGain);
            pushMessage(player.getId(), msg);

            return new Message(0, "收获成功！获得" + harvestCount + "个果实 经验+" + expGain + " 金币+" + goldGain + " 积分+" + pointGain);
        }
        return new Message(0, "收获成功！");
    }

    // ======================== 除枯草 ========================
    @Transactional
    public Message actionCleanLand(int landIndex, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");

        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) return new Message(-2, "土地不存在");
        if (!land.isWithered()) return new Message(-2, "该土地没有枯草需要清除");

        Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);

        // 直接清空土地
        land.setSeedId(null);
        land.setCurrentStage(null);
        land.setCurrentSeason(null);
        land.setStageStartTime(null);
        land.setPlantTime(null);
        land.setHasWorm(false);
        land.setWithered(false);
        land.setYieldReduction(0);
        landRepo.save(land);

        // 奖励
        player.setExp(player.getExp() + 5);
        player.setPoints(player.getPoints() + 5);
        playerRepo.save(player);
        session.setAttribute("currentPlayer", player);

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "cleaned");
        msg.put("landIndex", landIndex);
        pushMessage(player.getId(), msg);

        return new Message(0, "除枯草成功！经验+5 积分+5");
    }

    // ======================== 工具方法 ========================
    private Map<String, Object> toLandState(FarmLand land) {
        Map<String, Object> state = new HashMap<>();
        state.put("landIndex", land.getLandIndex());
        state.put("landType", land.getLandType());
        state.put("seedId", land.getSeedId());
        state.put("currentStage", land.getCurrentStage());
        state.put("hasWorm", land.isHasWorm());
        state.put("withered", land.isWithered());
        state.put("currentSeason", land.getCurrentSeason());

        if (land.getSeedId() != null) {
            Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);
            if (seed != null) {
                state.put("seedName", seed.getSeedName());
                if (land.isWithered()) {
                    state.put("imageUrl", "/images/crops/basic/9.png");
                    state.put("status", "withered");
                } else if (land.getCurrentStage() != null && land.getCurrentStage() == -1) {
                    // 成熟
                    state.put("imageUrl", "/images/crops/" + seed.getId() + "/5.png");
                    state.put("status", "mature");
                } else if (land.getCurrentStage() != null && land.getCurrentStage() == 1) {
                    // 种子阶段
                    state.put("imageUrl", "/images/crops/basic/0.png");
                    state.put("status", land.isHasWorm() ? "worm" : "growing");
                } else if (land.getCurrentStage() != null) {
                    int stage = land.getCurrentStage();
                    state.put("imageUrl", "/images/crops/" + seed.getId() + "/" + stage + ".png");
                    state.put("status", land.isHasWorm() ? "worm" : "growing");
                }
                // 生虫时附加 worm 图片
                if (land.isHasWorm()) {
                    state.put("wormImage", "/images/worm.png");
                }
            }
        } else {
            state.put("status", "empty");
        }
        return state;
    }

    private int parseSeasonCount(String seasonStr) {
        if (seasonStr == null) return 1;
        if (seasonStr.contains("2")) return 2;
        if (seasonStr.contains("3")) return 3;
        return 1;
    }

    /** 初始化玩家土地（首次访问时调用） */
    @Transactional
    public void initLandsForPlayer(Long playerId) {
        if (!landRepo.findByPlayerId(playerId).isEmpty()) return; // 已初始化

        String[][] landGrid = {
            {"黄土地","黄土地","黄土地","黄土地","黄土地","黄土地"},
            {"黑土地","黑土地","黑土地","黑土地","黑土地","黑土地"},
            {"沙土地","沙土地","沙土地","沙土地","沙土地","沙土地"},
            {"沙土地","沙土地","沙土地","沙土地","沙土地","沙土地"},
        };

        int index = 0;
        for (int row = 0; row < landGrid.length; row++) {
            for (int col = 0; col < landGrid[row].length; col++) {
                FarmLand land = new FarmLand();
                land.setPlayerId(playerId);
                land.setLandIndex(index);
                land.setLandType(landGrid[row][col]);
                landRepo.save(land);
                index++;
            }
        }
    }
}
