package cn.jxufe.seedsystem.service.impl;

import cn.jxufe.seedsystem.entity.Player;
import cn.jxufe.seedsystem.repository.PlayerRepository;
import cn.jxufe.seedsystem.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public List<Player> findByPage(int page, int rows, String username) {
        return playerRepository.findByUsernameLike(
                username != null ? username : "",
                PageRequest.of(page - 1, rows)
        );
    }

    @Override
    public long count(String username) {
        return playerRepository.countByUsernameLike(username != null ? username : "");
    }

    @Override
    @Transactional
    public void saveOrUpdate(Player player) {
        if (player.getId() != null) {
            Player existing = playerRepository.findById(player.getId()).orElse(null);
            if (existing != null) {
                if (player.getUsername() != null) existing.setUsername(player.getUsername());
                if (player.getNickname() != null) existing.setNickname(player.getNickname());
                existing.setExp(player.getExp());
                existing.setPoints(player.getPoints());
                existing.setGold(player.getGold());
                if (player.getAvatarUrl() != null) existing.setAvatarUrl(player.getAvatarUrl());
                playerRepository.save(existing);
                return;
            }
        }
        playerRepository.save(player);
    }

    @Override
    @Transactional
    public int update(Player player) {
        if (player.getId() == null) return 0;

        Player existing = playerRepository.findById(player.getId()).orElse(null);
        if (existing == null) return 0;

        if (player.getUsername() != null) existing.setUsername(player.getUsername());
        if (player.getNickname() != null) existing.setNickname(player.getNickname());
        existing.setExp(player.getExp());
        existing.setPoints(player.getPoints());
        existing.setGold(player.getGold());
        if (player.getAvatarUrl() != null) existing.setAvatarUrl(player.getAvatarUrl());

        playerRepository.save(existing);
        return 1;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateAvatarUrl(String username, String avatarUrl) {
        Player player = playerRepository.findByUsername(username);
        if (player != null) {
            player.setAvatarUrl(avatarUrl);
            player.setUpdateTime(java.time.LocalDateTime.now());
            playerRepository.save(player);
        }
    }

    @Override
    public Player getById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public int testCount() {
        return (int) playerRepository.count();
    }
}
