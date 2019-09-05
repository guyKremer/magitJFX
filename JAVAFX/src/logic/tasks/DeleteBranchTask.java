package logic.tasks;

import Engine.Engine;
import javafx.concurrent.Task;

public class DeleteBranchTask extends Task<Boolean> {

    private Engine engine;
    private String branchName;

    public DeleteBranchTask(Engine engine, String branchName) {
        this.engine = engine;
        this.branchName = branchName;
    }

    @Override
    protected Boolean call() throws Exception {
        engine.DeleteBranch(branchName);

        //run later here

        return Boolean.TRUE;
    }
}
