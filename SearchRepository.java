package repository.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnector;
import dto.search.SearchDto;

/**
 * 検索機能用Repository
 */
public class SearchRepository {

    /**
     * キーワードが入力されている場合、候補を最大10件取得する。
     * 優先順位は ユーザー → チャンネル → メッセージ の順。
     *
     * @param keyword キーワード
     * @param userId  現在のログインユーザーID
     * @return 検索結果のList
     * @throws SQLException SQL例外発生時
     */
    public List<SearchDto> find(String keyword, String userId) throws SQLException {

        List<SearchDto> list = new ArrayList<>();

        String sql = "SELECT type, word FROM ( " +

        // 自分が参加しているワークスペースのユーザーを検索
                "SELECT 'USER' AS type, u.user_name AS word, 1 AS priority " +
                "FROM slick_user u " +
                "JOIN workspace_user wu ON u.id = wu.user_id " +
                "JOIN workspace_user mywu ON mywu.workspace_id = wu.workspace_id " +
                "WHERE mywu.user_id = ? " +
                "AND u.user_name LIKE ? " +

                "UNION ALL " +

                // 自分が参加しているチャンネルを検索
                "SELECT 'CHANNEL' AS type, c.channel_name AS word, 2 AS priority " +
                "FROM channel c " +
                "JOIN user_channel uc ON c.channel_id = uc.channel_id " +
                "WHERE uc.user_id = ? " +
                "AND c.channel_name LIKE ? " +

                "UNION ALL " +

                // 自分が参加しているチャンネルのメッセージを検索
                "SELECT 'MESSAGE' AS type, m.message_text AS word, 3 AS priority " +
                "FROM message m " +
                "JOIN user_channel uc2 ON m.channel_id = uc2.channel_id " +
                "WHERE uc2.user_id = ? " +
                "AND m.message_text LIKE ? " +

                ") " +
                "ORDER BY priority " +
                "FETCH FIRST 10 ROWS ONLY";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            String word = "%" + keyword + "%";

            ps.setString(1, userId);
            ps.setString(2, word);
            ps.setString(3, userId);
            ps.setString(4, word);
            ps.setString(5, userId);
            ps.setString(6, word);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new SearchDto(
                            rs.getString("type"),
                            rs.getString("word")));
                }
            }
        }

        return list;
    }

    /**
     * 検索欄が空の場合、検索履歴を最大10件取得する。
     *
     * @param userId ログインユーザーID
     * @return 検索履歴のList
     * @throws SQLException SQL例外発生時
     */
    public List<SearchDto> findHistory(String userId) throws SQLException {

        List<SearchDto> list = new ArrayList<>();

        String sql = "SELECT search_history " +
                "FROM search " +
                "WHERE id = ? " +
                "ORDER BY search_id DESC " +
                "FETCH FIRST 10 ROWS ONLY";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new SearchDto(
                            "HISTORY",
                            rs.getString("search_history")));
                }
            }
        }

        return list;
    }

    /**
     * 検索履歴を登録する。
     *
     * @param userId  ログインユーザーID
     * @param keyword 検索ワード
     * @throws SQLException SQL例外発生時
     */
    public void insertHistory(String userId, String keyword) throws SQLException {

        String sql = "INSERT INTO search(id, search_history) " +
                "VALUES (?, ?)";

        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, keyword);

            int ret = ps.executeUpdate();

            if (ret == 0) {
                throw new SQLException("検索履歴の登録に失敗しました。");
            }
        }
    }
}