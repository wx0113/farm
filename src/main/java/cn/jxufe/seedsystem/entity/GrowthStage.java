package cn.jxufe.seedsystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "growth_stage")
@Data
public class GrowthStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "seed_id")
    private Integer seedId;

    @Column(name = "stage_order")
    private Integer stageOrder;

    @Column(name = "stage_title")
    private String stageTitle;

    @Column(name = "stage_duration")
    private Integer stageDuration;

    @Column(name = "pest_probability")
    private BigDecimal pestProbability;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "image_offset_x")
    private Integer imageOffsetX;

    @Column(name = "image_offset_y")
    private Integer imageOffsetY;

    @Column(name = "crop_status")
    private String cropStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}