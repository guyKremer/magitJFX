package logic.tasks;

import Engine.Engine;
import Engine.Conflict;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.Map;

public class MergeTask extends Task<Boolean> {

    private Engine engine;
    private String theirsBranchName;

    public MergeTask(Engine engine,String theirsBranchName){
        this.engine=engine;
        this.theirsBranchName=theirsBranchName;
    }

    @Override
    protected Boolean call() throws Exception {
        Map<Path, Conflict> temp = engine.CheckConflicts(this.theirsBranchName);
        for (Map.Entry<Path,Conflict> entry : temp.entrySet()){
            System.out.println(entry.getValue().getOursContent());
        }
        //engine.Merge(this.theirsBranchName);
        return Boolean.TRUE;
    }
}
