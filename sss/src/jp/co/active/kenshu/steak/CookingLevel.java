package jp.co.active.kenshu.steak;

/**
 * 焼き加減を表す enum です。
 * 出力時の説明文も一緒に持たせています。
 */
public enum CookingLevel implements Selectable {
    RARE(1, "レア", "素材の味を生かした"),
    MEDIUM(2, "ミディアム", "いい焼き加減の"),
    WELL_DONE(3, "ウェルダン", "しっかり焼いた");

    private final int code;
    private final String displayName;
    private final String message;

    CookingLevel(int code, String displayName, String message) {
        this.code = code;
        this.displayName = displayName;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getMessage() {
        return message;
    }
}