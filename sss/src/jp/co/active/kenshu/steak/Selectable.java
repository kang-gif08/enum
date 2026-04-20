package jp.co.active.kenshu.steak;

/**
 * 番号選択できる項目の共通インターフェイスです。
 * enum 側で実装することで、肉・焼き加減・サイドメニューを
 * 同じ入力処理で扱えるようにしています。
 */
public interface Selectable {
    int getCode();
    String getDisplayName();
}