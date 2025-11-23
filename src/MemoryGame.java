import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MemoryGame extends Application {

    private int COLS = 4;
    private int ROWS = 4;
    private int CARD_COUNT = COLS * ROWS;

    Card firstSelected = null;
    Card secondSelected = null;
    boolean busy = false;
    int attempts = 0;
    int matchesFound = 0;
    int mismatches = 0;
    int elapsedSeconds = 0;
    boolean timerStarted = false;

    public int totalScore = 0;
    public int score = 0;
    public int basePoints = 0;
    public int timeBonus = 0;
    public int accuracyBonus = 0;

    Label attemptsLabel;
    Label matchesLabel;
    Label timeLabel;
    Timeline timer;

    List<String> cardValues;
    public String selectedLevel = "Classic – 4x4";
    public String selectedTheme = "Black and White Icons";
    private double cardSize = 120;

    public LevelManager levelManager;
    public LevelHandler levelHandler;

    @Override
    public void start(Stage primaryStage) {
        levelManager = new LevelManager();
        levelManager.loadProgress();
        levelHandler = new LevelHandler(this);
        showHomeMenu(primaryStage);
    }


    public void showHomeMenu(Stage stage) {
        stage.setScene(HomeMenu.create(stage, this));
        stage.show();
    }

    public void showThemeSelection(Stage stage) {
        stage.setScene(ThemeSelection.create(stage, this));
    }

    public void setLevelDimensions(String level) {
        switch (level) {
            case "Easy – 4x3": COLS = 4; ROWS = 3; break;
            case "Classic – 4x4": COLS = 4; ROWS = 4; break;
            case "Medium – 5x4": COLS = 5; ROWS = 4; break;
            case "Hard – 6x5": COLS = 6; ROWS = 5; break;
            case "Expert – 8x5": COLS = 8; ROWS = 5; break;
            case "Master – 8x6": COLS = 8; ROWS = 6; break;
            case "Grandmaster – 9x6": COLS = 9; ROWS = 6; break;
            case "Legendary – 10x6": COLS = 10; ROWS = 6; break;
            default: COLS = 4; ROWS = 4; break;
        }
        CARD_COUNT = COLS * ROWS;
        if (CARD_COUNT % 2 != 0) CARD_COUNT++;
        basePoints = 100 * (Arrays.asList(levelManager.getAllLevels()).indexOf(level) + 1);
    }

    public void startGame(Stage stage) {
        resetGameState();
        computeCardSize();

        BorderPane root = new BorderPane();

        Image backgroundImage = new Image(getClass().getResource("/images/cardsmainbg.png").toExternalForm());
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1.0, 1.0, true, true, false, true)
        );
        root.setBackground(new Background(bgImage));

        root.setTop(makeTopBar(stage));
        root.setBottom(makeBottomBar());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        generateCardValues();
        List<Card> cards = createCards();

        int i = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (i >= cards.size()) break;
                Card card = cards.get(i++);
                card.setPrefSize(cardSize, cardSize);
                grid.add(card, c, r);
            }
        }

        root.setCenter(grid);

        Scene scene = new Scene(root,
                Math.max(900, COLS * (cardSize + 16) + 200),
                Math.max(700, ROWS * (cardSize + 16) + 200));
        stage.setScene(scene);
        stage.show();
    }

    private HBox makeTopBar(Stage stage) {
        HBox top = new HBox(16);
        top.setPadding(new Insets(12));
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(selectedLevel + " | " + selectedTheme);
        title.setFont(Font.font("Cambria", 20));

        attemptsLabel = new Label("Attempts: 0");
        attemptsLabel.setFont(Font.font("Cambria", 18));

        matchesLabel = new Label("Matches: 0/" + (CARD_COUNT / 2));
        matchesLabel.setFont(Font.font("Cambria", 18));

        timeLabel = new Label("Time: 0:00");
        timeLabel.setFont(Font.font("Cambria", 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button home = new Button("Home");
        home.setFont(Font.font("Cambria", 14));
        home.setOnAction(e -> showHomeMenu(stage));

        Button restart = new Button("Restart");
        restart.setFont(Font.font("Cambria", 14));
        restart.setOnAction(e -> startGame(stage));

        top.getChildren().addAll(title, attemptsLabel, matchesLabel, timeLabel, spacer, home, restart);
        return top;
    }

    private HBox makeBottomBar() {
        HBox bottom = new HBox();
        bottom.setPadding(new Insets(12));
        bottom.setAlignment(Pos.CENTER);
        Label hint = new Label("Tip: Click a card to flip. Match all pairs!");
        bottom.getChildren().add(hint);
        return bottom;
    }

    private void generateCardValues() {
        int pairsNeeded = CARD_COUNT / 2;
        cardValues = CardValueGenerator.generate(selectedTheme, pairsNeeded);
    }

    private List<Card> createCards() {
        List<Card> cards = new ArrayList<>();
        for (String val : cardValues) cards.add(new Card(val, cardSize, this));
        return cards;
    }

    public void onCardClicked(Card card) {
        if (busy || card.isMatched() || card.isRevealed()) return;

        if (!timerStarted) startTimer();
        card.reveal();

        if (firstSelected == null) {
            firstSelected = card;
            return;
        }

        if (secondSelected == null && card != firstSelected) {
            secondSelected = card;
            busy = true;
            attempts++;
            updateStats();

            Timeline t = new Timeline(new KeyFrame(Duration.millis(600), ev -> checkMatch()));
            t.play();
        }
    }

    private void checkMatch() {
        if (firstSelected != null && secondSelected != null) {
            if (firstSelected.getValue().equals(secondSelected.getValue())) {
                firstSelected.setMatched(true);
                secondSelected.setMatched(true);
                matchesFound++;
                firstSelected.pop();
                secondSelected.pop();
                updateStats();

                if (matchesFound == CARD_COUNT / 2)
                    Platform.runLater(() ->
                            levelHandler.showLevelComplete((Stage) timeLabel.getScene().getWindow()));

            } else {
                mismatches++;
                firstSelected.shake();
                secondSelected.shake();
                firstSelected.hide();
                secondSelected.hide();
            }
        }

        firstSelected = null;
        secondSelected = null;
        busy = false;
    }

    private void startTimer() {
        timerStarted = true;

        int countdown = getCountdownSeconds(selectedLevel);

        if (countdown > 0) {
            elapsedSeconds = countdown;
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                int min = elapsedSeconds / 60;
                int sec = elapsedSeconds % 60;
                timeLabel.setText(String.format("Time: %d:%02d", min, sec));
                elapsedSeconds--;
                if (elapsedSeconds < 0) {
                    timerStop();
                    Platform.runLater(() ->
                            levelHandler.handleTimeUp((Stage) timeLabel.getScene().getWindow()));
                }
            }));
        } else {
            elapsedSeconds = 0;
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                elapsedSeconds++;
                int min = elapsedSeconds / 60;
                int sec = elapsedSeconds % 60;
                timeLabel.setText(String.format("Time: %d:%02d", min, sec));
            }));
        }

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    public void timerStop() {
        if (timer != null) timer.stop();
    }

    private int getCountdownSeconds(String level) {
        return switch (level) {
            case "Expert – 8x5" -> 180;
            case "Master – 8x6" -> 210;
            case "Grandmaster – 9x6" -> 240;
            case "Legendary – 10x6" -> 270;
            default -> -1;
        };
    }

    private void updateStats() {
        attemptsLabel.setText("Attempts: " + attempts);
        matchesLabel.setText("Matches: " + matchesFound + "/" + (CARD_COUNT / 2));
    }

    private void resetGameState() {
        firstSelected = null;
        secondSelected = null;
        busy = false;
        attempts = 0;
        matchesFound = 0;
        mismatches = 0;
        elapsedSeconds = 0;
        timerStarted = false;
        score = 0;
        timeBonus = 0;
        accuracyBonus = 0;
        timerStop();
    }

    public String getNextLevel() {
        String[] all = levelManager.getAllLevels();
        for (int i = 0; i < all.length - 1; i++)
            if (all[i].equals(selectedLevel)) return all[i + 1];
        return null;
    }

    private void computeCardSize() {
        double allowedW = 700.0 / COLS * 3;
        double allowedH = 500.0 / ROWS * 3;

        int levelIndex = Arrays.asList(levelManager.getAllLevels()).indexOf(selectedLevel);
        double scaleFactor = 1.0 - (levelIndex * 0.20);
        scaleFactor = Math.max(0.4, scaleFactor);

        cardSize = Math.max(50, Math.min(140, Math.min(allowedW, allowedH) * scaleFactor));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
