package components.center;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import puk.team.course.magit.ancestor.finder.*;

import java.io.IOException;
import java.net.URL;

public class CommitNode extends AbstractCell {
    private String timestamp;
    private String committer;
    private String message;
    private String branchName;
    private CommitNodeController commitNodeController;

    public CommitNode(String timestamp, String committer, String message)
    {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
    }

    @Override
    public Region getGraphic(Graph graph)
    {
        try
        {

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);
            commitNodeController.setBranchName(branchName);

            return root;
        } catch (IOException e)
        {
            return new Label("Error when tried to create graphic node !");
        }
    }

    public void SetBranchName(String i_BranchesString)
    {
        this.branchName = appendArrowTobranch(i_BranchesString);
    }

    private String appendArrowTobranch(String i_BranchesString)
    {
        return i_BranchesString + "  " + "→";
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge)
    {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getMessageLabel());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode()
    {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
