package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.entity.Player;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface FarmService {
    List<Player> findAllPlayers();
    Message setCurUser(HttpSession session, Player player);
}