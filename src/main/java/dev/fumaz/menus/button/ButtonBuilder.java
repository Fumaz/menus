package dev.fumaz.menus.button;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ButtonBuilder<T> {

    private Supplier<ItemStack> item;
    private BiConsumer<T, InventoryClickEvent> click = (t, event) -> {
    };
    private boolean interactable = false;

    protected ButtonBuilder() {
    }

    public ButtonBuilder<T> item(@Nullable ItemStack item) {
        this.item = () -> item;
        return this;
    }

    public ButtonBuilder<T> item(@NotNull Supplier<ItemStack> item) {
        this.item = item;
        return this;
    }

    public ButtonBuilder<T> click(@Nullable BiConsumer<T, InventoryClickEvent> click) {
        this.click = click;
        return this;
    }

    public ButtonBuilder<T> interactable(boolean interactable) {
        this.interactable = interactable;
        return this;
    }

    public ButtonBuilder<T> interactable() {
        return interactable(true);
    }

    public Button<T> build() {
        return new Button<>() {
            @Override
            public @Nullable ItemStack getItemStack() {
                return item.get();
            }

            @Override
            public void click(@NotNull T t, @NotNull InventoryClickEvent event) {
                if (!interactable) {
                    event.setCancelled(true);
                }

                click.accept(t, event);
            }
        };
    }

}
