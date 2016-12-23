package purejavaxbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class StickApp extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        URL url = getClass().getResource("/purejavaxbox/StickApp.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        Parent content = loader.load();

        primaryStage.setScene(new Scene(content, 400, 400));
        primaryStage.show();
    }
}
