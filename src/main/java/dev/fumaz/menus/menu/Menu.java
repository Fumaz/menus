package dev.fumaz.menus.menu;

import dev.fumaz.menus.adapter.ClickAdapter;
import dev.fumaz.menus.button.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Menu<T> implements Listener {

    private final Map<Integer, Button<T>> buttons = new HashMap<>();
    private final JavaPlugin plugin;
    private final ClickAdapter<T> adapter;

    private Inventory inventory;
    private boolean registered = false;
    private boolean interactable = false;
    private boolean closeable = true;
    private boolean updateOnInteract = false;
    private String title;
    private int slots;
    private InventoryType inventoryType;
    private BiConsumer<T, InventoryCloseEvent> close = (t, event) -> {
    };
    private BukkitTask updateTask;

    public Menu(JavaPlugin plugin, ClickAdapter<T> adapter, String title, int rows) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.slots = rows * 9;
        this.title = title;

        createInventory();
    }

    public Menu(JavaPlugin plugin, ClickAdapter<T> adapter, String title, InventoryType inventoryType) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.slots = inventoryType.getDefaultSize();
        this.inventoryType = inventoryType;
        this.title = title;

        createInventory();
    }

    public void title(String title) {
        this.title = title;
        createInventory();
    }

    public void slots(int slots) {
        this.slots = slots;
        createInventory();
    }

    public void type(InventoryType type) {
        this.inventoryType = type;
        createInventory();
    }

    public void interactable(boolean interactable) {
        this.interactable = interactable;
    }

    public void updateOnInteract(boolean updateOnInteract) {
        this.updateOnInteract = updateOnInteract;
    }

    public void updateEvery(int ticks) {
        if (updateTask != null) {
            updateTask.cancel();
        }

        this.updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, ticks, ticks);
    }

    public void close(BiConsumer<T, InventoryCloseEvent> close) {
        this.close = close;
    }

    public void closeable(boolean closeable) {
        this.closeable = closeable;
    }

    public void add(int slot, Button<T> button) {
        buttons.put(slot, button);
    }

    public void add(int row, int column, Button<T> button) {
        add(((row - 1) * 9) + (column - 1), button);
    }

    public void remove(int slot) {
        buttons.remove(slot);
    }

    public void remove(int row, int column) {
        remove(((row - 1) * 9) + (column - 1));
    }

    public void remove(Button<T> button) {
        buttons.values().remove(button);
    }

    public Button<T> get(int slot) {
        return buttons.get(slot);
    }

    public void fill(Button<T> button) {
        for (int slot = 0; slot < slots; ++slot) {
            add(slot, button);
        }
    }

    public void fillEmpty(Button<T> button) {
        getEmptySlots().forEach(slot -> add(slot, button));
    }

    public void fillRow(int row, Button<T> button) {
        for (int slot = (row - 1) * 9; slot < row * 9; slot++) {
            add(slot, button);
        }
    }

    public void fillColumn(int column, Button<T> button) {
        for (int i = 0; i < (slots / 9); i++) {
            add((i * 9) + (column - 1), button);
        }
    }

    public void open(T t) {
        update();
        register();

        adapter.adapt(t).openInventory(inventory);
    }

    public void close(T t) {
        if (inventory.getViewers().isEmpty()) {
            unregister();
        }

        adapter.adapt(t).closeInventory();
    }

    public void update() {
        inventory.clear();
        buttons.forEach((slot, button) -> inventory.setItem(slot, button.getItemStack()));
    }

    private void register() {
        if (registered) {
            return;
        }

        registered = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void unregister() {
        if (!registered) {
            return;
        }

        HandlerList.unregisterAll(this);
        registered = false;

        if (updateTask != null) {
            updateTask.cancel();
        }
    }

    private List<Integer> getEmptySlots() {
        return IntStream.range(0, slots)
                .filter(slot -> get(slot) == null)
                .boxed()
                .collect(Collectors.toList());
    }

    private void createInventory() {
        List<HumanEntity> viewers = inventory.getViewers();

        if (inventoryType != null) {
            this.inventory = Bukkit.createInventory(null, inventoryType, title);
        } else {
            this.inventory = Bukkit.createInventory(null, slots, title);
        }

        viewers.forEach(viewer -> open(adapter.adapt((Player) viewer)));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory() != inventory) {
            return;
        }

        if (event.getRawSlot() >= slots) {
            return;
        }

        if (!interactable) {
            event.setCancelled(true);
        }

        T t = adapter.adapt((Player) event.getWhoClicked());
        Button<T> button = get(event.getRawSlot());

        if (button != null) {
            button.click(t, event);
        }

        if (updateOnInteract) {
            update();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory() != inventory) {
            return;
        }

        if (event.getRawSlots().stream().noneMatch(slot -> slot < slots)) {
            return;
        }

        if (!interactable) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory() != inventory) {
            return;
        }

        T t = adapter.adapt((Player) event.getPlayer());

        if (!closeable) {
            open(t);
            return;
        }

        close.accept(t, event);
    }

}
