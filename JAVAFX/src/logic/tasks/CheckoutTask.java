package logic.tasks;

import Engine.Engine;
import javafx.concurrent.Task;

public class CheckoutTask extends Task<Boolean> {
    private Engine engine;
    private String branchName;

    public CheckoutTask(Engine engine, String branchName) {
        this.engine = engine;
        this.branchName = branchName;
    }

    @Override
    protected Boolean call() throws Exception {
        engine.checkOut(branchName);

        //run later here
        return Boolean.TRUE;
    }
}
