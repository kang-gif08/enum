package dto;

/**
 * 検索サジェスト情報を表すDTO
 */
public class SearchSuggestionDto {

    private String type; // USER / CHANNEL / MESSAGE / HISTORY
    private String title; // 一覧に表示する文字列
    private String content; // メッセージの場合などの補足

    public SearchSuggestionDto(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}