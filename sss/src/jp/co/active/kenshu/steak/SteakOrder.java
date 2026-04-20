package jp.co.active.kenshu.steak;

/**
 * メインクラスです。
 * main メソッドには余計な処理を書かず、Waiter を呼び出すだけにしています。
 */
public class SteakOrder {
    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        waiter.startOrder();
    }
}