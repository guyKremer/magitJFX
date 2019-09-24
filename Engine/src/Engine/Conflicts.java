package Engine;

import java.nio.file.Path;

public class Conflicts {
    private Path filePath;
    private String oursContent;
    private String theirsContent;
    private String parentContent;

    public Conflicts(Path filePath,String parentContent, String oursContent, String theirsContent) {
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
}
