package cn.jxufe.seedsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_seed", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "seed_id"})
})
public class PlayerSeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "seed_id", nullable = false)
    private Integer seedId;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public PlayerSeed() {}

    public PlayerSeed(Long playerId, Integer seedId, int quantity) {
        this.playerId = playerId;
        this.seedId = seedId;
        this.quantity = quantity;
        this.createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Integer getSeedId() { return seedId; }
    public void setSeedId(Integer seedId) { this.seedId = seedId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
