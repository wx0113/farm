# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build / Run / Test

```bash
# Build the project
mvn clean compile

# Run the application (starts on port 8080)
mvn spring-boot:run

# Run tests
mvn test

# Access the app in browser
# http://localhost:8080/seed/index.html
```

## Architecture

**Stack**: Spring Boot 3.4.2, Java 21, PostgreSQL + Alibaba Druid, JPA/Hibernate, jQuery EasyUI frontend.

The application has two Spring Boot entry points; the one that matters is `SeedsystemApplication` (`@EnableScheduling` is required for the game loop). `FarmApplication` is an older/alternate entry point without scheduling.

### Layers (top → bottom)

| Layer | Package | Pattern |
|-------|---------|---------|
| Controller | `controller/*` | `@Controller` + `@ResponseBody`, returns `Map<String,Object>` or `Message`. Most endpoints get current player from `HttpSession`. |
| Service | `service/*` | `FarmGameService` is the game engine. Others are thin CRUD wrappers. |
| Repository | `repository/*` | `JpaRepository` interfaces with Spring Data named-query methods. |
| Entity | `entity/*` | 6 JPA entities (see below). `Message` and `Result<T>` are POJO response objects, not DB tables. |

### Database (PostgreSQL `farm_db`)

Six tables. `spring.jpa.hibernate.ddl-auto=update` means schema is auto-managed in dev.

| Table | Key Fields | Notes |
|-------|-----------|-------|
| `seed` | id (manual PK), seedId, seedName, landRequirement, xSeasonCrop | ID is manually assigned (not auto-increment). `landRequirement` must match `landType` on the target plot. |
| `player` | id (auto PK), username (unique), nickname, exp, gold, points | Session-stored as `currentPlayer`. |
| `player_seed` | id (auto PK), playerId + seedId (composite unique), quantity | Player's seed inventory. |
| `growth_stage` | id (auto PK), seedId, stageOrder (1..N), stageDuration (seconds), pestProbability (0..1), imageUrl + positioning fields | Each seed has multiple stages ordered by `stageOrder`. Crop images are positioned using width/height/offsetX/offsetY relative to a 293×150 reference frame. |
| `farm_land` | id (auto PK), playerId, landIndex (0..23), landType, seedId (nullable), currentStage, currentSeason, hasWorm, withered, yieldReduction | 24 plots per player. `currentStage`: 1..N = growing, -1 = mature, null = empty. |
| (none) | `Message` / `Result<T>` | POJOs. `Message.code=0` means success; `Result` is used only by the deprecated crop-image upload endpoint. |

### Game Loop (`FarmGameService`)

- `@Scheduled(fixedRate=2000)` runs `farmTick()` every 2 seconds.
- Iterates all `farm_land` rows where `seedId IS NOT NULL`.
- For each land, `processGrowth()` compares elapsed time against the current `GrowthStage.stageDuration`. If the stage's duration has expired, it advances `currentStage` by 1. If all stages are exhausted, sets `currentStage = -1` (mature).
- When entering a new stage, rolls `Math.random() < pestProbability` to set `hasWorm = true`.
- If a worm persists across a stage transition, `yieldReduction` increases by 1–2 (random).
- Harvest formula: `actualYield = max(1, baseHarvest - yieldReduction)`. Gold reward = `actualYield × fruitPrice`.
- Multi-season crops: `parseSeasonCount()` checks `xSeasonCrop` string for "2" or "3". On `cleanLand`, if `currentSeason < totalSeasons`, the land resets to stage 1 with `currentSeason + 1` instead of being cleared.

### Land Grid

24 plots arranged as 4 rows × 6 columns, staggered (even rows have `margin-left: 73px`).

| Row | Type | Background |
|-----|------|-----------|
| 0 | 黄土地 | `/images/land.png` |
| 1 | 黑土地 | `/images/landBlack.png` |
| 2 | 金土地 | `/images/landGold.png` |
| 3 | 沙土地 | `/images/landRed.png` |

### Image Positioning System

Crop images on the farm are placed using a coordinate system:
1. **Reference frame**: The positioning tool area is 293×150 pixels.
2. `GrowthStage` stores `imageWidth/Height/OffsetX/OffsetY` relative to this reference frame.
3. At render time (`addCropIcon` in farm.html), these are scaled: `scaleX = actualLandWidth / 293`, `scaleY = actualLandHeight / 150`.
4. `SeedService.@PostConstruct` auto-converts legacy percentage values (≤100) to pixel values.

### Message Push System

`FarmGameService` maintains `pendingMessages: Map<Long, List<Map>>` keyed by player ID. The farm page polls `/farm/messages` every 2 seconds. Message types: `planted`, `stageChange`, `worm`, `mature`, `wormKilled`, `harvested`, `newSeason`, `cleaned`.

### Frontend Pages

All pages share a common header (user panel + 5 nav icons) and use jQuery EasyUI + a green color theme (`green-theme.css`).

| Page | Route | Purpose |
|------|-------|---------|
| `index.html` | `/seed/index.html` | Welcome + user switching |
| `farm.html` | `/seed/farm.html` | 24-plot interactive farm game |
| `shop.html` | `/seed/shop.html` | Seed store + inventory |
| `seed.html` | `/seed/seed.html` | Seed CRUD + growth stage editor |
| `player.html` | `/seed/player.html` | Player CRUD + avatar upload |

### Session-Based Auth

No login system. `HttpSession.getAttribute("currentPlayer")` holds the active `Player`. Switching users calls `/setCurUser` (POST, JSON body with Player). Every farm action endpoint re-fetches the player from DB to avoid stale session data: `player = playerRepo.findById(player.getId()).orElse(player)`.

### Startup Initialization (`SeedService.@PostConstruct`)

On boot, `initGrowthStages()` performs three cleanup tasks:
1. Deletes all rows where `stageOrder = 0`.
2. Creates a "枯萎" (withered) stage for any seed missing one, with `cropStatus='枯萎'` and `imageUrl='/images/crops/basic/9.png'`.
3. Converts legacy percentage values (≤100) in positioning fields to pixel values.

### Key Patterns & Gotchas

- **Seed ID is manual**: `Seed.id` does not auto-increment. `saveSeed()` queries `MAX(id)` and adds 1 for new seeds.
- **Stage 1 image override**: `SeedController.saveGrowthStage()` forces `imageUrl = /images/crops/basic/0.png` when `stageOrder == 1`.
- **Object-fit calculations**: `land.png` files are 201×102 pixels. Inside a 146×146 square `land-plot`, `object-fit:contain` causes vertical letterboxing (~36px top/bottom). The `addCropIcon` function compensates for this.
- **Timer resilience**: `farmTick()` wraps the per-land processing loop in try-catch to prevent a single bad land from stopping the entire scheduler.
- **File uploads**: `FileSaver` writes to `src/main/resources/static/` so uploaded files are immediately accessible via URL. Uses a temp-file-then-move pattern to avoid Windows file-locking issues.
