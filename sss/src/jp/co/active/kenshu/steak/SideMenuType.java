package jp.co.active.kenshu.steak;

/**
 * サイドメニューの種類を表す enum です。
 */
public enum SideMenuType implements Selectable {
    SALAD(1, "サラダ"),
    SOUP(2, "スープ"),
    COFFEE(3, "コーヒー");

    private final int code;
    private final String displayName;

    SideMenuType(int code, String displayName) {
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