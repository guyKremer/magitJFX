package logic.tasks;

import Engine.Engine;
import javafx.concurrent.Task;

public class ResetBranchTask extends Task<Boolean> {

    private Engine engine;
    private String sha1;

    public ResetBranchTask(Engine engine, String sha1) {
        this.engine = engine;
        this.sha1 = sha1;
    }


    @Override
    protected Boolean call() throws Exception{
        engine.resetBranchSha1(engine.GetCurrentRepository().GetHeadBranch().getName(), sha1);

        //here run later

        return Boolean.TRUE;
    }
}
