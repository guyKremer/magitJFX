package components.center;

import Engine.MagitObjects.Branch;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.FolderItems.FolderItem;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import com.fxgraph.graph.Graph;
import components.app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.scene.text.TextFlow;
import puk.team.course.magit.ancestor.finder.*;

public class CenterController {

    private Map<Commit, ICell> m_MapCommitToIcell = new HashMap<>();
    private Graph m_TreeGraph;

    @FXML private Text userName;
    @FXML private Text repoName;
    @FXML private Text repoPath;
    @FXML private ScrollPane m_TreeScrollPane;
    @FXML private Text author;
    @FXML private Text date;
    @FXML private Text commitSHA1;
    @FXML private TextArea commitParents;

    //need to add logic to engine
    @FXML private TextFlow changedFiles;
    @FXML private TextFlow addedFiles;
    @FXML private TextFlow deletedFiles;
    @FXML private Text authorText;
    @FXML private Text dateText;
    @FXML private Text commitSha1Text;
    @FXML private Text parent1Sha1Text;
    @FXML private Text parent2Sha1Text;

    private AppController mainController;

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void ResetCommitsTree()
    {
        m_TreeGraph = new Graph();

        initComponentsInTree();

        PannableCanvas canvas = m_TreeGraph.getCanvas();
        m_TreeScrollPane.setContent(canvas);
    }

    private void initComponentsInTree()
    {
        m_TreeGraph.beginUpdate();

        createCommits();
        addEdgesToModel(m_TreeGraph.getModel());

        m_TreeGraph.endUpdate();
        m_TreeGraph.layout(new CommitTreeLayout(m_MapCommitToIcell));
    }

    private void addEdgesToModel(Model i_Model)
    {
        m_MapCommitToIcell
                .keySet()
                .stream()
                .forEach(commit -> {
                    try {
                        addEdgesToCommit(commit, i_Model);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }


    //code duplicate.. wrap to one function
    private void addEdgesToCommit(Commit i_Commit, Model i_Model) throws IOException {
        if (i_Commit.getFirstPrecedingSha1() != "")
        {
            final Edge edgeFirstPrev = new Edge(m_MapCommitToIcell
                    .get(i_Commit), m_MapCommitToIcell.get(new Commit(i_Commit.getFirstPrecedingSha1())));
            i_Model.addEdge(edgeFirstPrev);
        }

        if (i_Commit.getSecondPrecedingSha1() != "")
        {
            final Edge edgeSecondPrev = new Edge(m_MapCommitToIcell.get(i_Commit), m_MapCommitToIcell
                    .get(new Commit(i_Commit.getSecondPrecedingSha1())));
            i_Model.addEdge(edgeSecondPrev);
        }
    }

    private void createCommits()
    {
        mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetCommitsMap()
                .values()
                .stream()
                .forEach(commit ->
                {
                    CommitNode commitNode = new CommitNode
                            (commit.getDateOfCreation(),
                                    commit.getCreator(),
                                    commit.getMessage());

                    List<Branch> branchesOfCommit = getBranchesPointOn(commit);
                    if (branchesOfCommit.size() != 0)
                    {
                        String branchesString = appendBranchNames(branchesOfCommit);
                        commitNode.SetBranchName(branchesString);
                    }


                    // checkkkkk
                    m_TreeGraph.getGraphic(commitNode).setOnMouseClicked(event ->
                            onCommitNodeClicked(commit));


                    m_TreeGraph.getModel().addCell(commitNode);

                    m_MapCommitToIcell.put(commit, commitNode);
                });
    }

    private void onCommitNodeClicked(Commit i_commit){
        author.setText(i_commit.getCreator());
        date.setText(i_commit.getDateOfCreation());
        commitSHA1.setText(i_commit.);
    }

    private String appendBranchNames(List<Branch> i_BranchesOfCommit)
    {
        List<String> branchNames = i_BranchesOfCommit
                .stream()
                .map(branch -> branch.getName())
                .collect(Collectors.toList());

        return String.join(", ", branchNames);
    }

    private List<Branch> getBranchesPointOn(Commit i_Commit)
    {
        return mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetBranches().values()
                .stream()
                .filter(branch ->
                    branch.getCommitSha1().equals(i_Commit.getSha1())).collect(Collectors.toList());
    }

    public void ResetHeadBranchInTree(Commit i_Commit)
    {
        m_TreeGraph.layout(new CommitTreeLayout(m_MapCommitToIcell));
    }


    public void changeUserName(String name){
        mainController.getEngineAdapter().ChangeUserName(name, userName.textProperty()::set);
    }

    public void loadFromXml(File file) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().LoadFromXml(file, biConsumer);
    }

    public void createNewRepo(String path, String repName) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().CreateNewRepo(path,repName,biConsumer);
    }

    public void switchRepo(String path) {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().SwitchRepo(path,biConsumer);
    }

    public void createNewBranch(String branchName, boolean checkout) {
        mainController.getEngineAdapter().createNewBranch(branchName, checkout);
    }

    public void checkout(String branchName) {
        mainController.getEngineAdapter().checkout(branchName);
    }

    public void Commit(String message) {
        Consumer<Commit> commitConsumer = commit -> {
            authorText.textProperty().set(commit.getCreator());
            dateText.textProperty().set(commit.getDateOfCreation());
            commitSha1Text.textProperty().set(commit.getSha1());
            parent1Sha1Text.textProperty().set(commit.getFirstPrecedingSha1());
            parent2Sha1Text.textProperty().set(commit.getSecondPrecedingSha1());
        };
        mainController.getEngineAdapter().Commit(message,commitConsumer);
    }
}
