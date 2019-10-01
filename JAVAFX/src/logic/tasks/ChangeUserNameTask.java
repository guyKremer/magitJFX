package logic.tasks;

import Engine.Engine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;

import java.util.function.Consumer;

public class ChangeUserNameTask extends Task<Boolean> {

    private Engine engine;
    private Consumer<String> inputDelegate;
    private String name;

    public ChangeUserNameTask(Engine engine, String name,  Consumer<String> inputDelegate){
        this.engine = engine;
        this.name = name;
        this.inputDelegate = inputDelegate;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }

    @Override
    protected Boolean call() throws Exception {
        engine.m_user = name;

        Platform.runLater(
                () -> inputDelegate.accept(engine.m_user)
        );
        return Boolean.TRUE;
    }
}
