package logic.tasks;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.imageio.IIOException;
import java.lang.invoke.ConstantCallSite;
import java.nio.file.FileAlreadyExistsException;
import java.util.function.Consumer;

public class CommitTask extends Task<Boolean> {

    private Engine engine;
    private String message;
    private Consumer<Commit> commitConsumer;

    public CommitTask(Engine engine,String message,Consumer<Commit> commitConsumer){
        this.engine = engine;
        this.message = message;
        this.commitConsumer = commitConsumer;
    }
    @Override
    protected Boolean call() throws FileAlreadyExistsException,java.io.IOException {
        engine.createNewCommit(this.message);
            Platform.runLater(
                    () -> commitConsumer.accept(engine.GetCurrentRepository().GeCurrentCommit())
            );

        return Boolean.TRUE;
    }
}
