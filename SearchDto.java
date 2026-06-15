package dto.search;

/**
 * 検索サジェスト情報を表すDTO
 */
public class SearchDto {

    private String type; // USER / CHANNEL / MESSAGE / HISTORY
    private String word; // 一覧に表示する文字列

    public SearchDto() {
    }

    public SearchDto(String type, String word) {
        this.type = type;
        this.word = word;
    }

    public String getType() {
        return type;
    }

    public String getWord() {
        return word;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWord(String word) {
        this.word = word;
    }
}