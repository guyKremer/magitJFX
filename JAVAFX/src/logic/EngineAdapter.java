package logic;

import Engine.Engine;
import Engine.*;
import Engine.MagitObjects.Branch;
import javafx.beans.binding.BooleanExpression;
import javafx.concurrent.Task;
import logic.tasks.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EngineAdapter {

    private Engine engine = new Engine();

    private Task<Boolean> currentRunningTask;

    public void ChangeUserName(String name, Consumer<String> inputDelegate){
        currentRunningTask = new ChangeUserNameTask(engine, name, inputDelegate);
        new Thread(currentRunningTask).start();
    }

    public void LoadFromXml(File file, BiConsumer<String,String> repDetailsDelegate){
        currentRunningTask = new LoadFromXmlTask(engine, file.getPath(), repDetailsDelegate);
        new Thread(currentRunningTask).start();
    }

    public void CreateNewRepo(String path,String repoName, BiConsumer<String, String> repDetailsDelegate) {
        currentRunningTask = new CreateNewRepoTask(engine, path ,repoName, repDetailsDelegate);
        new Thread(currentRunningTask).start();
    }

    public void SwitchRepo(String path, BiConsumer<String, String> repDetailsDelegate) {
        currentRunningTask = new SwitchRepoTask(engine, path, repDetailsDelegate);
        new Thread(currentRunningTask).start();
    }

    public String showAllBranches() throws IOException {
        Map<String, Branch> branches = engine.GetRepoBranches();
        String str = getBranchesDetails(branches);
        return str;
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

    public void checkout(String branchName) {
        currentRunningTask = new CheckoutTask(engine, branchName);
        new Thread(currentRunningTask).start();
    }

    public boolean checkChangesBeforeOperation() throws IOException {
        Boolean res = false;
        Status status = engine.showStatus();
        if (!status.getModifiedFiles().isEmpty() || !status.getAddedFiles().isEmpty()
                || !status.getDeletedFiles().isEmpty()){
            res = true;
        }

        return res;
    }
}
