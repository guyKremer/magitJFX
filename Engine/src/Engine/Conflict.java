package Engine;

import java.nio.file.Path;

public class Conflict {
    private Path filePath;
    private String oursContent;
    private String theirsContent;
    private String parentContent;

    public Conflict(Path filePath, String parentContent, String oursContent, String theirsContent) {
        this.filePath = filePath;
        this.parentContent=parentContent;
        this.oursContent=oursContent;
        this.theirsContent=theirsContent;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getParentContent() {
        return parentContent;
    }

    public String getOursContent() {
        return oursContent;
    }

    public String getTheirsContent() {
        return theirsContent;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public void setOursContent(String oursContent) {
        this.oursContent = oursContent;
    }

    public void setParentContent(String parentContent) {
        this.parentContent = parentContent;
    }

    public void setTheirsContent(String theirsContent) {
        this.theirsContent = theirsContent;
    }
}
