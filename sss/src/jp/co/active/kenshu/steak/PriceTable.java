package jp.co.active.kenshu.steak;

import java.util.EnumMap;
import java.util.Map;

/**
 * 値段情報をまとめて持つクラスです。
 *
 * 要件変更時にここを差し替えるだけで済むように、
 * 肉の g 単価・サイドメニュー価格・消費税率を集中管理しています。
 */
public class PriceTable {
    private static final int DEFAULT_TAX_RATE = 10;

    private final Map<MeatType, Integer> meatPricePerGram;
    private final Map<SideMenuType, Integer> sideMenuPrice;
    private final int taxRate;

    public PriceTable() {
        this(DEFAULT_TAX_RATE);
    }

    public PriceTable(int taxRate) {
        this.taxRate = taxRate;
        this.meatPricePerGram = createMeatPriceMap();
        this.sideMenuPrice = createSideMenuPriceMap();
    }

    private Map<MeatType, Integer> createMeatPriceMap() {
        Map<MeatType, Integer> prices = new EnumMap<>(MeatType.class);
        prices.put(MeatType.SIRLOIN, 6);
        prices.put(MeatType.LOIN, 7);
        prices.put(MeatType.FILLET, 9);
        prices.put(MeatType.PORK, 5);
        return prices;
    }

    private Map<SideMenuType, Integer> createSideMenuPriceMap() {
        Map<SideMenuType, Integer> prices = new EnumMap<>(SideMenuType.class);
        prices.put(SideMenuType.SALAD, 100);
        prices.put(SideMenuType.SOUP, 200);
        prices.put(SideMenuType.COFFEE, 150);
        return prices;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public int getMeatPricePerGram(MeatType meatType) {
        return meatPricePerGram.get(meatType);
    }

    public int getSideMenuPrice(SideMenuType sideMenuType) {
        return sideMenuPrice.get(sideMenuType);
    }

    public int calculateSubtotal(Menu menu) {
        int subtotal = menu.getGrams() * getMeatPricePerGram(menu.getMeatType());

        for (SideMenuType sideMenuType : menu.getSideMenus()) {
            subtotal += getSideMenuPrice(sideMenuType);
        }
        return subtotal;
    }

    public int calculateTotal(Menu menu) {
        int subtotal = calculateSubtotal(menu);
        return subtotal * (100 + taxRate) / 100;
    }
}