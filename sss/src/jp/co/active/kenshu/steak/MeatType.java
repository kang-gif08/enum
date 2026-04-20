package jp.co.active.kenshu.steak;

/**
 * 肉の種類を表す enum です。
 * 「固定の選択肢」を安全に扱うために enum を使っています。
 */
public enum MeatType implements Selectable {
    SIRLOIN(1, "サーロイン"),
    LOIN(2, "ロース"),
    FILLET(3, "ヒレ"),
    PORK(4, "ポーク");

    private final int code;
    private final String displayName;

    MeatType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}