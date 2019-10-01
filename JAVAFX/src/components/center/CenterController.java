package components.center;
import Engine.MagitObjects.Branch;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.RBranch;
import Engine.MagitObjects.RTBranch;
import Engine.Status;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import com.fxgraph.graph.Graph;
import components.app.AppController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.scene.text.TextFlow;
import logic.EngineAdapter;

public class CenterController {

    private Map<Commit, ICell> m_MapCommitToIcell = new HashMap<>();
    private Graph m_TreeGraph;

    @FXML private Text userName;
    @FXML private Text repoName;
    @FXML private Text repoPath;
    @FXML private ScrollPane m_TreeScrollPane;

    @FXML private Text authorText;
    @FXML private Text dateText;
    @FXML private Text commitSha1Text;
    @FXML private Text parent1Sha1Text;
    @FXML private Text parent2Sha1Text;
    @FXML private TextArea commitMsg;

    //need to add logic to engine
    @FXML private TextFlow changedFiles;
    @FXML private TextFlow addedFiles;
    @FXML private TextFlow deletedFiles;

    private AppController mainController;
    private Consumer<Throwable> throwableConsumer;
    private Consumer<Commit> commitConsumer = commit -> {
        authorText.textProperty().set(commit.getCreator());
        dateText.textProperty().set(commit.getDateOfCreation());
        commitSha1Text.textProperty().set(commit.getSha1());
        parent1Sha1Text.textProperty().set(commit.getFirstPrecedingSha1());
        parent2Sha1Text.textProperty().set(commit.getSecondPrecedingSha1());
        commitMsg.textProperty().set(commit.getMessage());

    };

    public void setThrowableConsumer(Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = throwableConsumer;
    }

    public Text getAuthorText() {
        return authorText;
    }

    public Text getParent1Sha1Text() {
        return parent1Sha1Text;
    }

    public Text getParent2Sha1Text() {
        return parent2Sha1Text;
    }

    public Text getDateText() {
        return dateText;
    }

    public TextArea getCommitMsg() {
        return commitMsg;
    }

    public Text getCommitSha1Text() {
        return commitSha1Text;
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public void ResetCommitsTree() throws IOException {
        m_TreeGraph = new Graph();

        initComponentsInTree();

        PannableCanvas canvas = m_TreeGraph.getCanvas();
        Platform.runLater(() -> {
            m_TreeGraph.getUseViewportGestures().set(false);
            m_TreeGraph.getUseNodeGestures().set(false);
        });
        m_TreeScrollPane.setContent(canvas);
    }

    private void initComponentsInTree() throws IOException {
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
        if (!i_Commit.getFirstPrecedingSha1().isEmpty())
        {
            final Edge edgeFirstPrev = new Edge(
                    m_MapCommitToIcell.get(i_Commit),
                    m_MapCommitToIcell.get(new Commit(i_Commit.getFirstPrecedingSha1())));
            i_Model.addEdge(edgeFirstPrev);
        }

        if (!i_Commit.getSecondPrecedingSha1().isEmpty())
        {
            final Edge edgeSecondPrev = new Edge(m_MapCommitToIcell.get(i_Commit), m_MapCommitToIcell
                    .get(new Commit(i_Commit.getSecondPrecedingSha1())));
            i_Model.addEdge(edgeSecondPrev);
        }
    }

    private void createCommits() throws IOException {
        //System.out.println(mainController.getEngineAdapter().getEngine().GetCurrentRepository());
        Map<String , Commit> commitsMap;
        commitsMap = mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetCommitsMap();
        commitsMap
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

                    m_TreeGraph.getGraphic(commitNode).setOnMouseClicked(event ->
                            onCommitNodeClicked(commit));


                    m_TreeGraph.getModel().addCell(commitNode);

                    m_MapCommitToIcell.put(commit, commitNode);
                });

    }

    private void onCommitNodeClicked(Commit i_commit){
        authorText.setText(i_commit.getCreator());
        dateText.setText(i_commit.getDateOfCreation());
        commitSha1Text.setText(i_commit.getSha1());
        if(!i_commit.getFirstPrecedingSha1().isEmpty()) {
            parent1Sha1Text.setText(i_commit.getFirstPrecedingSha1());
        }
        else{
            parent1Sha1Text.setText("");
        }
        if(!i_commit.getSecondPrecedingSha1().isEmpty()){
            parent2Sha1Text.setText(i_commit.getSecondPrecedingSha1());
        }
        else{
            parent2Sha1Text.setText("");
        }

        commitMsg.setText(i_commit.getMessage());

        Consumer<Status> statusConsumer = (status)-> {
            changedFiles.getChildren().clear();
            addedFiles.getChildren().clear();
            deletedFiles.getChildren().clear();
            for (String str : status.getModifiedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                changedFiles.getChildren().add(new Text("- " +str));
                changedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
            for (String str : status.getAddedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                addedFiles.getChildren().add(new Text("- " +str));
                addedFiles.getChildren().add(new Text(System.lineSeparator()));

            }
            for (String str : status.getDeletedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                deletedFiles.getChildren().add(new Text("- " +str));
                deletedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
        };

        mainController.getEngineAdapter().ShowStatus(i_commit, statusConsumer, i_commit.getFirstPrecedingSha1());
    }

    private String appendBranchNames(List<Branch> i_BranchesOfCommit)
    {
        List<String> branchNames = i_BranchesOfCommit
                .stream()
                .map(branch ->
                {
                    StringBuilder currentBranch = new StringBuilder();
                    if (mainController.getEngineAdapter().getEngine().GetHeadBranch().getName().equals(branch.getName()))
                        currentBranch.append("--->");

                    if (branch.getClass().equals(RBranch.class))
                        currentBranch.append("(RB)");

                    if (branch.getClass().equals(RTBranch.class))
                        currentBranch.append("(RTB)");

                    return currentBranch.append(branch.getName()).toString();
                }).collect(Collectors.toList());

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

    public void loadFromXml(File file) throws InterruptedException {
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

    public void switchRepo(String path) throws IOException, InterruptedException {
        BiConsumer<String,String> biConsumer = (a,b)->{
            repoName.textProperty().set(a);
            repoPath.textProperty().set(b);
        };
        mainController.getEngineAdapter().SwitchRepo(path,biConsumer,commitConsumer);
    }

    public void createNewBranch(String branchName, boolean checkout) {
        mainController.getEngineAdapter().createNewBranch(branchName, checkout);
    }

    public void checkout(String branchName) throws InterruptedException {
        mainController.getEngineAdapter().checkout(branchName,commitConsumer);
    }

    public void Commit(String message) throws InterruptedException {
        Consumer<Status> statusConsumer = (status)-> {
            changedFiles.getChildren().clear();
            addedFiles.getChildren().clear();
            deletedFiles.getChildren().clear();
            for (String str : status.getModifiedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                changedFiles.getChildren().add(new Text("- " +str));
                changedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
            for (String str : status.getAddedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                addedFiles.getChildren().add(new Text("- " +str));
                addedFiles.getChildren().add(new Text(System.lineSeparator()));

            }
            for (String str : status.getDeletedFiles()) {
                if(str.equals(mainController.getEngineAdapter().getEngine().GetCurrentRepository().GetRepositoryPath().toString())){
                    continue;
                }
                deletedFiles.getChildren().add(new Text("- " +str));
                deletedFiles.getChildren().add(new Text(System.lineSeparator()));
            }
        };
        mainController.getEngineAdapter().Commit(message,commitConsumer,statusConsumer);
    }

    public void resetBranch(String sha1) {
        mainController.getEngineAdapter().ResetBranch(sha1,commitConsumer);
    }
}
