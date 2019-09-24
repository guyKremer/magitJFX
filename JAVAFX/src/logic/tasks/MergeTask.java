package logic.tasks;

import Engine.Engine;
import javafx.concurrent.Task;

public class MergeTask extends Task<Boolean> {

    private Engine engine;
    private String theirsBranchName;

    public MergeTask(Engine engine,String theirsBranchName){
        this.engine=engine;
        this.theirsBranchName=theirsBranchName;
    }

    @Override
    protected Boolean call() throws Exception {
        engine.CheckConflicts(this.theirsBranchName);
        //engine.Merge(this.theirsBranchName);
        return Boolean.TRUE;
    }
}
