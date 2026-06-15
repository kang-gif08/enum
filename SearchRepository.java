package repository.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import config.DatabaseConnector;
import dto.search.SearchDto;
import entity.search.Search;

/**
 * SEARCHテーブル用Repository。
 */
public class SearchRepository {

    /**
     * 検索履歴を登録する。
     *
     * @param search 登録する検索履歴Entity
     * @return 登録した検索履歴Entity
     * @throws SQLException SQL例外発生時
     */
    public Search insert(Search search) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO search (search_id, id, search_text) VALUES (?, ?, ?)",
                new String[] { "search_id" })) {

            ps.setString(1, search.getSearchId());
            ps.setString(2, search.getId());
            ps.setString(3, search.getSearchText());

            int ret = ps.executeUpdate();
            if (ret == 0) {
                throw new SQLException("登録に失敗しました。");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    search.setSearchId(rs.getString(1));
                }
            }

            return search;
        }
    }

    /**
     * ユーザー名から検索する。
     *
     * @param keyword 検索文字
     * @return 検索結果リスト
     * @throws SQLException SQL例外発生時
     */
    public ArrayList<SearchDto> selectUser(String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, name, display_name FROM slick_user "
                        + "WHERE name LIKE ? OR display_name LIKE ? "
                        + "LIMIT 10")) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("user");
                    dto.setTargetId(rs.getString("user_id"));
                    dto.setDisplayName(rs.getString("display_name"));
                    dto.setDetail(rs.getString("name"));
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * チャンネル名から検索する。
     *
     * @param keyword 検索文字
     * @return 検索結果リスト
     * @throws SQLException SQL例外発生時
     */
    public ArrayList<SearchDto> selectChannel(String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT channel_id, name FROM channel "
                        + "WHERE name LIKE ? "
                        + "LIMIT 10")) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("channel");
                    dto.setTargetId(rs.getString("channel_id"));
                    dto.setDisplayName(rs.getString("name"));
                    dto.setDetail("チャンネル");
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * メッセージ本文から検索する。
     *
     * @param keyword 検索文字
     * @return 検索結果リスト
     * @throws SQLException SQL例外発生時
     */
    public ArrayList<SearchDto> selectMessage(String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT message_id, message_text FROM message "
                        + "WHERE message_text LIKE ? "
                        + "ORDER BY created_at DESC "
                        + "LIMIT 10")) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("message");
                    dto.setTargetId(rs.getString("message_id"));
                    dto.setDisplayName(rs.getString("message_text"));
                    dto.setDetail("メッセージ");
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * 検索履歴を取得する。
     *
     * @param userId ログインユーザーID
     * @return 検索履歴リスト
     * @throws SQLException SQL例外発生時
     */
    public ArrayList<SearchDto> selectHistory(String userId) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT search_id, search_text FROM search "
                        + "WHERE id = ? "
                        + "ORDER BY search_id DESC "
                        + "LIMIT 10")) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("history");
                    dto.setTargetId(rs.getString("search_id"));
                    dto.setDisplayName(rs.getString("search_text"));
                    dto.setDetail("検索履歴");
                    list.add(dto);
                }
            }
        }

        return list;
    }
}