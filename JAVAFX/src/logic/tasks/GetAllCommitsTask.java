package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import javafx.concurrent.Task;

import java.util.List;

public class GetAllCommitsTask extends Task<Boolean> {

    private Engine engine;
    private
    public GetAllCommitsTask(Engine engine){
        this.engine=engine;
    }
    @Override
    protected Boolean call() throws Exception {
        List<Commit> commits = engine.GetAllCommits();

    }
}
