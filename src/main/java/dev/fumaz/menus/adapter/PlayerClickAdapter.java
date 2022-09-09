package dev.fumaz.menus.adapter;

import org.bukkit.entity.Player;

public class PlayerClickAdapter implements ClickAdapter<Player> {

    @Override
    public Player adapt(Player player) {
        return player;
    }

}
