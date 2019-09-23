package components.center;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import puk.team.course.magit.ancestor.finder.*;

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Circle CommitCircle;
    @FXML private Label branchLabel;

    public int getMessageLabel()
    {
        return (int) messageLabel.getLayoutX() + 110;
    }

    public void setCommitTimeStamp(String timeStamp)
    {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName)
    {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage)
    {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }


    public void setBranchName(String branchName)
    {
        branchLabel.setText(branchName);
        branchLabel.setTooltip(new Tooltip(branchName));
    }
}
