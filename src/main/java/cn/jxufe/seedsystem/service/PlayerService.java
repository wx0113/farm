package cn.jxufe.seedsystem.service;

import cn.jxufe.seedsystem.entity.Player;
import java.util.List;

public interface PlayerService {

    List<Player> findByPage(int page, int rows, String username);

    long count(String username);

    void saveOrUpdate(Player player);

    int update(Player player);

    void delete(Long id);

    void updateAvatarUrl(String username, String avatarUrl);

    Player getById(Long id);

    int testCount();
}
