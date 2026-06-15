package dto.search;

/**
 * 検索結果をJSONで返すためのDTO
 */
public class SearchDto {

    private String type; // user / channel / message / history
    private String targetId; // 遷移先ID
    private String displayName; // 画面に表示する名前
    private String detail; // 補足情報

    public SearchDto() {
    }

    public SearchDto(String type, String targetId, String displayName, String detail) {
        this.type = type;
        this.targetId = targetId;
        this.displayName = displayName;
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDetail() {
        return detail;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}