package logic.tasks;

import Engine.Engine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import logic.EngineAdapter;
import xmlFormat.xmlUtiles;

import javax.swing.plaf.synth.SynthScrollBarUI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LoadFromXmlTask extends Task<Boolean> {

    private Engine engine;
    private BiConsumer<String,String> repDetailsDelegate;
    private String file;

    public LoadFromXmlTask(Engine engine,String file, BiConsumer<String,String> repDetailsDelegate){
        this.engine = engine;
        this.file = file;
        this.repDetailsDelegate = repDetailsDelegate;
        setOnFailed(event -> {
            EngineAdapter.throwableConsumer.accept(getException());
        });
    }

    @Override
    protected Boolean call() throws Exception {

            engine.setCurrentRepository(xmlUtiles.LoadXml(file));
            Platform.runLater(
                    () -> repDetailsDelegate.accept(engine.GetCurrentRepository().GetName(),
                            engine.GetCurrentRepository().GetRepositoryPath().toString())
            );
        return Boolean.TRUE;
    }
}

