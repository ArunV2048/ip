package yuri.gui;

import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * Dialog bubble with a circular avatar image and a text label.
 * Left-aligned for Yuri, right-aligned for the user.
 */
public class DialogBox extends HBox {

    private static final double AVATAR_SIZE = 36.0;

    private static final Image USER_AVATAR = loadImage("/view/user.png");
    private static final Image YURI_AVATAR = loadImage("/view/yuri.png");

    private static Image loadImage(String path) {
        InputStream is = DialogBox.class.getResourceAsStream(path);
        if (is == null) {
            // Fail fast with a clear message so asset issues are obvious during dev
            throw new IllegalStateException("Missing required image on classpath: " + path);
        }
        return new Image(is);
    }

    private DialogBox(String text, Image avatar, boolean isUser) {
        setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        setSpacing(8);
        setPadding(new Insets(6));

        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(AVATAR_SIZE);
        imageView.setFitHeight(AVATAR_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Circle clip = new Circle(AVATAR_SIZE / 2, AVATAR_SIZE / 2, AVATAR_SIZE / 2);
        imageView.setClip(clip);

        Label label = new Label(text);
        label.setWrapText(true);
        label.setPadding(new Insets(8, 12, 8, 12));
        label.setStyle(isUser
                ? "-fx-background-color: #dbeafe; -fx-background-radius: 10; -fx-font-size: 13px;"
                : "-fx-background-color: #f1f3f5; -fx-background-radius: 10; -fx-font-size: 13px;");

        if (isUser) {
            // Right side: label then avatar (RTL trick not needed with ordering)
            getChildren().addAll(label, imageView);
        } else {
            // Left side: avatar then label
            getChildren().addAll(imageView, label);
        }
    }

    /**
     * Returns a dialog styled as a user message (right-aligned).
     *
     * @param text message text
     * @return dialog node
     */
    public static DialogBox getUserDialog(String text) {
        return new DialogBox(text, USER_AVATAR, true);
    }

    /**
     * Returns a dialog styled as a Yuri message (left-aligned).
     *
     * @param text message text
     * @return dialog node
     */
    public static DialogBox getYuriDialog(String text) {
        return new DialogBox(text, YURI_AVATAR, false);
    }
}
