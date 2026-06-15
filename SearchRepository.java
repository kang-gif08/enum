package repository.search;

import config.DatabaseConnector;
// ↑ DBコネクションを取得するユーティリティクラス想定
// ここはプロジェクトの実装に合わせて読み替えてください

import dto.search.SearchDto;
import entity.search.Search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * SEARCHテーブルなどを操作するRepository。
 * - ユーザー検索(ログインユーザーが所属するワークスペース内)
 * - チャンネル検索(ログインユーザーが参加しているチャンネルのみ)
 * - メッセージ検索(参加しているチャンネル・DM内のみ)
 * - 検索履歴の登録・取得
 */
public class SearchRepository {

    /**
     * 検索履歴の登録。
     * 
     * @param search 登録する検索履歴エンティティ
     * @return 登録結果のエンティティ
     * @throws SQLException SQL例外
     */
    public Search insert(Search search) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();

        // searchテーブルへ挿入
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO search (search_id, user_id, search_text, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new String[] { "search_id" })) {
            ps.setString(1, search.getSearchId());
            ps.setString(2, search.getUserId()); // entityのプロパティに合わせて
            ps.setString(3, search.getSearchText());

            int ret = ps.executeUpdate();
            if (ret == 0) {
                throw new SQLException("検索履歴の登録に失敗しました。");
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
     * 自分が所属するワークスペース内のユーザーを検索。
     * ログインユーザーが所属しているworkspace_idのみを対象とし、名前・表示名の部分一致。
     *
     * @param userId  ログインユーザーID
     * @param keyword 検索文字
     * @return 検索結果リスト（最大10件）
     * @throws SQLException SQL例外
     */
    public ArrayList<SearchDto> selectUser(String userId, String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        String sql = "SELECT DISTINCT u.user_id, u.name, u.display_name " +
                "FROM slick_user u " +
                "JOIN workspace_user wu ON u.workspace_id = wu.workspace_id " +
                "WHERE wu.user_id = ? " + // ログインユーザーが参加しているworkspace
                "AND (u.name LIKE ? OR u.display_name LIKE ?) " +
                "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("user");
                    dto.setTargetId(rs.getString("user_id"));
                    // 画面表示用に display_name を優先。無ければ name でも可。
                    dto.setDisplayName(rs.getString("display_name"));
                    dto.setDetail(rs.getString("name"));
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * 自分が参加しているチャンネルのみを検索。
     *
     * @param userId  ログインユーザーID
     * @param keyword チャンネル名のキーワード
     * @return 検索結果リスト（最大10件）
     * @throws SQLException SQL例外
     */
    public ArrayList<SearchDto> selectChannel(String userId, String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        String sql = "SELECT c.channel_id, c.name " +
                "FROM channel c " +
                "JOIN channel_user cu ON c.channel_id = cu.channel_id " +
                "WHERE cu.user_id = ? " + // 参加チャンネルのみ
                "AND c.name LIKE ? " +
                "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, "%" + keyword + "%");

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
     * 自分が参加しているチャンネル内のメッセージを検索。
     * メッセージに投稿者やチャンネル情報を付けたい場合はSELECT項目にJOINした列を追加します。
     *
     * @param userId  ログインユーザーID
     * @param keyword メッセージ本文のキーワード
     * @return 検索結果リスト（最大10件）
     * @throws SQLException SQL例外
     */
    public ArrayList<SearchDto> selectChannelMessage(String userId, String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        String sql = "SELECT m.message_id, m.message_text, c.name AS channel_name " +
                "FROM message m " +
                "JOIN channel c         ON m.channel_id = c.channel_id " +
                "JOIN channel_user cu   ON c.channel_id = cu.channel_id " +
                "WHERE cu.user_id = ? " + // ログインユーザーが参加中
                "AND m.message_text LIKE ? " +
                "ORDER BY m.created_at DESC " + // 新しいメッセージを優先
                "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("message");
                    dto.setTargetId(rs.getString("message_id"));
                    dto.setDisplayName(rs.getString("message_text"));
                    // チャンネル名をdetailに付与
                    dto.setDetail("チャンネル: " + rs.getString("channel_name"));
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * 自分が参加しているDM内のメッセージを検索。
     *
     * @param userId  ログインユーザーID
     * @param keyword DMメッセージのキーワード
     * @return 検索結果リスト（最大10件）
     * @throws SQLException SQL例外
     */
    public ArrayList<SearchDto> selectDmMessage(String userId, String keyword) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        String sql = "SELECT dm_msg.dm_message_id, dm_msg.message_text " +
                "FROM dm_message dm_msg " +
                "JOIN dm_user du ON dm_msg.dm_id = du.dm_id " +
                "WHERE du.user_id = ? " + // 参加DMのみ
                "AND dm_msg.message_text LIKE ? " +
                "ORDER BY dm_msg.created_at DESC " +
                "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SearchDto dto = new SearchDto();
                    dto.setType("message");
                    dto.setTargetId(rs.getString("dm_message_id"));
                    dto.setDisplayName(rs.getString("message_text"));
                    dto.setDetail("DMメッセージ");
                    list.add(dto);
                }
            }
        }

        return list;
    }

    /**
     * 検索履歴を取得（新しい順で最大10件）。
     *
     * @param userId ログインユーザーID
     * @return 検索履歴リスト
     * @throws SQLException SQL例外
     */
    public ArrayList<SearchDto> selectHistory(String userId) throws SQLException {
        ArrayList<SearchDto> list = new ArrayList<>();
        Connection conn = DatabaseConnector.getConnection();

        String sql = "SELECT search_id, search_text " +
                "FROM search " +
                "WHERE user_id = ? " +
                "ORDER BY created_at DESC " +
                "LIMIT 10";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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