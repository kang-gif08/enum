package jp.co.active.kenshu.steak;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 注文受付・入力チェック・金額計算・結果表示を担当するクラスです。
 *
 * main には処理を書かず、このクラスに役割を集約しています。
 */
public class Waiter {
    private static final int MIN_GRAMS = 1;
    private static final int MAX_GRAMS = 1000;

    private final Scanner scanner;
    private final PriceTable priceTable;
    private final List<Menu> orderHistory;

    public Waiter() {
        this.scanner = new Scanner(System.in);
        this.priceTable = new PriceTable();
        this.orderHistory = new ArrayList<>();
    }

    /**
     * 注文処理全体を開始します。
     */
    public void startOrder() {
        boolean continueOrder = true;

        while (continueOrder) {
            Menu menu = takeOrder();
            orderHistory.add(menu);
            printOrderResult(menu);
            continueOrder = askAdditionalOrder();
        }

        System.out.println("ご注文ありがとうございました。");
        scanner.close();
    }

    /**
     * 1回分の注文を受け付けます。
     */
    private Menu takeOrder() {
        MeatType meatType = promptSingleChoice(
                "肉の種類を選んでください。 " + buildChoiceGuide(MeatType.values(), true),
                MeatType.class);

        int grams = promptGrams();

        CookingLevel cookingLevel = promptCookingLevel(meatType);

        List<SideMenuType> sideMenus = promptMultipleChoice(
                "サブメニューを選んでください。 "
                        + buildChoiceGuide(SideMenuType.values(), false)
                        + "（複数選ぶ場合はカンマ区切り、なしの場合は空 Enter）",
                SideMenuType.class);

        return new Menu(meatType, grams, cookingLevel, sideMenus);
    }

    /**
     * グラム数を入力させます。
     */
    private int promptGrams() {
        while (true) {
            try {
                System.out.println("グラム数を入力してください。(1～1000)");
                String input = scanner.nextLine().trim();
                int grams = Integer.parseInt(input);

                if (grams < MIN_GRAMS || grams > MAX_GRAMS) {
                    throw new SteakException("グラム数は1～1000の範囲で入力してください。");
                }
                return grams;
            } catch (NumberFormatException e) {
                System.out.println("グラム数は数字で入力してください。");
            } catch (SteakException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 肉の種類に応じた焼き加減入力を行います。
     * ポーク × レア の組み合わせは独自例外で弾きます。
     */
    private CookingLevel promptCookingLevel(MeatType meatType) {
        while (true) {
            try {
                CookingLevel cookingLevel = promptSingleChoice(
                        "焼き加減を選んでください。 " + buildChoiceGuide(CookingLevel.values(), true),
                        CookingLevel.class);

                validateCookingCombination(meatType, cookingLevel);
                return cookingLevel;
            } catch (SteakException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * enum の単一選択入力を共通処理化したメソッドです。
     */
    private <E extends Enum<E> & Selectable> E promptSingleChoice(String message, Class<E> enumClass) {
        while (true) {
            try {
                System.out.println(message);
                String input = scanner.nextLine().trim();

                int code = Integer.parseInt(input);
                E selected = findByCode(enumClass, code);

                if (selected == null) {
                    throw new SteakException("選択肢にない番号です。正しい番号を入力してください。");
                }
                return selected;
            } catch (NumberFormatException e) {
                System.out.println("番号は数字で入力してください。");
            } catch (SteakException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * enum の複数選択入力を共通処理化したメソッドです。
     */
    private <E extends Enum<E> & Selectable> List<E> promptMultipleChoice(String message, Class<E> enumClass) {
        while (true) {
            try {
                System.out.println(message);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    return new ArrayList<>();
                }

                String[] tokens = input.split(",");
                Set<E> selectedSet = new LinkedHashSet<>();

                for (String token : tokens) {
                    int code = Integer.parseInt(token.trim());
                    E selected = findByCode(enumClass, code);

                    if (selected == null) {
                        throw new SteakException("選択肢にない番号です。正しい番号を入力してください。");
                    }
                    if (!selectedSet.add(selected)) {
                        throw new SteakException("同じサブメニューは複数選択できません。");
                    }
                }
                return new ArrayList<>(selectedSet);
            } catch (NumberFormatException e) {
                System.out.println("番号はカンマ区切りの数字で入力してください。");
            } catch (SteakException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * 選択肢の表示文字列を組み立てます。
     * 価格表示が必要なものだけ価格を付けています。
     */
    private String buildChoiceGuide(MeatType[] meatTypes, boolean lineBreak) {
        return buildChoiceGuideInternal(meatTypes, lineBreak, true, false);
    }

    private String buildChoiceGuide(CookingLevel[] cookingLevels, boolean lineBreak) {
        return buildChoiceGuideInternal(cookingLevels, lineBreak, false, false);
    }

    private String buildChoiceGuide(SideMenuType[] sideMenus, boolean lineBreak) {
        return buildChoiceGuideInternal(sideMenus, lineBreak, false, true);
    }

    private <E extends Enum<E> & Selectable> String buildChoiceGuideInternal(
            E[] values,
            boolean lineBreak,
            boolean meatPrice,
            boolean sidePrice) {
        String separator = lineBreak ? System.lineSeparator() : " ";
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            E value = values[i];
            builder.append(value.getCode())
                    .append(":")
                    .append(value.getDisplayName());

            if (meatPrice && value instanceof MeatType) {
                builder.append("(")
                        .append(priceTable.getMeatPricePerGram((MeatType) value))
                        .append("円/g)");
            }
            if (sidePrice && value instanceof SideMenuType) {
                builder.append("(")
                        .append(priceTable.getSideMenuPrice((SideMenuType) value))
                        .append("円)");
            }

            if (i < values.length - 1) {
                builder.append(lineBreak ? separator : " / ");
            }
        }
        return builder.toString();
    }

    /**
     * 番号から enum を取り出します。
     */
    private <E extends Enum<E> & Selectable> E findByCode(Class<E> enumClass, int code) {
        for (E value : enumClass.getEnumConstants()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }

    /**
     * ポークをレアで注文しようとした場合のチェックです。
     */
    private void validateCookingCombination(MeatType meatType, CookingLevel cookingLevel) {
        if (meatType == MeatType.PORK && cookingLevel == CookingLevel.RARE) {
            throw new SteakException("お腹をこわしますよ！");
        }
    }

    /**
     * 注文結果を表示します。
     */
    private void printOrderResult(Menu menu) {
        int total = priceTable.calculateTotal(menu);
        StringBuilder builder = new StringBuilder();

        builder.append(menu.getCookingLevel().getMessage())
                .append(menu.getMeatType().getDisplayName())
                .append("ステーキ ")
                .append(menu.getGrams())
                .append("g");

        if (!menu.getSideMenus().isEmpty()) {
            builder.append(" ");
            for (int i = 0; i < menu.getSideMenus().size(); i++) {
                builder.append(menu.getSideMenus().get(i).getDisplayName());
                if (i < menu.getSideMenus().size() - 1) {
                    builder.append("・");
                }
            }
            builder.append("付");
        }

        builder.append(" (")
                .append(String.format("%,d", total))
                .append("円)");

        System.out.println(builder.toString());
    }

    /**
     * 追加注文の有無を確認します。
     */
    private boolean askAdditionalOrder() {
        while (true) {
            System.out.println("もう一つ注文しますか？[y/n]");
            String input = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(input)) {
                return true;
            }
            if ("n".equals(input)) {
                return false;
            }
            System.out.println("y か n を入力してください。");
        }
    }
}