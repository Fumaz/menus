package dev.fumaz.menus.menu;

import dev.fumaz.items.ItemBuilder;
import dev.fumaz.menus.adapter.ClickAdapter;
import dev.fumaz.menus.button.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaginatedMenu<T> extends Menu<T> {

    private final List<Button<T>> buttons = new ArrayList<>();
    private final Map<Integer, Button<T>> pinned = new HashMap<>();

    private ItemStack previousPageItem = ItemBuilder.of(Material.ARROW)
            .displayName(ChatColor.LIGHT_PURPLE + "Previous Page")
            .build();
    private ItemStack nextPageItem = ItemBuilder.of(Material.ARROW)
            .displayName(ChatColor.LIGHT_PURPLE + "Next Page")
            .build();

    private int previousPageSlot = 45;
    private int nextPageSlot = 53;
    private int page = 0;
    private boolean infinite = false;

    public PaginatedMenu(JavaPlugin plugin, ClickAdapter<T> adapter, String title, int rows) {
        super(plugin, adapter, title, rows);
    }

    public PaginatedMenu(JavaPlugin plugin, ClickAdapter<T> adapter, String title, InventoryType inventoryType) {
        super(plugin, adapter, title, inventoryType);
    }

    @Override
    public void add(Button<T> button) {
        buttons.add(button);
    }

    @Override
    public void add(int slot, Button<T> button) {
        pinned.put(slot, button);
    }

    @Override
    public void add(int row, int column, Button<T> button) {
        add(((row - 1) * 9) + (column - 1), button);
    }

    @Override
    public void remove(Button<T> button) {
        buttons.remove(button);
    }

    @Override
    public void remove(int slot) {
        pinned.remove(slot);
    }

    @Override
    public void remove(int row, int column) {
        pinned.remove(((row - 1) * 9) + (column - 1));
    }

    @Override
    public void fill(Button<T> button) {
        pinned.clear();
        IntStream.range(0, slots()).forEach(slot -> pinned.put(slot, button));
    }

    @Override
    public void fill(Supplier<Button<T>> buttonSupplier) {
        IntStream.range(0, slots()).forEach(slot -> pinned.put(slot, buttonSupplier.get()));
    }

    @Override
    public void fillEmpty(Button<T> button) {
        IntStream.range(0, slots()).forEach(slot -> {
            if (pinned.containsKey(slot)) {
                return;
            }

            pinned.put(slot, button);
        });
    }

    @Override
    public void fillEmpty(Supplier<Button<T>> buttonSupplier) {
        IntStream.range(0, slots()).forEach(slot -> {
            if (pinned.containsKey(slot)) {
                return;
            }

            pinned.put(slot, buttonSupplier.get());
        });
    }

    @Override
    public void fillRow(int row, Button<T> button) {
        IntStream.range((row - 1) * 9, (row - 1) * 9 + 9).forEach(slot -> pinned.put(slot, button));
    }

    @Override
    public void fillColumn(int column, Button<T> button) {
        IntStream.range(column - 1, slots()).forEach(slot -> {
            if (slot % 9 != column - 1) {
                return;
            }

            pinned.put(slot, button);
        });
    }

    @Override
    public void fillRow(int row, Supplier<Button<T>> buttonSupplier) {
        IntStream.range((row - 1) * 9, (row - 1) * 9 + 9).forEach(slot -> pinned.put(slot, buttonSupplier.get()));
    }

    @Override
    public void fillColumn(int column, Supplier<Button<T>> buttonSupplier) {
        IntStream.range(column - 1, slots()).forEach(slot -> {
            if (slot % 9 != column - 1) {
                return;
            }

            pinned.put(slot, buttonSupplier.get());
        });
    }

    public void nextPageItem(ItemStack nextPageItem) {
        this.nextPageItem = nextPageItem;
    }

    public void previousPageItem(ItemStack previousPageItem) {
        this.previousPageItem = previousPageItem;
    }

    public void nextPageSlot(int nextPageSlot) {
        this.nextPageSlot = nextPageSlot;
    }

    public void previousPageSlot(int previousPageSlot) {
        this.previousPageSlot = previousPageSlot;
    }

    public void infinite(boolean infinite) {
        this.infinite = infinite;
    }

    public void nextPage() {
        if (page >= (pages() - 1) && !infinite) {
            return;
        }

        page++;

        if (page >= pages()) {
            page = 0;
        }
    }

    public void previousPage() {
        if (page <= 0 && !infinite) {
            return;
        }

        page--;

        if (page < 0) {
            page = pages();
        }
    }

    @Override
    public void update() {
        super.clear();

        pinned.forEach(super::add);
        super.add(previousPageSlot, Button.<T>builder()
                .item(previousPageItem)
                .click((clicker, event) -> {
                    previousPage();
                    update();
                })
                .build());
        super.add(nextPageSlot, Button.<T>builder()
                .item(nextPageItem)
                .click((clicker, event) -> {
                    nextPage();
                    update();
                })
                .build());

        buttons(page).forEach(super::add);

        super.update();
    }

    private List<Integer> availableSlots() {
        return IntStream.range(0, slots())
                .filter(slot -> !pinned.containsKey(slot))
                .boxed()
                .collect(Collectors.toList());
    }

    private int pages() {
        return (int) Math.ceil((double) buttons.size() / availableSlots().size());
    }

    private List<Button<T>> buttons(int page) {
        int start = page * availableSlots().size();
        int end = start + availableSlots().size();

        if (end > buttons.size()) {
            end = buttons.size();
        }

        return buttons.subList(start, end);
    }

}
