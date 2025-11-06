import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.*;

public class App extends Application {

    private Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private final Set<Integer> selectedIndices = new HashSet<>();
    private boolean canReplace = false;

    private final GridPane playerPane = new GridPane();
    private final GridPane dealerPane = new GridPane();
    private final Label statusLabel = new Label("Press DEAL to start.");
    private final Label playerRankLabel = new Label();
    private final Label dealerRankLabel = new Label();
    private final Button dealBtn = new Button("Deal");
    private final Button drawBtn = new Button("Draw");
    private final Button showdownBtn = new Button("Showdown");
    private final Button quitBtn = new Button("Quit");

    @Override
    public void start(Stage primaryStage) throws Exception {

        System.out.println(getClass().getResource("/cards/AH.png"));
        System.out.println(getClass().getResource("/cards/back.png"));

        BorderPane main = new BorderPane();
        main.setPadding(new Insets(12));

        // Load and set background image
        Image bgImage = new Image(getClass().getResourceAsStream("/cards/cardGameBackground.jpg"));
        BackgroundImage backgroundImage = new BackgroundImage(
            bgImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(
                    BackgroundSize.AUTO,
                    BackgroundSize.AUTO,
                    true,
                    true,
                    true,
                    false
            )
        );
main.setBackground(new Background(backgroundImage));


        Label title = new Label("5-Card Draw — Player vs Dealer");
        title.setFont(Font.font(20));
        HBox top = new HBox(title);
        top.setAlignment(Pos.CENTER);
        main.setTop(top);

        VBox center = new VBox(10);
        center.setAlignment(Pos.CENTER);

        Label dealerLabel = new Label("Dealer");
        dealerLabel.setFont(Font.font(16));
        dealerPane.setHgap(8);
        VBox dealerBox = new VBox(5, dealerLabel, dealerPane, dealerRankLabel);

        Label playerLabel = new Label("Player");
        playerLabel.setFont(Font.font(16));
        playerPane.setHgap(8);
        VBox playerBox = new VBox(5, playerLabel, playerPane, playerRankLabel);

        center.getChildren().addAll(dealerBox, playerBox);
        main.setCenter(center);

        // Text and Button Styling 
        String labelStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 1, 1);";
        String titleStyle = "-fx-text-fill: gold; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 3, 0.8, 2, 2);";
        String buttonStyle = "-fx-background-color: linear-gradient(#ffcc00, #cc9900); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6;";

        // Apply to text labels
        title.setStyle(titleStyle);
        dealerLabel.setStyle(labelStyle);
        playerLabel.setStyle(labelStyle);
        statusLabel.setStyle(labelStyle);
        playerRankLabel.setStyle(labelStyle);
        dealerRankLabel.setStyle(labelStyle);

        // Apply to buttons
        dealBtn.setStyle(buttonStyle);
        drawBtn.setStyle(buttonStyle);
        showdownBtn.setStyle(buttonStyle);
        quitBtn.setStyle(buttonStyle);

        dealBtn.setOnAction(e -> dealNewHands());
        drawBtn.setOnAction(e -> drawNewCards());
        quitBtn.setOnAction(e -> primaryStage.close());

        // Button Hover Effects
        dealBtn.setOnMouseEntered(e -> dealBtn.setStyle("-fx-background-color: linear-gradient(#ffee88, #ccaa00); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6;"));
        dealBtn.setOnMouseExited(e -> dealBtn.setStyle(buttonStyle));

        drawBtn.setOnMouseEntered(e -> drawBtn.setStyle("-fx-background-color: linear-gradient(#ffee88, #ccaa00); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6;"));
        drawBtn.setOnMouseExited(e -> drawBtn.setStyle(buttonStyle));

        showdownBtn.setOnMouseEntered(e -> showdownBtn.setStyle("-fx-background-color: linear-gradient(#ffee88, #ccaa00); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6;"));
        showdownBtn.setOnMouseExited(e -> showdownBtn.setStyle(buttonStyle));

        quitBtn.setOnMouseEntered(e -> quitBtn.setStyle("-fx-background-color: linear-gradient(#ffee88, #ccaa00); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6;"));
        quitBtn.setOnMouseExited(e -> quitBtn.setStyle(buttonStyle));


        HBox buttons = new HBox(10, dealBtn, drawBtn, showdownBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);

        statusLabel.setFont(Font.font(14));

