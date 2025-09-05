package yuri.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yuri.Yuri;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        Scene scene = new Scene(loader.load());
        MainWindow controller = loader.getController();
        controller.setYuri(new Yuri()); // <-- inject backend here

        stage.setTitle("Yuri");
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/view/app-icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
