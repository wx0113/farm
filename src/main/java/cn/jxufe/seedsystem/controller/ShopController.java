package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.entity.Player;
import cn.jxufe.seedsystem.entity.PlayerSeed;
import cn.jxufe.seedsystem.entity.Seed;
import cn.jxufe.seedsystem.repository.PlayerRepository;
import cn.jxufe.seedsystem.repository.PlayerSeedRepository;
import cn.jxufe.seedsystem.repository.SeedRepository;
import cn.jxufe.seedsystem.service.SeedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private SeedService seedService;

    @Autowired
    private SeedRepository seedRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerSeedRepository playerSeedRepository;

    @GetMapping("/seeds")
    @ResponseBody
    public Map<String, Object> seeds(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "4") int rows) {
        Map<String, Object> result = new HashMap<>();
        List<Seed> seeds = seedService.querySeeds(null, page, rows);
        long total = seedService.countSeeds(null);

        List<Map<String, Object>> seedList = new ArrayList<>();
        for (Seed s : seeds) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("seedName", s.getSeedName());
            item.put("tipInfo", s.getTipInfo());
            item.put("purchasePrice", s.getPurchasePrice());
            item.put("matureImage", "/images/crops/" + s.getId() + "/5.png");
            seedList.add(item);
        }

        result.put("total", total);
        result.put("rows", seedList);
        return result;
    }

    @GetMapping("/mySeeds")
    @ResponseBody
    public List<Map<String, Object>> mySeeds(HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) return new ArrayList<>();

        List<PlayerSeed> owned = playerSeedRepository.findByPlayerId(player.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlayerSeed ps : owned) {
            Map<String, Object> item = new HashMap<>();
            item.put("seedId", ps.getSeedId());
            item.put("quantity", ps.getQuantity());
            item.put("image", "/images/crops/" + ps.getSeedId() + "/5.png");
            // 查询种子名称
            seedRepository.findById(ps.getSeedId()).ifPresent(seed -> {
                item.put("seedName", seed.getSeedName());
                item.put("tipInfo", seed.getTipInfo());
            });
            result.add(item);
        }
        return result;
    }

    @PostMapping("/buy")
    @ResponseBody
    public Message buy(@RequestParam Integer seedId, HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player == null) {
            return new Message(401, "请先登录后再购买");
        }

        Optional<Seed> opt = seedRepository.findById(seedId);
        if (opt.isEmpty()) {
            return new Message(404, "种子不存在");
        }

        Seed seed = opt.get();
        int price = seed.getPurchasePrice().intValue();

        if (player.getGold() < price) {
            return new Message(400, "金币不足，需要 " + price + " 金币，当前拥有 " + player.getGold() + " 金币");
        }

        // 扣金币
        player.setGold(player.getGold() - price);
        playerRepository.save(player);
        session.setAttribute("currentPlayer", player);

        // 加库存
        PlayerSeed ps = playerSeedRepository.findByPlayerIdAndSeedId(player.getId(), seedId);
        if (ps != null) {
            ps.setQuantity(ps.getQuantity() + 1);
        } else {
            ps = new PlayerSeed(player.getId(), seedId, 1);
        }
        playerSeedRepository.save(ps);

        return new Message(200, "购买成功", player.getGold());
    }

    @GetMapping("/playerGold")
    @ResponseBody
    public int playerGold(HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        return player != null ? player.getGold() : 0;
    }
}
