package logic.tasks;

import Engine.Engine;
import Engine.Conflict;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.Map;

public class CheckConflictsTask extends Task<Map<Path, Conflict>> {

    private Engine engine;
    private String theirsBranchName;

    public CheckConflictsTask(Engine engine, String theirsBranchName){
        this.engine=engine;
        this.theirsBranchName=theirsBranchName;
    }

    @Override
    protected Map<Path, Conflict> call() throws Exception {
        return engine.CheckConflicts(this.theirsBranchName);
    }
}
