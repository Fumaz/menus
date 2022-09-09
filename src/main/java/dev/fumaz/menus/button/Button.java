package dev.fumaz.menus.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Button<T> {

    static <T> ButtonBuilder<T> builder() {
        return new ButtonBuilder<>();
    }

    @Nullable ItemStack getItemStack();

    void click(@NotNull T t, @NotNull InventoryClickEvent event);

}
