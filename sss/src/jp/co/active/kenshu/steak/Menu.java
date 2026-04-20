package jp.co.active.kenshu.steak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1回分の注文情報を持つクラスです。
 *
 * このクラスは「情報を持つ役割」に限定し、
 * 計算や入力処理は持たせていません。
 */
public class Menu {
    private final MeatType meatType;
    private final int grams;
    private final CookingLevel cookingLevel;
    private final List<SideMenuType> sideMenus;

    public Menu(MeatType meatType, int grams, CookingLevel cookingLevel, List<SideMenuType> sideMenus) {
        this.meatType = meatType;
        this.grams = grams;
        this.cookingLevel = cookingLevel;
        this.sideMenus = new ArrayList<>(sideMenus);
    }

    public MeatType getMeatType() {
        return meatType;
    }

    public int getGrams() {
        return grams;
    }

    public CookingLevel getCookingLevel() {
        return cookingLevel;
    }

    public List<SideMenuType> getSideMenus() {
        return Collections.unmodifiableList(sideMenus);
    }
}