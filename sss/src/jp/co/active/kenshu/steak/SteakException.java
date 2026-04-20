package jp.co.active.kenshu.steak;

/**
 * ステーキ注文プログラム専用の独自例外です。
 * RuntimeException を継承しています。
 */
public class SteakException extends RuntimeException {
    public SteakException(String message) {
        super(message);
    }
}