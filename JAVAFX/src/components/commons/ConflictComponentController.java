package components.commons;

import Engine.Conflict;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ConflictComponentController {
    @FXML private TextArea ancestor;
    @FXML private TextArea ours;
    @FXML private TextArea theirs;
    @FXML private TextArea result;

    private Conflict conflict;

    public void setAncestor(TextArea ancestor) {
        this.ancestor = ancestor;
    }

    public void setOurs(TextArea ours) {
        this.ours = ours;
    }

    public void setTheirs(TextArea theirs) {
        this.theirs = theirs;
    }

    public void setResult(TextArea result) {
        this.result = result;
    }

    public void setConflict(Conflict conflict) {
        this.conflict = conflict;
    }
}
