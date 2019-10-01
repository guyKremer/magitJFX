package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;
import Engine.Status;

import java.util.function.Consumer;

public class ShowStatusTask extends Task<Boolean>{

    private Engine engine;
    private Consumer<Status> statusConsumer;
    private Commit commit;
    private String prevCommitSha1;


    public ShowStatusTask(Engine engine, Consumer<Status> statusConsumer,Commit commit,String prevCommitSha1) {
        this.engine = engine;
        this.statusConsumer=statusConsumer;
        this.commit =commit;
        this.prevCommitSha1 = prevCommitSha1;

        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }

    @Override
    protected Boolean call() throws Exception {
        Status status;
            //status calculated against the wc
                if(this.commit == null) {
                     status = engine.showStatus();
                    Platform.runLater(()->{
                        statusConsumer.accept(status);
                    });

                }
                //status calculated against prev commit
                else{
                    if(prevCommitSha1!=null) {
                        status = engine.showStatusAgainstOtherCommits(this.commit, prevCommitSha1);
                        Platform.runLater(()->{
                            statusConsumer.accept(status);
                        });
                    }
                }

        return Boolean.TRUE;
    }
}

