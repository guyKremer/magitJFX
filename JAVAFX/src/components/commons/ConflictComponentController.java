package components.commons;

import Engine.Conflict;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOException;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class ConflictComponentController {
    @FXML private TextArea ancestor;
    @FXML private TextArea ours;
    @FXML private TextArea theirs;
    @FXML private TextArea result;
    @FXML private Button save;
    @FXML private RadioButton delete;

    private Conflict conflict;
    private Stage conflictStage;
    private Consumer<Conflict> conflictConsumer;

    public void setAncestorText(String ancestor) {
        this.ancestor.textProperty().set(ancestor);

    }

    public void setOursText(String ours) {
        this.ours.textProperty().set(ours);
    }

    public void setTheirsText(String theirs) {
        this.theirs.textProperty().set(theirs);
    }

    public void setResultText(String result) {
        this.result.textProperty().set(result);
    }

    public void setConflict(Conflict conflict) {
        this.conflict = conflict;
        setAncestorText(conflict.getParentContent());
        setOursText(conflict.getOursContent());
        setTheirsText(conflict.getTheirsContent());
    }

    public void setConflictConsumer(Consumer<Conflict> conflictConsumer) {
        this.conflictConsumer = conflictConsumer;
    }

    @FXML
    public void saveActionListener(ActionEvent event)throws IOException {
        if(delete.isSelected()){
            FileUtils.deleteQuietly(this.conflict.getFilePath().toFile());
        }
        else if(!result.getText().isEmpty()){
            FileUtils.writeStringToFile(this.conflict.getFilePath().toFile(), result.getText(), Charset.forName("utf-8"),false);
        }

        this.conflictConsumer.accept(this.conflict);
        conflictStage.close();
    }

    public void setStage(Stage conflictStage) {
        this.conflictStage = conflictStage;
    }
}
