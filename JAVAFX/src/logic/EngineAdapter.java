package logic;

import Engine.Engine;
import javafx.concurrent.Task;
import logic.tasks.ChangeUserNameTask;
import logic.tasks.CreateNewRepoTask;
import logic.tasks.LoadFromXmlTask;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EngineAdapter {

    private Engine engine = new Engine();

    private Task<Boolean> currentRunningTask;

    public void ChangeUserName(String name, Consumer<String> inputDelegate){
        currentRunningTask = new ChangeUserNameTask(engine, name, inputDelegate);
        new Thread(currentRunningTask).start();
    }

    public void LoadFromXml(File file, BiConsumer<String,String> repDetailsDelegate){
        currentRunningTask = new LoadFromXmlTask(engine, file.getPath(), repDetailsDelegate);
        new Thread(currentRunningTask).start();
    }

    public void CreateNewRepo(String path,String repoName, BiConsumer<String, String> repDetailsDelegate) {
        currentRunningTask = new CreateNewRepoTask(engine, path ,repoName, repDetailsDelegate);
        new Thread(currentRunningTask).start();
    }
}
