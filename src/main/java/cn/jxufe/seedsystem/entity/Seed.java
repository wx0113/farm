package cn.jxufe.seedsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "seed")
@Data
public class Seed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "seed_id")
    private String seedId;

    @Column(name = "seed_name")
    private String seedName;

    @Column(name = "x_season_crop")
    private String xSeasonCrop;

    @Column(name = "seed_level")
    private Integer seedLevel;

    @Column(name = "seed_type")
    private String seedType;

    @Column(name = "experience")
    private Integer experience;

    @Column(name = "maturity_time")
    private Integer maturityTime;

    @Column(name = "harvest_count")
    private Integer harvestCount;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Column(name = "fruit_price")
    private BigDecimal fruitPrice;

    @Column(name = "land_requirement")
    private String landRequirement;

    @Column(name = "points")
    private Integer points;

    @Column(name = "tip_info")
    private String tipInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