        VBox bottom = new VBox(10, buttons, statusLabel);
        bottom.setAlignment(Pos.CENTER);
        main.setBottom(bottom);

        primaryStage.setScene(new Scene(main, 770, 450));
        primaryStage.setResizable(false);
        primaryStage.setTitle("5-Card Stud");
        primaryStage.show();
    }

    private void dealNewHands() {
        deck = new Deck();
        deck.shuffle();
        playerHand.clear();
        dealerHand.clear();
        selectedIndices.clear();

        for (int i = 0; i < 5; i++) {
            playerHand.add(deck.dealCard());
            dealerHand.add(deck.dealCard());
        }

        canReplace = true;
        drawBtn.setDisable(false);
        showdownBtn.setDisable(true);

        renderHands();
        statusLabel.setText("Click up to 3 player cards to replace, then press DRAW.");
        playerRankLabel.setText("");
        dealerRankLabel.setText("");
    }

    private void drawNewCards() {
        if (!canReplace) return;

        if (selectedIndices.size() > 3) {
            statusLabel.setText("You can only replace up to 3 cards!");
            return;
        }

        for (int i : selectedIndices) {
            playerHand.set(i, deck.dealCard());
        }
        selectedIndices.clear();
        canReplace = false;
        drawBtn.setDisable(true);
        showdownBtn.setDisable(false);

        renderHands();
        statusLabel.setText("Replacement done. Press SHOWDOWN to see the winner.");
    }

    private void renderHands() {
        playerPane.getChildren().clear();
        dealerPane.getChildren().clear();

        // Dealer cards: show back until showdown
        for (int i = 0; i < dealerHand.size(); i++) {
            ImageView iv = new ImageView(canReplace ? new Image(getClass().getResourceAsStream("/cards/back.png")) 
                                                    : dealerHand.get(i).getImageView(80,120).getImage());
            iv.setFitWidth(80);
            iv.setFitHeight(120);
            dealerPane.add(iv, i, 0);
        }

        for (int i = 0; i < playerHand.size(); i++) {
            Card c = playerHand.get(i);
            ImageView iv = c.getImageView(80, 120);
            final int index = i;

            iv.setOnMouseClicked(e -> {
                if (!canReplace) return;
                if (selectedIndices.contains(index)) selectedIndices.remove(index);
                else if (selectedIndices.size() < 3) selectedIndices.add(index);
                renderHands();
            });

            if (selectedIndices.contains(i)) {
                iv.setStyle("-fx-effect: dropshadow(gaussian, dodgerblue, 25, 0.7, 0, 0);");
            } else {
                iv.setStyle(null);
            }

            playerPane.add(iv, i, 0);
        }
    }

    // Supporting Classes

    static class Deck {
        private final List<Card> cards = new ArrayList<>();
        private int currentIndex = 0;

        Deck() {
            for (Suit s : Suit.values()) {
                for (Rank r : Rank.values()) {
                    cards.add(new Card(r, s));
                }
            }
        }

        void shuffle() {
            Collections.shuffle(cards);
            currentIndex = 0;
        }

        Card dealCard() {
            if (currentIndex >= cards.size()) shuffle();
            return cards.get(currentIndex++);
        }
    }

    enum Rank { // Set up all ranks and make them final
        TWO(2,"2"), THREE(3,"3"), FOUR(4,"4"), FIVE(5,"5"), SIX(6,"6"),
        SEVEN(7,"7"), EIGHT(8,"8"), NINE(9,"9"), TEN(10,"10"),
        JACK(11,"J"), QUEEN(12,"Q"), KING(13,"K"), ACE(14,"A");
        final int value; final String symbol;
        Rank(int v,String s){value=v;symbol=s;}
    }

    enum Suit { // Set up all suits and make them final
        HEARTS("H"), DIAMONDS("D"), CLUBS("C"), SPADES("S");
        final String letter;
        Suit(String l){letter=l;}
    }

    static class Card {
        final Rank rank;
        final Suit suit;
        final Image image;

        Card(Rank r, Suit s) {
            rank = r;
            suit = s;
            String fileName = r.symbol + s.letter + ".png"; // ex: "AH.png"
            image = new Image(getClass().getResourceAsStream("/cards/" + fileName));
        }

        ImageView getImageView(double width, double height) {
            ImageView iv = new ImageView(image);
            iv.setFitWidth(width);
            iv.setFitHeight(height);
            return iv;
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
