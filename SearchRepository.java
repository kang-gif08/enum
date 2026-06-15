package repository;

import dto.SearchSuggestionDto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 検索機能用のリポジトリ
 */
public class SearchRepository {

    private Connection conn;

    public SearchRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * キーワードが入力されている場合：候補を最大10件返す。
     * 優先順位はユーザー → チャンネル → メッセージ。
     *
     * @param keyword キーワード
     * @param userId  現在のログインユーザーID
     */
    public List<SearchSuggestionDto> findSuggestions(String keyword, String userId) throws SQLException {

        List<SearchSuggestionDto> list = new ArrayList<>();

        String sql = "SELECT type, title, content FROM (" +

        // 自分が参加しているワークスペースのユーザーを検索
                "SELECT 'USER' AS type, u.user_name AS title, '' AS content, 1 AS priority " +
                "FROM slick_user u " +
                "JOIN workspace_user wu ON u.id = wu.user_id " +
                "JOIN workspace_user mywu ON mywu.workspace_id = wu.workspace_id AND mywu.user_id = ? " +
                "WHERE u.user_name LIKE ? " +

                "UNION ALL " +

                // 自分が参加しているチャンネルを検索
                "SELECT 'CHANNEL' AS type, c.channel_name AS title, '' AS content, 2 AS priority " +
                "FROM channel c " +
                "JOIN user_channel uc ON c.channel_id = uc.channel_id " +
                "WHERE uc.user_id = ? " +
                "  AND c.channel_name LIKE ? " +

                "UNION ALL " +

                // 自分が参加しているチャンネルのメッセージを検索
                "SELECT 'MESSAGE' AS type, m.message_text AS title, m.message_text AS content, 3 AS priority " +
                "FROM message m " +
                "JOIN user_channel uc2 ON m.channel_id = uc2.channel_id " +
                "WHERE uc2.user_id = ? " +
                "  AND m.message_text LIKE ? " +

                ") " +
                "ORDER BY priority " +
                "FETCH FIRST 10 ROWS ONLY";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            String word = "%" + keyword + "%";
            // 1つ目のJOIN句用
            ps.setString(1, userId);
            ps.setString(2, word);
            // チャンネル用
            ps.setString(3, userId);
            ps.setString(4, word);
            // メッセージ用
            ps.setString(5, userId);
            ps.setString(6, word);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new SearchSuggestionDto(
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("content")));
            }
        }

        return list;
    }

    /**
     * 検索欄が空の場合：検索履歴を最大10件取得する。
     *
     * @param userId ログインユーザーID
     */
    public List<SearchSuggestionDto> findHistory(String userId) throws SQLException {

        List<SearchSuggestionDto> list = new ArrayList<>();

        String sql = "SELECT search_history " +
                "FROM search " +
                "WHERE id = ? " +
                "ORDER BY search_id DESC " +
                "FETCH FIRST 10 ROWS ONLY";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new SearchSuggestionDto(
                        "HISTORY",
                        rs.getString("search_history"),
                        ""));
            }
        }

        return list;
    }

    /**
     * 検索履歴を登録する
     *
     * @param userId  ログインユーザーID
     * @param keyword 検索ワード
     */
    public void insertHistory(String userId, String keyword) throws SQLException {

        String sql = "INSERT INTO search(search_id, id, search_history) " +
                "VALUES (LPAD(search_seq.NEXTVAL, 6, '0'), ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, keyword);
            ps.executeUpdate();
        }
    }
}