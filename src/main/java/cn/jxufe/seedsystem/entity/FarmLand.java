package cn.jxufe.seedsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farm_land")
public class FarmLand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "land_index", nullable = false)
    private int landIndex;

    @Column(name = "land_type", length = 20)
    private String landType;

    @Column(name = "seed_id")
    private Integer seedId;

    @Column(name = "current_stage")
    private Integer currentStage;

    @Column(name = "current_season")
    private Integer currentSeason;

    @Column(name = "stage_start_time")
    private LocalDateTime stageStartTime;

    @Column(name = "has_worm")
    private boolean hasWorm;

    @Column(name = "withered")
    private boolean withered;

    @Column(name = "yield_reduction")
    private int yieldReduction;

    @Column(name = "plant_time")
    private LocalDateTime plantTime;

    // ---- getter/setter ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public int getLandIndex() { return landIndex; }
    public void setLandIndex(int landIndex) { this.landIndex = landIndex; }

    public String getLandType() { return landType; }
    public void setLandType(String landType) { this.landType = landType; }

    public Integer getSeedId() { return seedId; }
    public void setSeedId(Integer seedId) { this.seedId = seedId; }

    public Integer getCurrentStage() { return currentStage; }
    public void setCurrentStage(Integer currentStage) { this.currentStage = currentStage; }

    public Integer getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(Integer currentSeason) { this.currentSeason = currentSeason; }

    public LocalDateTime getStageStartTime() { return stageStartTime; }
    public void setStageStartTime(LocalDateTime stageStartTime) { this.stageStartTime = stageStartTime; }

    public boolean isHasWorm() { return hasWorm; }
    public void setHasWorm(boolean hasWorm) { this.hasWorm = hasWorm; }

    public boolean isWithered() { return withered; }
    public void setWithered(boolean withered) { this.withered = withered; }

    public int getYieldReduction() { return yieldReduction; }
    public void setYieldReduction(int yieldReduction) { this.yieldReduction = yieldReduction; }

    public LocalDateTime getPlantTime() { return plantTime; }
    public void setPlantTime(LocalDateTime plantTime) { this.plantTime = plantTime; }

    /** 是否为成熟状态 */
    public boolean isMature() {
        return seedId != null && !withered && currentStage != null && currentStage < 0;
    }

    /** 是否为空地 */
    public boolean isEmpty() { return seedId == null; }
}
