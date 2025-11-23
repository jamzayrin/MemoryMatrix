import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LevelHandler {

    private final MemoryGame game;

    public LevelHandler(MemoryGame game) {
        this.game = game;
    }

    public void computeScore() {
        game.timeBonus = Math.max(0, (int)((300.0 / (game.elapsedSeconds + 1)) * 10));
        game.accuracyBonus = Math.max(0, 200 - (game.mismatches * 12));
        game.score = game.basePoints + game.timeBonus + game.accuracyBonus;
        game.totalScore += game.score;
    }

    public void showLevelComplete(Stage stage) {
        game.timerStop();

        computeScore();

        String nextLevel = game.getNextLevel();
        boolean hasNext = nextLevel != null;

        Stage popup = new Stage();
        popup.setTitle("Level Complete");
        popup.initOwner(stage);
        popup.setResizable(false);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        Label header = new Label(" Level Complete! 🎉");
        header.setFont(Font.font("Cambria", 26));

        ProgressBar pb = new ProgressBar();
        pb.setProgress(1.0);
        pb.setPrefWidth(400);
        pb.setPrefHeight(30);
        pb.setStyle("""
            -fx-accent: linear-gradient(to right, #2196f3, #9c27b0);
            -fx-control-inner-background: #e0e0e0;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        """);

        Label result = new Label(
                "Base Points: " + game.basePoints + "\n" +
                        "Time Bonus: " + game.timeBonus + "\n" +
                        "Accuracy Bonus: " + game.accuracyBonus + "\n" +
                        "Total Score: " + game.score + "\n\n" +
                        (hasNext ? "Proceed to next level?" : "You finished the last level!")
        );
        result.setFont(Font.font("Cambria", 16));
        result.setWrapText(true);

        Button nextBtn = new Button("Next Level");
        Button restartBtn = new Button("Restart");
        Button exitBtn = new Button("Exit to Menu");
        nextBtn.setDisable(!hasNext);

        HBox buttons = new HBox(15, nextBtn, restartBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        nextBtn.setOnAction(e -> {
            popup.close();
            game.selectedLevel = nextLevel;
            game.setLevelDimensions(nextLevel);
            game.startGame(stage);
        });

        restartBtn.setOnAction(e -> {
            popup.close();
            game.startGame(stage);
        });

        exitBtn.setOnAction(e -> {
            popup.close();
            game.showHomeMenu(stage);
        });

        layout.getChildren().addAll(header, pb, result, buttons);

        Scene scene = new Scene(layout, 420, 330);
        popup.setScene(scene);
        popup.show();
    }

    public void handleTimeUp(Stage stage) {
        game.busy = true;
        game.timerStop();

        Stage popup = new Stage();
        popup.setTitle("Time's Up!");
        popup.initOwner(stage);
        popup.setResizable(false);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffcccc, #ffe6e6); " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        Label header = new Label("⏰ Time's Up!");
        header.setFont(Font.font("Cambria", 28));

        Label message = new Label("Better luck next time!");
        message.setFont(Font.font("Cambria", 18));
        message.setWrapText(true);
        message.setStyle("-fx-text-alignment: center;");

        Button retryBtn = new Button("Retry Level");
        Button exitBtn = new Button("Exit to Menu");

        retryBtn.setOnAction(e -> {
            popup.close();
            game.startGame(stage);
        });

        exitBtn.setOnAction(e -> {
            popup.close();
            game.showHomeMenu(stage);
        });

        HBox buttons = new HBox(15, retryBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(header, message, buttons);

        Scene scene = new Scene(layout, 400, 250);
        popup.setScene(scene);
        popup.show();
    }
}
