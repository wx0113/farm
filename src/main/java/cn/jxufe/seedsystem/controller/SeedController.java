package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.GrowthStage;
import cn.jxufe.seedsystem.entity.Seed;
import cn.jxufe.seedsystem.service.SeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    // 查询种子列表（GET，接收 datagrid 的分页参数）
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
            result.put("rows", new java.util.ArrayList<>());
        }
        return result;
    }

    // 保存种子（POST 表单提交，不是 JSON）
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(Seed seed) {
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
    public Map<String, Object> delete(Integer id) {
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
    public Map<String, Object> queryGrowthStages(Integer seedId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<GrowthStage> stages = seedService.queryGrowthStages(seedId);
            result.put("total", stages.size());
            result.put("rows", stages);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("total", 0);
            result.put("rows", new java.util.ArrayList<>());
        }
        return result;
    }

    // 保存成长阶段
    @PostMapping("/growth/save")
    @ResponseBody
    public Map<String, Object> saveGrowthStage(GrowthStage stage) {
        Map<String, Object> result = new HashMap<>();
        try {
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
    public Map<String, Object> deleteGrowthStage(Integer id) {
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
}