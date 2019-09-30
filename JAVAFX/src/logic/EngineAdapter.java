package logic;

import Engine.Engine;
import Engine.*;
import Engine.MagitObjects.Branch;
import Engine.MagitObjects.Commit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import logic.tasks.*;
import org.apache.commons.io.FileUtils;
import sun.awt.AWTAccessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EngineAdapter {

    private Engine engine = new Engine();

    private Task<Boolean> currentRunningTask;
    public static Consumer<Throwable> throwableConsumer = createThrowableConsumer();

    private static Consumer<Throwable> createThrowableConsumer () {
        return (throwable) -> {
            Platform.runLater(() -> {
                System.out.println("sdfs");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(throwable.getMessage());
                alert.showAndWait();
            });
        };
    }

    public void ChangeUserName(String name, Consumer<String> inputDelegate){
        currentRunningTask = new ChangeUserNameTask(engine, name, inputDelegate);
        new Thread(currentRunningTask).start();
    }

    public void LoadFromXml(File file, BiConsumer<String,String> repDetailsDelegate) throws InterruptedException {
        currentRunningTask = new LoadFromXmlTask(engine, file.getPath(), repDetailsDelegate);
        Thread t = new Thread(currentRunningTask);
        t.start();
        t.join();
    }

    public void CreateNewRepo(String path,String repoName, BiConsumer<String, String> repDetailsDelegate) {
        currentRunningTask = new CreateNewRepoTask(engine, path ,repoName, repDetailsDelegate);
        Thread currentThread = new Thread(currentRunningTask);
        currentThread.start();
    }

    public Engine getEngine() {
        return engine;
    }
    public void SwitchRepo(String path, BiConsumer<String, String> repDetailsDelegate,Consumer<Commit> commitConsumer) throws InterruptedException {
        currentRunningTask = new SwitchRepoTask(engine, path, repDetailsDelegate,commitConsumer);
        Thread t = new Thread(currentRunningTask);
        t.start();
        t.join();
    }

    public void showAllBranches() throws IOException {
        try{
            Map<String, Branch> branches = engine.GetRepoBranches();
            String str = getBranchesDetails(branches);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Repository Branches");
            alert.setHeaderText("Repository Branches");
            alert.setContentText(str);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.showAndWait();
        }
        catch (Exception e){
            throwableConsumer.accept(e);
        }
    }

    private String getBranchesDetails(Map<String, Branch> i_repoBranches) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Branch head = engine.GetHeadBranch();
        for(String key: i_repoBranches.keySet()){
            stringBuilder.append(getBranchDetails(i_repoBranches.get(key),head.getName()));
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    private StringBuilder getBranchDetails(Branch i_branch, String i_headBranchName) throws IOException {
        StringBuilder str = new StringBuilder();
        if (i_branch.getName().equals(i_headBranchName)) {
            str.append("-->");
        }
        str.append("Name: ").append(i_branch.getName()).
                append(System.lineSeparator()).
                append("Pointed Commit: ").append(i_branch.getCommitSha1());
        try{
            str.append(System.lineSeparator());
            String commitMsg= i_branch.getCommitMsg();
            str.append("Commit Message: ").append(commitMsg);
            str.append(System.lineSeparator());
        }
        catch (FileNotFoundException e){
            str.append(" Commit Message: ").append("Nothing has been Committed yet");
        }

        str.append(System.lineSeparator());

        return str;
    }

    public void createNewBranch(String branchName, boolean checkout) {
        currentRunningTask = new CreateNewBranchTask(engine, branchName, checkout);
        new Thread(currentRunningTask).start();
    }


    public void checkout(String branchName,Consumer<Commit>commitConsumer) {
        currentRunningTask = new CheckoutTask(engine, branchName,commitConsumer);
        new Thread(currentRunningTask).start();
    }


    public Map<Path,Conflict> Merge (String theirsBranchName,Consumer<Commit> commitConsumer,boolean checkConflicts)throws FileAlreadyExistsException , IOException{
            Map<Path,Conflict> conflicts = new HashMap<>();
            int fastMergeType = engine.needFastForwardMerge(theirsBranchName);
            if(fastMergeType == 1){
                 engine.forwardMerge(theirsBranchName);
                 commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit());
                 return conflicts;
            }
            else if (fastMergeType == 2){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("information");
                alert.setHeaderText("No Merge Needed");
                alert.setContentText("no merge needed because " + theirsBranchName  +" last commit is contained inside " +engine.GetHeadBranch().getName() + " last commit");

                alert.showAndWait();
                return conflicts;
            }
            else{
                conflicts = engine.Merge(theirsBranchName,checkConflicts);
                if(conflicts.isEmpty()){
                    commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit());
                }
                return conflicts;
            }

    }


    public void Commit(String message, Consumer<Commit> commitConsumer) {
        currentRunningTask = new CommitTask(engine,message,commitConsumer);
        new Thread(currentRunningTask).start();
    }
}
