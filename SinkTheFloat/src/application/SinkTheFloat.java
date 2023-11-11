package application;

import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SinkTheFloat extends Application {

	private static final int SHIP = 1;

	private int rows;
	private int columns;
	private int boatsLess;
	private int[][] board;
	private int attemps;
	private MediaPlayer mediaPlayer;
	private Timeline gameTimeline;
	private Text attemptsText;
	private Text shipsLeftText;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		createMenu(primaryStage);
		primaryStage.setTitle("Sink the Float");
		primaryStage.getIcons().add(new Image(getClass().getResource("/Images/icon.png").toString()));
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	private void createMenu(Stage primaryStage) {
		MenuBar menuBar = new MenuBar();
		Menu difficultyMenu = new Menu("Difficulty");
		MenuItem easyItem = new MenuItem("Easy (5x5) - 5 boats - 15 attemps");
		MenuItem normalItem = new MenuItem("Normal (10x10) - 10 boats - 50 attemps");
		MenuItem hardItem = new MenuItem("Hard (15x15) - 15 boats - 100 attemps");

		easyItem.setOnAction(event -> setDifficulty(5, 5, 5, 15, primaryStage));
		normalItem.setOnAction(event -> setDifficulty(10, 10, 10, 50, primaryStage));
		hardItem.setOnAction(event -> setDifficulty(15, 15, 15, 100, primaryStage));

		difficultyMenu.getItems().addAll(easyItem, normalItem, hardItem);

		menuBar.getMenus().add(difficultyMenu);
		primaryStage.setScene(new Scene(menuBar, 300, 300));
	}

	private void setDifficulty(int newRows, int newColumns, int newBoats, int newAttempts, Stage primaryStage) {
		rows = newRows;
		columns = newColumns;
		boatsLess = newBoats;
		attemps = newAttempts;

		initializeBoard();

		VBox vbox = new VBox();
		vbox.setAlignment(Pos.BOTTOM_CENTER);

		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);

		shipsLeftText = new Text("Ships left: " + boatsLess);
		shipsLeftText.setStyle("-fx-font-size: 14;");

		Label spaceLabel = new Label("   ");

		attemptsText = new Text("Attempts left: " + newAttempts);
		attemptsText.setStyle("-fx-font-size: 14;");

		hbox.getChildren().addAll(shipsLeftText, spaceLabel, attemptsText);

		GridPane grid = new GridPane();
		vbox.getChildren().addAll(grid, hbox);

		Scene scene = new Scene(vbox, newColumns * 60, (newRows + 1) * 60);

		String audioFile = "/Sounds/main.wav";
		Media sound = new Media(getClass().getResource(audioFile).toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.setVolume(0.2);
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		mediaPlayer.play();

		gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			if (boatsLess == 0) {
				Platform.runLater(() -> showEndGameAlert(primaryStage));
				gameTimeline.stop();
			} else if (attemps == 0) {
				Platform.runLater(() -> showLooseGameAlert(primaryStage));
				gameTimeline.stop();
			}
		}));
		gameTimeline.setCycleCount(Timeline.INDEFINITE);
		gameTimeline.play();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Button button = createButton(i, j);
				grid.add(button, j, i);
			}
		}

		primaryStage.setScene(scene);
	}

	private void initializeBoard() {
		board = new int[rows][columns];
		Random random = new Random();

		for (int i = 0; i < boatsLess; i++) {
			int randomRow, randomCol;
			do {
				randomRow = random.nextInt(rows);
				randomCol = random.nextInt(columns);
			} while (board[randomRow][randomCol] == SHIP);

			board[randomRow][randomCol] = SHIP;
		}
	}

	private Button createButton(int row, int col) {
		Button button = new Button();
		button.setMinSize(60, 60);
		button.setOnAction(event -> handleButtonClick(row, col, button));
		return button;
	}

	private void handleButtonClick(int row, int col, Button button) {
		if (board[row][col] == SHIP) {
			handleShipHit(button);
			shipsLeftText.setText("Ships left: " + boatsLess);
		} else {
			handleWaterHit(button);
			attemptsText.setText("Attempts left: " + attemps);
		}
	}

	private void handleShipHit(Button button) {
		ImageView enemyImageView = new ImageView(new Image(getClass().getResource("/Images/enemy.jpg").toString()));
		enemyImageView.setFitWidth(button.getWidth());
		enemyImageView.setFitHeight(button.getHeight());
		button.setGraphic(enemyImageView);
		playExplosionSound();
		boatsLess--;
	}

	private void handleWaterHit(Button button) {
		ImageView waterDropImageView = new ImageView(new Image(getClass().getResource("/Images/water.png").toString()));
		waterDropImageView.setFitWidth(button.getWidth());
		waterDropImageView.setFitHeight(button.getHeight());
		button.setGraphic(waterDropImageView);
		playWaterDropSound();
		attemps--;
	}

	private void playExplosionSound() {
		playSound("/Sounds/explosion.mp3");
	}

	private void playWaterDropSound() {
		playSound("/Sounds/water.mp3");
	}

	private void playSound(String soundFile) {
		try {
			new MediaPlayer(new Media(getClass().getResource(soundFile).toString())).play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showEndGameAlert(Stage primaryStage) {
		primaryStage.getTitle();
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("End game");
		alert.setHeaderText(null);
		alert.setContentText("All ships sunk!");
		ImageView imageView = new ImageView(getClass().getResource("/Images/icon.png").toString());
		imageView.setFitHeight(48);
		imageView.setFitWidth(48);
		alert.getDialogPane().setGraphic(imageView);
		alert.showAndWait();
		primaryStage.close();
	}

	private void showLooseGameAlert(Stage primaryStage) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("End game");
		alert.setHeaderText(null);
		alert.setContentText("0 attemps left!");
		ImageView imageView = new ImageView(getClass().getResource("/Images/icon.png").toString());
		imageView.setFitHeight(48);
		imageView.setFitWidth(48);
		alert.getDialogPane().setGraphic(imageView);
		alert.showAndWait();
		primaryStage.close();
	}
}
