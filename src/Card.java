import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.Objects;

public class Card extends StackPane {

    private final Label frontLabel;
    private final ImageView frontImage;
    private final Region back;
    private boolean revealed = false;
    private boolean matched = false;
    private final String value;
    private final double cardSize;
    private final MemoryGame game;

    public Card(String val, double size, MemoryGame gameRef) {
        value = val;
        cardSize = size;
        game = gameRef;

        frontLabel = new Label();
        frontLabel.setStyle("-fx-font-size: " + (cardSize / 2) + "px;");
        frontLabel.setVisible(false);

        frontImage = new ImageView();
        frontImage.setFitWidth(cardSize * 0.8);
        frontImage.setFitHeight(cardSize * 0.8);
        frontImage.setPreserveRatio(true);
        frontImage.setVisible(false);

        back = new Region();
        back.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #4A90E2, #6EB1FF);" +
                        " -fx-border-color: #1565c0;" +
                        " -fx-border-width: 3;" +
                        " -fx-background-radius: 12;" +
                        " -fx-border-radius: 12;"
        );

        setEffect(new DropShadow(10, Color.web("#D6A8FF")));

        getChildren().addAll(back, frontLabel, frontImage);
        setCursor(Cursor.HAND);

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> game.onCardClicked(this));
    }

    public String getValue() {
        return value;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean m) {
        matched = m;
        if (m) back.setStyle(
                "-fx-background-color: #81c784;" +
                        " -fx-border-color: #2e7d32;" +
                        " -fx-border-width: 3;" +
                        " -fx-background-radius: 12;" +
                        " -fx-border-radius: 12;"
        );
    }

    public void reveal() {
        if (revealed || matched) return;

        revealed = true;
        back.setVisible(false);

        if (isImage()) {
            try {
                Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(value)));
                frontImage.setImage(img);

                ColorAdjust brighten = new ColorAdjust();
                brighten.setBrightness(0.2);

                DropShadow shadow = new DropShadow();
                shadow.setRadius(10);
                shadow.setOffsetX(4);
                shadow.setOffsetY(4);
                shadow.setColor(Color.rgb(0, 0, 0, 0.8));

                brighten.setInput(shadow);
                frontImage.setEffect(brighten);

                frontImage.setStyle(
                        "-fx-border-color: black;" +
                                "-fx-border-width: 3;" +
                                "-fx-border-radius: 12;"
                );

                frontImage.setVisible(true);
                frontLabel.setVisible(false);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + value);
                frontLabel.setText("X");
                frontLabel.setVisible(true);
            }
        } else {
            frontLabel.setText(value);
            frontLabel.setVisible(true);
            frontImage.setVisible(false);
        }

        animateFlip();
    }

    public void hide() {
        if (matched) return;

        revealed = false;
        back.setVisible(true);
        frontLabel.setVisible(false);
        frontImage.setVisible(false);
        animateFlip();
    }

    public void pop() {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), this);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void animateFlip() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), this);
        st.setFromX(0);
        st.setToX(1);
        st.play();
    }

    private boolean isImage() {
        return value.endsWith(".jpg") || value.endsWith(".png");
    }

    public void shake() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), this);
        tt.setByX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.play();
    }
}
