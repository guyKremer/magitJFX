package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;
import Engine.Status;

import javax.imageio.IIOException;
import java.lang.invoke.ConstantCallSite;
import java.nio.file.FileAlreadyExistsException;
import java.util.function.Consumer;

public class CommitTask extends Task<Boolean> {

    private Engine engine;
    private String message;
    private Consumer<Commit> commitConsumer;
    private Consumer<Status> statusConsumer;

    public CommitTask(Engine engine,String message,Consumer<Commit> commitConsumer,Consumer<Status> statusConsumer){
        this.engine = engine;
        this.message = message;
        this.commitConsumer = commitConsumer;
        this.statusConsumer=statusConsumer;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }
    @Override
    protected Boolean call() throws FileAlreadyExistsException,java.io.IOException {
        engine.createNewCommit(this.message);
        Commit createdCommit = engine.GetCurrentRepository().GeCurrentCommit();
        Status status = engine.showStatusAgainstOtherCommits(createdCommit,createdCommit.getFirstPrecedingSha1());
            Platform.runLater(
                    () -> {commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit());
                            statusConsumer.accept(status);
                    }
            );

        return Boolean.TRUE;
    }
}
