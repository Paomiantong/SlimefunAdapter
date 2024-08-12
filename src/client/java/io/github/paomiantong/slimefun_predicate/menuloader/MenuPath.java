package io.github.paomiantong.slimefun_predicate.menuloader;

import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Stack;

@Getter
public class MenuPath {
    private static final Stack<MenuPath> MENU_PATH_STACK = new Stack<>();
    @Setter
    private boolean unlocked;
    private final String title;
    private final Type type;

    public static MenuPath current() {
        return MENU_PATH_STACK.peek();
    }

    public static void push(String title, Type type) {
        MENU_PATH_STACK.push(new MenuPath(title, type));
    }

    public static void pop() {
        MenuPath screen = MENU_PATH_STACK.pop();
        if (screen != null && screen.type == Type.Category) {
            if (screen.unlocked)
                SlimefunManager.addCompletelyUnlockedCategory(screen.title);
            else { // 如果当前子分类未完全解锁，则将其父分类也标记为未完全解锁
                MenuPath cur = MENU_PATH_STACK.peek();
                if (cur.type != Type.Item)
                    cur.setUnlocked(false);
            }
        }
    }

    public static void init() {
        MENU_PATH_STACK.clear();
        MENU_PATH_STACK.push(new MenuPath("Root", Type.Root));
    }

    private MenuPath(String title, Type type) {
        this.unlocked = true;
        this.title = title;
        this.type = type;
    }

    public enum Type {
        Category, Item, Root
    }
}
