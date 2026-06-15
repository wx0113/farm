package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.GrowthStage;
import cn.jxufe.seedsystem.entity.Result;
import cn.jxufe.seedsystem.entity.Seed;
import cn.jxufe.seedsystem.service.SeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seed")
public class SeedController {

    @Autowired
    private SeedService seedService;

    // 种子管理主页面
    @GetMapping("/list")
    public String list() {
        return "farm/seedList";
    }

    // 查询种子列表（支持分页+按名称模糊查询）
    @GetMapping("/query")
    @ResponseBody
    public Map<String, Object> query(
            @RequestParam(required = false) String seedName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Seed> seeds = seedService.querySeeds(seedName, page, rows);
            long total = seedService.countSeeds(seedName);
            result.put("total", total);
            result.put("rows", seeds);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("total", 0);
            result.put("rows", List.of());
        }
        return result;
    }

    // 按种子ID或名称搜索
    @GetMapping("/search")
    @ResponseBody
    public List<Seed> search(@RequestParam(required = false) Integer id,
                             @RequestParam(required = false) String name) {
        try {
            if (name != null && !name.trim().isEmpty()) {
                return seedService.searchByName(name.trim());
            }
            if (id != null) {
                return seedService.searchById(id);
            }
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // 保存种子（接收JSON格式）
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(@RequestBody Seed seed) {
        Map<String, Object> result = new HashMap<>();
        try {
            seedService.saveSeed(seed);
            result.put("code", 0);
            result.put("msg", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "保存失败：" + e.getMessage());
        }
        return result;
    }

    // 删除种子
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            seedService.deleteSeed(id);
            result.put("code", 0);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // 查询成长阶段列表
    @GetMapping("/growth/list")
    @ResponseBody
    public List<GrowthStage> queryGrowthStages(@RequestParam Integer seedId) {
        try {
            return seedService.queryGrowthStages(seedId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // 保存成长阶段（接收JSON格式）
    @PostMapping("/growth/save")
    @ResponseBody
    public Map<String, Object> saveGrowthStage(@RequestBody GrowthStage stage) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 特殊阶段强制固定图片路径，不受前端提交值影响
            if ("枯萎".equals(stage.getCropStatus())) {
                stage.setImageUrl("/images/crops/basic/9.png");
            } else if (stage.getStageOrder() != null && stage.getStageOrder() == 1) {
                stage.setImageUrl("/images/crops/basic/0.png");
            } else if (stage.getImageUrl() == null || stage.getImageUrl().isEmpty()) {
                // 普通阶段 imageUrl 为空时按种子ID和阶段序号补全
                if (stage.getSeedId() != null && stage.getStageOrder() != null) {
                    stage.setImageUrl("/images/crops/" + stage.getSeedId() + "/" + stage.getStageOrder() + ".png");
                }
            }
            seedService.saveGrowthStage(stage);
            result.put("code", 0);
            result.put("msg", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "保存失败：" + e.getMessage());
        }
        return result;
    }

    // 删除成长阶段
    @PostMapping("/growth/delete")
    @ResponseBody
    public Map<String, Object> deleteGrowthStage(@RequestParam Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            seedService.deleteGrowthStage(id);
            result.put("code", 0);
            result.put("msg", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @Deprecated
    @PostMapping("/growth/cropImage")
    @ResponseBody
    public Result<String> cropImage(@RequestParam("image") MultipartFile file,
                                    @RequestParam(value = "oldPath", required = false) String oldPath) {
        try {
            String path = System.getProperty("user.dir") + "/src/main/resources/static/crops/";
            java.io.File dir = new java.io.File(path);
            if (!dir.exists()) dir.mkdirs();

            // 覆盖：删除旧文件
            if (oldPath != null && !oldPath.isEmpty()) {
                java.io.File oldFile = new java.io.File(path + oldPath.replace("/crops/", ""));
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String fileName = java.util.UUID.randomUUID() + ".png";
            file.transferTo(new java.io.File(dir, fileName));

            return Result.success("/crops/" + fileName);
        } catch (Exception e) {
            return Result.error("裁剪图片保存失败：" + e.getMessage());
        }
    }
}