package dev.fumaz.menus.adapter;

import org.bukkit.entity.Player;

public interface ClickAdapter<T> {

    static PlayerClickAdapter player() {
        return new PlayerClickAdapter();
    }

    T adapt(Player player);

    Player adapt(T t);

}
