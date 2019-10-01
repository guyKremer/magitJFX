package logic.tasks;

import Engine.Engine;
import javafx.concurrent.Task;
import logic.EngineAdapter;

public class CreateNewBranchTask extends Task<Boolean> {

    private Engine engine;
    private String branchName;
    private boolean checkout;

    public CreateNewBranchTask(Engine engine, String branchName, boolean checkout) {
        this.engine = engine;
        this.branchName = branchName;
        this.checkout = checkout;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }

    @Override
    protected Boolean call() throws Exception {
        engine.AddBranch(branchName, checkout);
        //need run later to commits tree
        return Boolean.TRUE;
    }
}
