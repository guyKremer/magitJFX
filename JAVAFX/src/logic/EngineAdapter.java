package logic;

import Engine.Engine;
import javafx.concurrent.Task;
import logic.tasks.ChangeUserNameTask;

import java.util.function.Consumer;

public class EngineAdapter {

    private Engine engine = new Engine();

    private Task<Boolean> currentRunningTask;

    public void ChangeUserName(String name, Consumer<String> inputDelegate){
        currentRunningTask = new ChangeUserNameTask(engine, name, inputDelegate);
        new Thread(currentRunningTask).start();
    }
}
