package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.Message;
import cn.jxufe.seedsystem.entity.Player;
import cn.jxufe.seedsystem.repository.PlayerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmServiceImpl implements FarmService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public List<Player> findAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Message setCurUser(HttpSession session, Player player) {
        Player dbPlayer = playerRepository.findByNickname(player.getNickname());
        if (dbPlayer != null) {
            session.setAttribute("currentPlayer", dbPlayer);
            return new Message(0, "角色设置成功", dbPlayer);
        } else {
            return new Message(1, "角色不存在");
        }
    }
}
