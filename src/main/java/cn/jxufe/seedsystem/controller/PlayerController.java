package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.entity.Player;
import cn.jxufe.seedsystem.service.FarmService;
import cn.jxufe.seedsystem.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    @Autowired
    private FarmService farmService;

    @Autowired
    private PlayerService playerService;

    // ==================== 原有接口（供首页用户面板/切换用户使用）====================

    @GetMapping("/player/list")
    @ResponseBody
    public List<Player> getPlayerList() {
        List<Player> players = farmService.findAllPlayers();
        log.info("查询到玩家数量：{}", players.size());
        return players;
    }

    @GetMapping("/player/findByNickname")
    @ResponseBody
    public Player findByNickname(@RequestParam("nickname") String nickname) {
        log.info("查询昵称：{}", nickname);
        return farmService.findAllPlayers()
                .stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/player/current")
    @ResponseBody
    public Message getCurrentPlayer(HttpSession session) {
        Player player = (Player) session.getAttribute("currentPlayer");
        if (player != null) {
            log.info("当前session玩家：{}", player.getNickname());
            return new Message(0, "获取成功", player);
        } else {
            log.info("session中没有玩家信息");
            return new Message(1, "未设置当前玩家");
        }
    }

    @RequestMapping(value = "/setCurUser", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message setCurUser(HttpSession session, @RequestBody Player player) {
        log.info("设置当前玩家，昵称：{}", player.getNickname());
        return farmService.setCurUser(session, player);
    }

    // ==================== 新增接口（供用户管理页面 player.html 使用）====================

    @RequestMapping(value = "/player/query", produces = "application/json")
    @ResponseBody
    public Map<String, Object> query(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int rows,
                                      String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Player> players = playerService.findByPage(page, rows, username);
            long total = playerService.count(username);
            result.put("total", total);
            result.put("rows", players);
        } catch (Exception e) {
            result.put("total", 0);
            result.put("rows", new ArrayList<>());
        }
        return result;
    }

    @RequestMapping(value = "/player/save", produces = "application/json")
    @ResponseBody
    public Message save(@RequestBody Player player) {
        if (player.getUsername() == null || player.getUsername().trim().isEmpty()) {
            return new Message(400, "用户名不能为空");
        }
        try {
            playerService.saveOrUpdate(player);
            return new Message(200, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(500, "保存失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/player/update", produces = "application/json")
    @ResponseBody
    public Message update(@RequestBody Map<String, Object> updates) {
        try {
            Object idObj = updates.get("id");
            if (idObj == null) {
                return new Message(400, "缺少 id 参数");
            }
            Long id = Long.valueOf(idObj.toString());

            Player player = playerService.getById(id);
            if (player == null) {
                return new Message(404, "玩家不存在");
            }

            if (updates.containsKey("username")) {
                player.setUsername((String) updates.get("username"));
            }
            if (updates.containsKey("nickname")) {
                player.setNickname((String) updates.get("nickname"));
            }
            if (updates.containsKey("exp")) {
                player.setExp(Integer.parseInt(updates.get("exp").toString()));
            }
            if (updates.containsKey("points")) {
                player.setPoints(Integer.parseInt(updates.get("points").toString()));
            }
            if (updates.containsKey("gold")) {
                player.setGold(Integer.parseInt(updates.get("gold").toString()));
            }

            int result = playerService.update(player);
            return new Message(200, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(500, "更新失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/player/delete", produces = "application/json")
    @ResponseBody
    public Message delete(@RequestParam("id") Long id) {
        try {
            playerService.delete(id);
            return new Message(200, "删除成功");
        } catch (Exception e) {
            return new Message(500, "删除失败：" + e.getMessage());
        }
    }

    @RequestMapping("player/test")
    @ResponseBody
    public int test() {
        return playerService.testCount();
    }

    @RequestMapping(value = "/player/updateAvatar", produces = "application/json")
    @ResponseBody
    public Message updateAvatar(@RequestParam String username, @RequestParam String avatarUrl) {
        try {
            playerService.updateAvatarUrl(username, avatarUrl);
            return new Message(200, "头像更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(500, "头像更新失败：" + e.getMessage());
        }
    }
}
