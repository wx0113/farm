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

        // 计算非枯萎阶段的最大数量（枯萎阶段不应作为生长阶段的一部分）
        int maxNormalStages = 0;
        for (GrowthStage s : stages) {
            if (!"枯萎".equals(s.getCropStatus())) {
                maxNormalStages++;
            }
        }
        if (maxNormalStages == 0) return;

        int maxStages = stages.size();
        int currentStageOrder = land.getCurrentStage();

        // 已成熟
        if (currentStageOrder < 0) return;

        GrowthStage currentGs = null;
        if (currentStageOrder >= 1 && currentStageOrder <= maxStages) {
            currentGs = stages.get(currentStageOrder - 1);
        }

        // 如果当前是枯萎阶段，直接推进到成熟
        if (currentGs != null && "枯萎".equals(currentGs.getCropStatus())) {
            land.setCurrentStage(-1);
            land.setStageStartTime(LocalDateTime.now());
            land.setHasWorm(false);
            landRepo.save(land);
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "mature");
            msg.put("landIndex", land.getLandIndex());
            pushMessage(land.getPlayerId(), msg);
            return;
        }

        if (currentGs == null) {
            // 检查是否应该进入成熟
            if (currentStageOrder > maxNormalStages) {
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
            // 如果本阶段有虫未除，减产（虫不消失，持续到下一阶段）
            if (land.isHasWorm()) {
                int reduction = 1 + (int)(Math.random() * 2); // 1-2
                land.setYieldReduction(land.getYieldReduction() + reduction);
            }

            // 进入下一阶段
            int nextStage = currentStageOrder + 1;
            if (nextStage > maxNormalStages) {
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
                // 进入下一生长阶段，跳过枯萎阶段直接成熟
                GrowthStage nextGs = stages.get(nextStage - 1);
                if ("枯萎".equals(nextGs.getCropStatus())) {
                    // 跳过枯萎阶段，直接成熟
                    land.setCurrentStage(-1);
                    land.setStageStartTime(LocalDateTime.now());
                    land.setHasWorm(false);
                    landRepo.save(land);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("type", "mature");
                    msg.put("landIndex", land.getLandIndex());
                    pushMessage(land.getPlayerId(), msg);
                    return;
                }
                land.setCurrentStage(nextStage);
                land.setStageStartTime(LocalDateTime.now());

                // 生虫概率判断（已有虫则不新增）
                if (!land.isHasWorm()) {
                    BigDecimal pestProb = nextGs.getPestProbability();
                    if (pestProb != null && Math.random() < pestProb.doubleValue()) {
                        land.setHasWorm(true);
                        landRepo.save(land);
                        Map<String, Object> msg = new HashMap<>();
                        msg.put("type", "worm");
                        msg.put("landIndex", land.getLandIndex());
                        pushMessage(land.getPlayerId(), msg);
                        return;
                    }
                }
                landRepo.save(land);
                Map<String, Object> msg = new HashMap<>();
                msg.put("type", "stageChange");
                msg.put("landIndex", land.getLandIndex());
                msg.put("stage", nextStage);
                msg.put("imageUrl", nextGs.getImageUrl());
                pushMessage(land.getPlayerId(), msg);
            }
        }
    }

    // ======================== 播种 ========================
    @Transactional
    public Message actionPlant(int landIndex, int seedId, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");
        // 刷新为数据库最新数据，防止 session 过期
        player = playerRepo.findById(player.getId()).orElse(player);

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
        player = playerRepo.findById(player.getId()).orElse(player);

        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) return new Message(-2, "土地不存在");
        if (land.isEmpty()) return new Message(-2, "土地上没有作物");
        if (!land.isHasWorm()) return new Message(-3, "该作物没有虫害");

        // 除虫：减少产量
        land.setHasWorm(false);
        land.setYieldReduction(land.getYieldReduction() + 1);
        landRepo.save(land);

        // 奖励
        player.setExp(player.getExp() + 2);
        player.setPoints(player.getPoints() + 2);
        playerRepo.save(player);
        session.setAttribute("currentPlayer", player);

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "wormKilled");
        msg.put("landIndex", landIndex);
        pushMessage(player.getId(), msg);

        return new Message(0, "除虫成功！经验+2 积分+2（产量-1）");
    }

    // ======================== 收获 ========================
    @Transactional
    public Message actionHarvest(int landIndex, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new Message(-1, "请先登录");
        player = playerRepo.findById(player.getId()).orElse(player);

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
        player = playerRepo.findById(player.getId()).orElse(player);

        FarmLand land = landRepo.findByPlayerIdAndLandIndex(player.getId(), landIndex);
        if (land == null) return new Message(-2, "土地不存在");
        if (!land.isWithered()) return new Message(-2, "该土地没有枯草需要清除");

        Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);

        // 检查是否为多季作物
        int totalSeasons = parseSeasonCount(seed != null ? seed.getXSeasonCrop() : null);
        int currentSeason = land.getCurrentSeason() != null ? land.getCurrentSeason() : 1;

        if (seed != null && currentSeason < totalSeasons) {
            // 还有下一季：回到种子阶段
            land.setCurrentSeason(currentSeason + 1);
            land.setCurrentStage(1);
            land.setStageStartTime(LocalDateTime.now());
            land.setHasWorm(false);
            land.setWithered(false);
            land.setYieldReduction(0);
            landRepo.save(land);

            // 第一阶段生虫概率
            GrowthStage firstGs = growthStageRepo.findBySeedIdAndStageOrder(seed.getId(), 1).orElse(null);
            if (firstGs != null && firstGs.getPestProbability() != null
                    && Math.random() < firstGs.getPestProbability().doubleValue()) {
                land.setHasWorm(true);
                landRepo.save(land);
                Map<String, Object> wm = new HashMap<>();
                wm.put("type", "worm");
                wm.put("landIndex", landIndex);
                pushMessage(player.getId(), wm);
            }

            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "newSeason");
            msg.put("landIndex", landIndex);
            msg.put("currentSeason", land.getCurrentSeason());
            pushMessage(player.getId(), msg);

            return new Message(0, "进入第" + land.getCurrentSeason() + "季！经验+5 积分+5");
        }

        // 最后一季或单季：清空土地
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
        state.put("yieldReduction", land.getYieldReduction());

        if (land.getSeedId() != null) {
            Seed seed = seedRepo.findById(land.getSeedId()).orElse(null);
            if (seed != null) {
                state.put("totalSeasons", parseSeasonCount(seed.getXSeasonCrop()));
                state.put("baseHarvest", seed.getHarvestCount() != null ? seed.getHarvestCount() : 1);
            }
            if (seed != null) {
                state.put("seedName", seed.getSeedName());

                // 确定要查询的 GrowthStage stageOrder
                Integer lookupStage = null;
                if (land.getCurrentStage() != null) {
                    if (land.getCurrentStage() >= 1) {
                        lookupStage = land.getCurrentStage();
                    }
                    // 成熟阶段 (currentStage == -1) 不设 lookupStage，
                    // 改为取最后一个非枯萎阶段的图片数据（见下方）
                }
                // 枯萎也查（保留当前阶段的偏移值）
                if (land.isWithered() && land.getCurrentStage() != null && land.getCurrentStage() >= 1) {
                    lookupStage = land.getCurrentStage();
                }

                // 查询 GrowthStage 获取偏移值和 imageUrl
                GrowthStage gs = null;
                if (land.isWithered()) {
                    // 枯萎：优先查 cropStatus='枯萎' 的专属阶段，其次 fallback 当前阶段
                    gs = growthStageRepo.findBySeedIdAndCropStatus(seed.getId(), "枯萎").orElse(null);
                    if (gs == null && lookupStage != null) {
                        gs = growthStageRepo.findBySeedIdAndStageOrder(seed.getId(), lookupStage).orElse(null);
                    }
                } else if (land.getCurrentStage() != null && land.getCurrentStage() == -1) {
                    // 成熟（未枯萎）：取最后一个非枯萎阶段的图片和偏移数据，
                    // 避免硬编码 stageOrder 与自动创建的枯萎阶段冲突
                    List<GrowthStage> allStages = growthStageRepo.findBySeedIdOrderByStageOrder(seed.getId());
                    for (int i = allStages.size() - 1; i >= 0; i--) {
                        GrowthStage s = allStages.get(i);
                        if (!"枯萎".equals(s.getCropStatus())) {
                            gs = s;
                            break;
                        }
                    }
                } else if (lookupStage != null) {
                    gs = growthStageRepo.findBySeedIdAndStageOrder(seed.getId(), lookupStage).orElse(null);
                }

                // 状态
                String status;
                if (land.isWithered()) {
                    status = "withered";
                } else if (land.getCurrentStage() != null && land.getCurrentStage() == -1) {
                    status = "mature";
                } else {
                    status = land.isHasWorm() ? "worm" : "growing";
                }
                state.put("status", status);

                // imageUrl + 偏移值
                // 枯萎阶段：始终使用固定枯草图片 + 默认居中定位（防止无 GrowthStage 时图片飞走）
                if (land.isWithered()) {
                    state.put("imageUrl", "/images/crops/basic/9.png");
                    state.put("imageWidth", gs != null ? gs.getImageWidth() : 200);
                    state.put("imageHeight", gs != null ? gs.getImageHeight() : 100);
                    state.put("imageOffsetX", gs != null ? gs.getImageOffsetX() : 47);
                    state.put("imageOffsetY", gs != null ? gs.getImageOffsetY() : 25);
                } else if (land.getCurrentStage() != null && land.getCurrentStage() == 1) {
                    state.put("imageUrl", "/images/crops/basic/0.png");
                    if (gs != null) {
                        state.put("imageWidth", gs.getImageWidth());
                        state.put("imageHeight", gs.getImageHeight());
                        state.put("imageOffsetX", gs.getImageOffsetX());
                        state.put("imageOffsetY", gs.getImageOffsetY());
                    }
                } else if (gs != null && gs.getImageUrl() != null && !gs.getImageUrl().isEmpty()) {
                    state.put("imageUrl", gs.getImageUrl());
                    state.put("imageWidth", gs.getImageWidth());
                    state.put("imageHeight", gs.getImageHeight());
                    state.put("imageOffsetX", gs.getImageOffsetX());
                    state.put("imageOffsetY", gs.getImageOffsetY());
                } else {
                    // fallback：硬编码路径（无偏移值）
                    String fallbackUrl = null;
                    if (land.isWithered()) {
                        fallbackUrl = "/images/crops/basic/9.png";
                    } else if (land.getCurrentStage() != null && land.getCurrentStage() == -1) {
                        // 成熟：用最后一个正常阶段的图片（如 /images/crops/{id}/5.png）
                        int lastNormalOrder = findLastNormalStageOrder(seed.getId());
                        fallbackUrl = "/images/crops/" + seed.getId() + "/" + lastNormalOrder + ".png";
                    } else if (land.getCurrentStage() != null && land.getCurrentStage() == 1) {
                        fallbackUrl = "/images/crops/basic/0.png";
                    } else if (land.getCurrentStage() != null) {
                        fallbackUrl = "/images/crops/" + seed.getId() + "/" + land.getCurrentStage() + ".png";
                    }
                    if (fallbackUrl != null) {
                        state.put("imageUrl", fallbackUrl);
                    }
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

    /** 查找种子最后一个非枯萎阶段的 stageOrder，用于成熟图片 fallback */
    private int findLastNormalStageOrder(Integer seedId) {
        if (seedId == null) return 5;
        List<GrowthStage> stages = growthStageRepo.findBySeedIdOrderByStageOrder(seedId);
        int lastOrder = 5; // 默认值
        for (GrowthStage s : stages) {
            if (!"枯萎".equals(s.getCropStatus()) && s.getStageOrder() != null) {
                lastOrder = s.getStageOrder();
            }
        }
        return lastOrder;
    }

    /** 初始化/修正玩家土地类型（首次访问时创建，后续访问时自动修正变更的土地类型） */
    @Transactional
    public void initLandsForPlayer(Long playerId) {
        List<FarmLand> existing = landRepo.findByPlayerId(playerId);

        String[][] landGrid = {
            {"黄土地","黄土地","黄土地","黄土地","黄土地","黄土地"},
            {"黑土地","黑土地","黑土地","黑土地","黑土地","黑土地"},
            {"金土地","金土地","金土地","金土地","金土地","金土地"},
            {"沙土地","沙土地","沙土地","沙土地","沙土地","沙土地"},
        };

        if (existing.isEmpty()) {
            // 首次访问：创建全部 24 块土地
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
        } else {
            // 已初始化：检查并修正与当前网格定义不一致的土地类型
            for (FarmLand land : existing) {
                int row = land.getLandIndex() / 6;
                int col = land.getLandIndex() % 6;
                if (row >= 0 && row < landGrid.length && col >= 0 && col < landGrid[row].length) {
                    String expectedType = landGrid[row][col];
                    if (!expectedType.equals(land.getLandType())) {
                        land.setLandType(expectedType);
                        landRepo.save(land);
                    }
                }
            }
        }
    }
}
