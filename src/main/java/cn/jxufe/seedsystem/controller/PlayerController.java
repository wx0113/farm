package cn.jxufe.seedsystem.controller;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.entity.Player;
import cn.jxufe.seedsystem.service.FarmService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    @Autowired
    private FarmService farmService;

    // 返回所有玩家列表
    @GetMapping("/player/list")
    @ResponseBody
    public List<Player> getPlayerList() {
        List<Player> players = farmService.findAllPlayers();
        log.info("查询到玩家数量：{}", players.size());
        return players;
    }

    // 根据昵称获取玩家
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

    // 获取当前 session 中的玩家
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

    // 设置当前玩家到 session
    @RequestMapping(value = "/setCurUser", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message setCurUser(HttpSession session, @RequestBody Player player) {
        log.info("设置当前玩家，昵称：{}", player.getNickname());
        return farmService.setCurUser(session, player);
    }
}