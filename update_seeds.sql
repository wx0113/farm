-- ============================================
-- 种子数据更新：名称 + 属性 + 小贴士
-- ============================================

-- 1. 草莓 (水果，1季，黄土地)
UPDATE seed SET
    seed_name = '草莓',
    seed_type = '水果',
    x_season_crop = '1季作物',
    seed_level = 2,
    land_requirement = '黄土地',
    experience = 20,
    maturity_time = 300,
    harvest_count = 5,
    purchase_price = 50.00,
    fruit_price = 15.00,
    points = 10,
    tip_info = '🍓 草莓是新手最爱的水果！红润多汁，种在黄土地上生长飞快。成熟后每颗可卖15金币，是前期攒钱的不二之选。'
WHERE id = 1;

-- 6. 茄子 (蔬菜，2季，黑土地)
UPDATE seed SET
    seed_name = '茄子',
    seed_type = '蔬菜',
    x_season_crop = '2季作物',
    seed_level = 3,
    land_requirement = '黑土地',
    experience = 30,
    maturity_time = 400,
    harvest_count = 4,
    purchase_price = 80.00,
    fruit_price = 25.00,
    points = 15,
    tip_info = '🍆 紫皮长茄，黑土地上的明星作物！两季收获，果实饱满。记得及时除虫，否则产量会大打折扣哦~'
WHERE id = 6;

-- 7. 西红柿 (蔬菜，2季，黄土地)
UPDATE seed SET
    seed_name = '西红柿',
    seed_type = '蔬菜',
    x_season_crop = '2季作物',
    seed_level = 3,
    land_requirement = '黄土地',
    experience = 25,
    maturity_time = 350,
    harvest_count = 6,
    purchase_price = 70.00,
    fruit_price = 20.00,
    points = 12,
    tip_info = '🍅 红彤彤的西红柿，酸甜可口！两季作物产量高，黄土地就能种。果实单价虽不高，但胜在数量多，薄利多销。'
WHERE id = 7;

-- 8. 豌豆 (蔬菜，1季，黄土地)
UPDATE seed SET
    seed_name = '豌豆',
    seed_type = '蔬菜',
    x_season_crop = '1季作物',
    seed_level = 2,
    land_requirement = '黄土地',
    experience = 18,
    maturity_time = 280,
    harvest_count = 5,
    purchase_price = 45.00,
    fruit_price = 12.00,
    points = 8,
    tip_info = '🫛 翠绿的豌豆，小巧玲珑。生长周期短，成熟快，适合新手练手。虽然单价不高，但生长速度能让你快速积累经验。'
WHERE id = 8;

-- 9. 辣椒 (蔬菜，2季，沙土地)
UPDATE seed SET
    seed_name = '辣椒',
    seed_type = '蔬菜',
    x_season_crop = '2季作物',
    seed_level = 4,
    land_requirement = '沙土地',
    experience = 35,
    maturity_time = 380,
    harvest_count = 4,
    purchase_price = 90.00,
    fruit_price = 30.00,
    points = 18,
    tip_info = '🌶️ 火辣辣的小辣椒！只长在沙土地上，两季收获。成熟后每个能卖30金币，利润可观。注意虫害概率较高，勤除虫哦！'
WHERE id = 9;

-- 13. 葡萄 (水果，2季，沙土地)
UPDATE seed SET
    seed_name = '葡萄',
    seed_type = '水果',
    x_season_crop = '2季作物',
    seed_level = 5,
    land_requirement = '沙土地',
    experience = 45,
    maturity_time = 450,
    harvest_count = 3,
    purchase_price = 120.00,
    fruit_price = 50.00,
    points = 25,
    tip_info = '🍇 晶莹剔透的葡萄串，沙土地上的贵族水果。果实单价高达50金币，两季收入可观。耐心等待成熟，回报丰厚！'
WHERE id = 13;

-- 14. 西瓜 (水果，1季，黄土地)
UPDATE seed SET
    seed_name = '西瓜',
    seed_type = '水果',
    x_season_crop = '1季作物',
    seed_level = 4,
    land_requirement = '黄土地',
    experience = 40,
    maturity_time = 500,
    harvest_count = 2,
    purchase_price = 100.00,
    fruit_price = 60.00,
    points = 20,
    tip_info = '🍉 夏天必备的大西瓜！生长周期较长但果实巨大，每个卖60金币。虽然单季单次收获量不多，但单价高适合长期投资。'
WHERE id = 14;

-- 30. 星星果 (水果，3季，黑土地)
UPDATE seed SET
    seed_name = '星星果',
    seed_type = '水果',
    x_season_crop = '3季作物',
    seed_level = 8,
    land_requirement = '黑土地',
    experience = 60,
    maturity_time = 600,
    harvest_count = 3,
    purchase_price = 250.00,
    fruit_price = 100.00,
    points = 35,
    tip_info = '⭐ 传说中的星星果！黑土地专属高级作物，三季收获让你一次投入长期受益。果实价值100金币，是中后期发家致富的利器！'
WHERE id = 30;

-- 32. 钻石果 (水果，3季，沙土地)
UPDATE seed SET
    seed_name = '钻石果',
    seed_type = '水果',
    x_season_crop = '3季作物',
    seed_level = 10,
    land_requirement = '沙土地',
    experience = 80,
    maturity_time = 700,
    harvest_count = 1,
    purchase_price = 500.00,
    fruit_price = 300.00,
    points = 50,
    tip_info = '💎 农场中最珍贵的钻石果！沙土地顶级作物，三季生长，每季结出一颗价值300金币的果实。投资大但回报惊人，适合老玩家挑战！'
WHERE id = 32;

-- 418. 玉米 (谷物，1季，黄土地)
UPDATE seed SET
    seed_name = '玉米',
    seed_type = '谷物',
    x_season_crop = '1季作物',
    seed_level = 1,
    land_requirement = '黄土地',
    experience = 10,
    maturity_time = 200,
    harvest_count = 6,
    purchase_price = 30.00,
    fruit_price = 8.00,
    points = 5,
    tip_info = '🌽 金灿灿的玉米棒子！最基础的谷物作物，黄土地上快速生长。产量多、周期短，是新手熟悉农场操作的最佳选择。'
WHERE id = 418;

-- 933. 白萝卜 (蔬菜，1季，黄土地)
UPDATE seed SET
    seed_name = '白萝卜',
    seed_type = '蔬菜',
    x_season_crop = '1季作物',
    seed_level = 1,
    land_requirement = '黄土地',
    experience = 8,
    maturity_time = 180,
    harvest_count = 8,
    purchase_price = 25.00,
    fruit_price = 6.00,
    points = 4,
    tip_info = '🥬 白胖胖的大萝卜！生长速度极快，180秒就能成熟，一次收获8个。虽然单价低，但胜在薄利多收，适合快速刷经验。'
WHERE id = 933;

-- 940. 土豆 (蔬菜，1季，黄土地)
UPDATE seed SET
    seed_name = '土豆',
    seed_type = '蔬菜',
    x_season_crop = '1季作物',
    seed_level = 1,
    land_requirement = '黄土地',
    experience = 12,
    maturity_time = 220,
    harvest_count = 7,
    purchase_price = 35.00,
    fruit_price = 10.00,
    points = 6,
    tip_info = '🥔 朴实无华的土豆！黄土地基础作物，生长稳健产量不错。每个能卖10金币，是新手起步期稳定收入的保障。'
WHERE id = 940;

-- ============================================
-- 验证更新结果
-- ============================================
-- SELECT id, seed_name, seed_type, x_season_crop, land_requirement, tip_info FROM seed ORDER BY id;
