package service.search;

import java.sql.SQLException;
import java.util.List;

import dto.search.SearchDto;
import repository.search.SearchRepository;

/**
 * 検索機能用Service
 */
public class SearchService {

    private SearchRepository searchRepository = new SearchRepository();

    /**
     * 検索欄に表示する候補を取得する。
     *
     * キーワードが空の場合：検索履歴を最大10件取得
     * キーワードがある場合：ユーザー、チャンネル、メッセージを優先順位順に最大10件取得
     *
     * @param keyword 検索キーワード
     * @param userId  ログインユーザーID
     * @return 検索候補List
     * @throws SQLException SQL例外発生時
     */
    public List<SearchDto> getSearchList(String keyword, String userId) throws SQLException {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ユーザーIDが存在しません。");
        }

        // 検索欄が空なら検索履歴を返す
        if (keyword == null || keyword.trim().isEmpty()) {
            return searchRepository.findHistory(userId);
        }

        // 検索欄に文字が入力されている場合は検索候補を返す
        return searchRepository.find(keyword.trim(), userId);
    }

    /**
     * 検索履歴を登録する。
     *
     * Enterキー押下時や検索ボタン押下時に呼び出す。
     *
     * @param keyword 検索キーワード
     * @param userId  ログインユーザーID
     * @throws SQLException SQL例外発生時
     */
    public void saveSearchHistory(String keyword, String userId) throws SQLException {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ユーザーIDが存在しません。");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        searchRepository.insertHistory(userId, keyword.trim());
    }
}