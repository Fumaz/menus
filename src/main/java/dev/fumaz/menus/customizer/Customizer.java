package dev.fumaz.menus.customizer;

import dev.fumaz.menus.menu.Menu;

public interface Customizer<T> {

    void customize(Menu<T> menu);

}
