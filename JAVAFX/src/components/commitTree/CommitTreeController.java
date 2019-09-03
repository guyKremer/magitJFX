package components.commitTree;

import Engine.MagitObjects.Commit;
import components.center.CenterController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.control.ScrollPane;

public class CommitTreeController {
    @FXML private ScrollPane commitTreeScrollPane;
    @FXML private GridPane commitTreeGridPane;
    @FXML private GridPane commitData;

    private int gridRows;
    private int gridCols;

    private CenterController parentController;

    public void setMainController(CenterController centerController) {
        parentController=centerController;
    }

    public void addCommit(Commit commit){

        commitData.add(new Text("blassfgd"),0,0);
    }
}
