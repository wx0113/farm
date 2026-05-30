package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.*;
import cn.jxufe.seedsystem.repository.*;
import cn.jxufe.seedsystem.service.FarmGameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/farm")
public class FarmController {

    @Autowired private SeedRepository seedRepository;
    @Autowired private PlayerSeedRepository playerSeedRepository;
    @Autowired private FarmGameService farmService;

    /** 获取当前玩家拥有的种子列表（含土地需求信息） */
    @GetMapping("/mySeeds")
    @ResponseBody
    public List<Map<String, Object>> mySeeds(HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new ArrayList<>();

        List<PlayerSeed> owned = playerSeedRepository.findByPlayerId(player.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlayerSeed ps : owned) {
            if (ps.getQuantity() <= 0) continue;
            Optional<Seed> opt = seedRepository.findById(ps.getSeedId());
            if (opt.isEmpty()) continue;

            Seed seed = opt.get();
            Map<String, Object> item = new HashMap<>();
            item.put("seedId", seed.getId());
            item.put("seedName", seed.getSeedName());
            item.put("landRequirement", seed.getLandRequirement());
            item.put("quantity", ps.getQuantity());
            item.put("matureImage", "/images/crops/" + seed.getId() + "/5.png");
            item.put("tipInfo", seed.getTipInfo());
            result.add(item);
        }
        return result;
    }

    /** 获取当前玩家所有土地状态 */
    @GetMapping("/landStates")
    @ResponseBody
    public Map<String, Object> landStates(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) {
            result.put("code", -1);
            result.put("msg", "未登录");
            return result;
        }
        // 首次访问初始化
        farmService.initLandsForPlayer(player.getId());
        result.put("code", 0);
        result.put("lands", farmService.getLandStates(player.getId()));
        return result;
    }

    /** 获取待推送消息 */
    @GetMapping("/messages")
    @ResponseBody
    public List<Map<String, Object>> messages(HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new ArrayList<>();
        return farmService.fetchMessages(player.getId());
    }

    /** 播种 */
    @PostMapping("/plant")
    @ResponseBody
    public Map<String, Object> plant(@RequestParam int landIndex,
                                     @RequestParam int seedId,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 先确保土地已初始化
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player != null) farmService.initLandsForPlayer(player.getId());

        Message msg = farmService.actionPlant(landIndex, seedId, session);
        result.put("code", msg.getCode());
        result.put("msg", msg.getMsg());
        return result;
    }

    /** 除虫 */
    @PostMapping("/killWorm")
    @ResponseBody
    public Map<String, Object> killWorm(@RequestParam int landIndex, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Message msg = farmService.actionKillWorm(landIndex, session);
        result.put("code", msg.getCode());
        result.put("msg", msg.getMsg());
        return result;
    }

    /** 收获 */
    @PostMapping("/harvest")
    @ResponseBody
    public Map<String, Object> harvest(@RequestParam int landIndex, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Message msg = farmService.actionHarvest(landIndex, session);
        result.put("code", msg.getCode());
        result.put("msg", msg.getMsg());
        return result;
    }

    /** 除枯草 */
    @PostMapping("/cleanLand")
    @ResponseBody
    public Map<String, Object> cleanLand(@RequestParam int landIndex, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Message msg = farmService.actionCleanLand(landIndex, session);
        result.put("code", msg.getCode());
        result.put("msg", msg.getMsg());
        return result;
    }
}
