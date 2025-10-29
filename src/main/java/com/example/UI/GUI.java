package com.example.UI;

import com.example.model.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Optional;

public class GUI extends Application {

    private Game game;
    private Player human;
    private TextArea logArea;
    private Card selectedSuspect;
    private Card selectedWeapon;
    private Card selectedRoom;
    private Button suggestBtn;
    private Button accuseBtn;
    private Label turnLabel;
    private Label aiChoiceLabel;
    private FlowPane suspectCardsBox;
    private FlowPane weaponCardsBox;
    private FlowPane roomCardsBox;
    private VBox playerHandBox;

    @Override
    public void start(Stage stage) {showMainMenu(stage);}

    private void showMainMenu(Stage stage) {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        Label title = new Label("CLUE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#f5f5f5"));
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0.5, 0, 2);");

        Label subtitle = new Label("The Classic Mystery Game");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.web("#e0e0e0"));

        Label label = new Label("Choose number of AI opponents:");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.web("#ffffff"));

        ChoiceBox<Integer> aiCountBox = new ChoiceBox<>();
        aiCountBox.getItems().addAll(1, 2, 3, 4, 5);
        aiCountBox.getSelectionModel().selectFirst();
        aiCountBox.setStyle("-fx-font-size: 14px; -fx-background-color: #0f3460; -fx-mark-color: white;");

        Button startBtn = new Button("Start Game");
        styleMenuButton(startBtn, "#16a085", "#1abc9c");

        Button exitBtn = new Button("Exit");
        styleMenuButton(exitBtn, "#c0392b", "#e74c3c");

        startBtn.setOnAction(e -> {
            int numAI = aiCountBox.getValue();
            startLocalMultiplayer(stage, numAI);
        });
        exitBtn.setOnAction(e -> Platform.exit());

        VBox buttonBox = new VBox(15, startBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, subtitle, new Label(""), label, aiCountBox, new Label(""), buttonBox);

        stage.setScene(new Scene(root, 500, 500));
        stage.setTitle("Clue - Main Menu");
        stage.show();
    }

    private void styleMenuButton(Button btn, String baseColor, String hoverColor) {
        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.3, 0, 2);",
                baseColor
        ));

        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0.5, 0, 3); " +
                        "-fx-scale-x: 1.05; -fx-scale-y: 1.05;",
                hoverColor
        )));

        btn.setOnMouseExited(e -> btn.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.3, 0, 2);",
                baseColor
        )));
    }

    private void startLocalMultiplayer(Stage stage, int numAI) {
        game = new Game();
        human = new Player("You");
        game.addPlayer(human);
        for (int i = 1; i <= numAI; i++) {
            AIPlayer ai = new AIPlayer("Computer" + i);
            game.addPlayer(ai);
        }
        game.setupAndDeal();

        selectedSuspect = game.getSuspectCards().get(0);
        selectedWeapon = game.getWeaponCards().get(0);
        selectedRoom = game.getRoomCards().get(0);

        suggestBtn = new Button("Make Suggestion");
        accuseBtn = new Button("Make Accusation");
        styleActionButton(suggestBtn, "#2980b9", "#3498db");
        styleActionButton(accuseBtn, "#d35400", "#e67e22");

        suggestBtn.setOnAction(e -> onPlayerSuggestion());
        accuseBtn.setOnAction(e -> onPlayerAccusation());

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(180);
        logArea.setStyle(
                "-fx-control-inner-background: #2c3e50; " +
                        "-fx-text-fill: #ecf0f1; " +
                        "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-color: #34495e; " +
                        "-fx-background-radius: 8;"
        );

        turnLabel = new Label();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        updateTurnLabel();

        // AI Choice display
        aiChoiceLabel = new Label("AI's Last Choice: (Waiting for AI turn...)");
        aiChoiceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        aiChoiceLabel.setTextFill(Color.web("#ecf0f1"));
        aiChoiceLabel.setWrapText(true);
        aiChoiceLabel.setMaxWidth(1000);
        aiChoiceLabel.setAlignment(Pos.CENTER);
        aiChoiceLabel.setPadding(new Insets(10));
        aiChoiceLabel.setStyle(
                "-fx-background-color: rgba(52, 73, 94, 0.8); " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.3, 0, 2);"
        );

        suspectCardsBox = new FlowPane(15, 15);
        suspectCardsBox.setAlignment(Pos.CENTER);
        suspectCardsBox.setPrefWrapLength(800);
        List<Card> allSuspects = new java.util.ArrayList<>(game.getSuspectCards());
        allSuspects.add(game.getSolution().get(Card.CardType.SUSPECT));
        java.util.Collections.shuffle(allSuspects);
        createCardButtons(suspectCardsBox, allSuspects, "SUSPECT");

        weaponCardsBox = new FlowPane(15, 15);
        weaponCardsBox.setAlignment(Pos.CENTER);
        weaponCardsBox.setPrefWrapLength(800);
        List<Card> allWeapons = new java.util.ArrayList<>(game.getWeaponCards());
        allWeapons.add(game.getSolution().get(Card.CardType.WEAPON));
        java.util.Collections.shuffle(allWeapons);
        createCardButtons(weaponCardsBox, allWeapons, "WEAPON");

        roomCardsBox = new FlowPane(15, 15);
        roomCardsBox.setAlignment(Pos.CENTER);
        roomCardsBox.setPrefWrapLength(800);
        List<Card> allRooms = new java.util.ArrayList<>(game.getRoomCards());
        allRooms.add(game.getSolution().get(Card.CardType.ROOM));
        java.util.Collections.shuffle(allRooms);
        createCardButtons(roomCardsBox, allRooms, "ROOM");

        playerHandBox = new VBox(10);
        updatePlayerHandDisplay();

        VBox suspectsSection = createCardSection("SELECT SUSPECT", suspectCardsBox, "#e74c3c");
        VBox weaponsSection = createCardSection("SELECT WEAPON", weaponCardsBox, "#f39c12");
        VBox roomsSection = createCardSection("SELECT ROOM", roomCardsBox, "#9b59b6");

        HBox buttons = new HBox(20, suggestBtn, accuseBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 10, 0));

        VBox selectionArea = new VBox(18, suspectsSection, weaponsSection, roomsSection, buttons);
        selectionArea.setPadding(new Insets(20));
        selectionArea.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0.4, 0, 4);"
        );

        ScrollPane logScroll = new ScrollPane(logArea);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(180);
        logScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Label logTitle = new Label("GAME LOG");
        logTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        logTitle.setTextFill(Color.web("#ecf0f1"));
        logTitle.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0.5, 0, 1);");

        VBox logSection = new VBox(8, logTitle, logScroll);
        logSection.setPadding(new Insets(15));
        logSection.setStyle(
                "-fx-background-color: #34495e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0.4, 0, 4);"
        );

        ScrollPane mainScroll = new ScrollPane();
        mainScroll.setContent(selectionArea);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox mainContent = new HBox(20, mainScroll, playerHandBox);
        mainContent.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(mainScroll, Priority.ALWAYS);

        VBox layout = new VBox(15, turnLabel, aiChoiceLabel, mainContent, logSection);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        log("Game started! Players: " + playersListString(game.getPlayers()));
        log("Your cards: " + human.getHand());
        log("First turn: " + game.getCurrentPlayer().getName());

        Scene scene = new Scene(layout, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("Clue - Local Multiplayer");
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show();

        runTurnsUntilHuman();
    }

    private VBox createCardSection(String title, FlowPane cardPane, String accentColor) {
        Label header = new Label(title);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        header.setTextFill(Color.WHITE);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(8, 0, 8, 0));
        header.setStyle(
                "-fx-background-color: " + accentColor + "; " +
                        "-fx-background-radius: 8 8 0 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.3, 0, 2);"
        );

        VBox container = new VBox(0, header, cardPane);
        container.setStyle(
                "-fx-background-color: rgba(52, 73, 94, 0.6); " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 0 0 12 0;"
        );

        return container;
    }

    private void styleActionButton(Button btn, String baseColor, String hoverColor) {
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.4, 0, 3);",
                baseColor
        ));

        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(String.format(
                        "-fx-background-color: %s; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 12, 0.5, 0, 4);",
                        hoverColor
                ));
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(String.format(
                        "-fx-background-color: %s; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.4, 0, 3);",
                        baseColor
                ));
            }
        });
    }

    private void createCardButtons(FlowPane container, List<Card> cards, String category) {
        for (Card card : cards) {
            StackPane cardBox = create3DCard(card, category);
            cardBox.setOnMouseClicked(e -> selectCard(card, category, container));
            container.getChildren().add(cardBox);
        }
        if (!cards.isEmpty()) {
            highlightSelectedCard(container, 0);
        }
    }

    private StackPane create3DCard(Card card, String category) {
        StackPane cardStack = new StackPane();
        cardStack.setPrefSize(135, 190);
        cardStack.setMaxSize(135, 190);
        cardStack.setMinSize(135, 190);

        // 3D Card layers - back layer (shadow/depth)
        VBox cardBack = new VBox();
        cardBack.setPrefSize(135, 190);
        cardBack.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.4); " +
                        "-fx-background-radius: 15; " +
                        "-fx-translate-x: 4; " +
                        "-fx-translate-y: 4;"
        );

        // Middle layer (border/frame)
        VBox cardBorder = new VBox();
        cardBorder.setPrefSize(135, 190);
        String borderColor = getCategoryBorderColor(category);
        cardBorder.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + borderColor + ", " + darkenColor(borderColor) + "); " +
                        "-fx-background-radius: 15; " +
                        "-fx-translate-x: 2; " +
                        "-fx-translate-y: 2;"
        );

        // Front layer (main card)
        VBox cardFront = new VBox(10);
        cardFront.setAlignment(Pos.CENTER);
        cardFront.setPrefSize(130, 185);
        cardFront.setPadding(new Insets(15, 10, 15, 10));

        String bgGradient = getCardGradient(category);
        cardFront.setStyle(
                "-fx-background-color: " + bgGradient + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 2; " +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.2), 5, 0.3, 0, 2);"
        );

        // Character portrait area with frame
        StackPane portraitFrame = new StackPane();
        portraitFrame.setPrefSize(90, 90);
        portraitFrame.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, rgba(255,255,255,0.9), rgba(240,240,240,0.7)); " +
                        "-fx-background-radius: 45; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-radius: 45; " +
                        "-fx-border-width: 3; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.4, 0, 2);"
        );

        Label icon = new Label(getCardEmoji(card));
        icon.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        icon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0.5, 0, 1);");

        portraitFrame.getChildren().add(icon);

        // Card name with decorative underline
        VBox nameBox = new VBox(3);
        nameBox.setAlignment(Pos.CENTER);

        Label name = new Label(card.getName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        name.setWrapText(true);
        name.setTextAlignment(TextAlignment.CENTER);
        name.setMaxWidth(110);
        name.setAlignment(Pos.CENTER);
        name.setStyle(
                "-fx-text-fill: #2c3e50; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.8), 1, 0.5, 0, 1);"
        );

        Rectangle underline = new Rectangle(60, 2);
        underline.setFill(Color.web(borderColor));
        underline.setArcWidth(2);
        underline.setArcHeight(2);

        nameBox.getChildren().addAll(name, underline);

        // Category badge with shine
        StackPane badgeStack = new StackPane();
        Rectangle badge = new Rectangle(80, 20);
        badge.setFill(Color.web(borderColor));
        badge.setArcWidth(10);
        badge.setArcHeight(10);

        Label typeBadge = new Label(category);
        typeBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        typeBadge.setTextFill(Color.WHITE);
        typeBadge.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0.5, 0, 1);");

        badgeStack.getChildren().addAll(badge, typeBadge);

        cardFront.getChildren().addAll(portraitFrame, nameBox, badgeStack);

        // Add decorative corner elements
        addCornerDecorations(cardFront, borderColor);

        cardStack.getChildren().addAll(cardBack, cardBorder, cardFront);
        cardStack.setStyle("-fx-cursor: hand;");

        // Enhanced hover effects with rotation
        cardStack.setOnMouseEntered(e -> {
            if (!isCardSelected(card, category)) {
                cardStack.setEffect(new DropShadow(20, Color.rgb(52, 152, 219, 0.8)));
                cardStack.setScaleX(1.08);
                cardStack.setScaleY(1.08);
                cardStack.setTranslateY(-5);
            }
        });

        cardStack.setOnMouseExited(e -> {
            if (!isCardSelected(card, category)) {
                cardStack.setEffect(null);
                cardStack.setScaleX(1.0);
                cardStack.setScaleY(1.0);
                cardStack.setTranslateY(0);
            }
        });

        return cardStack;
    }

    private void addCornerDecorations(VBox cardFront, String color) {
        // Top-left corner decoration
        Label topLeft = new Label("◆");
        topLeft.setFont(Font.font(12));
        topLeft.setTextFill(Color.web(color));
        topLeft.setTranslateX(-50);
        topLeft.setTranslateY(-75);

        // Top-right corner decoration
        Label topRight = new Label("◆");
        topRight.setFont(Font.font(12));
        topRight.setTextFill(Color.web(color));
        topRight.setTranslateX(50);
        topRight.setTranslateY(-75);

        cardFront.getChildren().addAll(topLeft, topRight);
    }

    private String getCardGradient(String category) {
        switch(category) {
            case "SUSPECT": return "linear-gradient(to bottom, #ffe6e6, #ffd4d4, #ffc2c2)";
            case "WEAPON": return "linear-gradient(to bottom, #fff8e6, #ffefd4, #ffe6c2)";
            case "ROOM": return "linear-gradient(to bottom, #f5e6ff, #ead4ff, #dfc2ff)";
            default: return "linear-gradient(to bottom, #ffffff, #f5f5f5, #eeeeee)";
        }
    }

    private String getCategoryBorderColor(String category) {
        switch(category) {
            case "SUSPECT": return "#e74c3c";
            case "WEAPON": return "#f39c12";
            case "ROOM": return "#9b59b6";
            default: return "#95a5a6";
        }
    }

    private String darkenColor(String hexColor) {
        // Simple darkening by reducing brightness
        if (hexColor.equals("#e74c3c")) return "#c0392b";
        if (hexColor.equals("#f39c12")) return "#d68910";
        if (hexColor.equals("#9b59b6")) return "#8e44ad";
        return "#7f8c8d";
    }

    private String getCardEmoji(Card card) {
        String name = card.getName().toLowerCase();

        // Suspects - using text initials instead of emojis
        if (name.contains("scarlet")) return "MS";
        if (name.contains("mustard")) return "CM";
        if (name.contains("white")) return "MW";
        if (name.contains("green")) return "MG";
        if (name.contains("peacock")) return "MP";
        if (name.contains("plum")) return "PP";

        // Weapons - using text abbreviations
        if (name.contains("rope")) return "RP";
        if (name.contains("pipe")) return "PP";
        if (name.contains("knife")) return "KN";
        if (name.contains("wrench")) return "WR";
        if (name.contains("candlestick")) return "CS";
        if (name.contains("revolver") || name.contains("gun") || name.contains("pistol")) return "RV";

        // Rooms - using text abbreviations
        if (name.contains("kitchen")) return "KT";
        if (name.contains("ballroom")) return "BR";
        if (name.contains("conservatory")) return "CN";
        if (name.contains("dining")) return "DR";
        if (name.contains("billiard")) return "BI";
        if (name.contains("library")) return "LB";
        if (name.contains("lounge")) return "LG";
        if (name.contains("hall")) return "HL";
        if (name.contains("study")) return "ST";

        return "?";
    }

    private void selectCard(Card card, String category, FlowPane container) {
        if (category.equals("SUSPECT")) {
            selectedSuspect = card;
        } else if (category.equals("WEAPON")) {
            selectedWeapon = card;
        } else if (category.equals("ROOM")) {
            selectedRoom = card;
        }

        List<Card> cards = category.equals("SUSPECT") ? game.getSuspectCards() :
                category.equals("WEAPON") ? game.getWeaponCards() :
                        game.getRoomCards();

        for (int i = 0; i < container.getChildren().size(); i++) {
            StackPane cardStack = (StackPane) container.getChildren().get(i);
            Card cardAtIndex = cards.get(i);

            if (cardAtIndex.equals(card)) {
                // Selected state - golden glow
                DropShadow glow = new DropShadow();
                glow.setColor(Color.rgb(241, 196, 15, 0.9));
                glow.setRadius(25);
                glow.setSpread(0.6);
                cardStack.setEffect(glow);
                cardStack.setScaleX(1.1);
                cardStack.setScaleY(1.1);
                cardStack.setTranslateY(-8);

                // Add golden border to front card
                VBox frontCard = (VBox) cardStack.getChildren().get(2);
                frontCard.setStyle(frontCard.getStyle().replace(
                        "-fx-border-color: rgba(255, 255, 255, 0.3);",
                        "-fx-border-color: #f1c40f;"
                ).replace(
                        "-fx-border-width: 2;",
                        "-fx-border-width: 4;"
                ));
            } else {
                cardStack.setEffect(null);
                cardStack.setScaleX(1.0);
                cardStack.setScaleY(1.0);
                cardStack.setTranslateY(0);

                // Reset front card border
                VBox frontCard = (VBox) cardStack.getChildren().get(2);
                String bgGradient = getCardGradient(category);
                frontCard.setStyle(
                        "-fx-background-color: " + bgGradient + "; " +
                                "-fx-background-radius: 12; " +
                                "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                                "-fx-border-radius: 12; " +
                                "-fx-border-width: 2; " +
                                "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.2), 5, 0.3, 0, 2);"
                );
            }
        }
    }

    private void highlightSelectedCard(FlowPane container, int index) {
        StackPane cardStack = (StackPane) container.getChildren().get(index);
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(241, 196, 15, 0.9));
        glow.setRadius(25);
        glow.setSpread(0.6);
        cardStack.setEffect(glow);
        cardStack.setScaleX(1.1);
        cardStack.setScaleY(1.1);
        cardStack.setTranslateY(-8);
    }

    private boolean isCardSelected(Card card, String category) {
        if (category.equals("SUSPECT")) return card.equals(selectedSuspect);
        if (category.equals("WEAPON")) return card.equals(selectedWeapon);
        if (category.equals("ROOM")) return card.equals(selectedRoom);
        return false;
    }

    private void updatePlayerHandDisplay() {
        playerHandBox.getChildren().clear();
        playerHandBox.setMinWidth(280);
        playerHandBox.setMaxWidth(280);

        Label title = new Label("YOUR HAND");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(10));
        title.setStyle(
                "-fx-background-color: #16a085; " +
                        "-fx-background-radius: 10 10 0 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.3, 0, 2);"
        );

        VBox cardsContainer = new VBox(12);
        cardsContainer.setAlignment(Pos.TOP_CENTER);
        cardsContainer.setPadding(new Insets(15));
        cardsContainer.setStyle(
                "-fx-background-color: rgba(22, 160, 133, 0.2); " +
                        "-fx-background-radius: 0 0 10 10;"
        );

        for (Card card : human.getHand()) {
            StackPane cardItem = createHandCard3D(card);
            cardsContainer.getChildren().add(cardItem);
        }

        VBox container = new VBox(0, title, cardsContainer);
        container.setStyle(
                "-fx-background-color: #34495e; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0.4, 0, 4);"
        );

        playerHandBox.getChildren().add(container);
    }

    private StackPane createHandCard3D(Card card) {
        StackPane cardStack = new StackPane();
        cardStack.setPrefSize(250, 90);

        // Shadow layer
        HBox shadowBox = new HBox();
        shadowBox.setPrefSize(250, 90);
        shadowBox.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.4); " +
                        "-fx-background-radius: 10; " +
                        "-fx-translate-x: 3; " +
                        "-fx-translate-y: 3;"
        );

        // Border layer
        HBox borderBox = new HBox();
        borderBox.setPrefSize(250, 90);
        borderBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #1abc9c, #16a085); " +
                        "-fx-background-radius: 10; " +
                        "-fx-translate-x: 1.5; " +
                        "-fx-translate-y: 1.5;"
        );

        // Main card
        HBox cardBox = new HBox(12);
        cardBox.setAlignment(Pos.CENTER_LEFT);
        cardBox.setPadding(new Insets(12, 15, 12, 15));
        cardBox.setPrefSize(245, 85);
        cardBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #48c9b0, #1abc9c); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.4); " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 2; " +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.2), 3, 0.3, 0, 1);"
        );

        // Portrait circle
        StackPane portraitCircle = new StackPane();
        portraitCircle.setPrefSize(60, 60);
        portraitCircle.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, rgba(255,255,255,0.95), rgba(240,240,240,0.8)); " +
                        "-fx-background-radius: 30; " +
                        "-fx-border-color: white; " +
                        "-fx-border-radius: 30; " +
                        "-fx-border-width: 3; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.4, 0, 2);"
        );

        Label emoji = new Label(getCardEmoji(card));
        emoji.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        portraitCircle.getChildren().add(emoji);

        // Card info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label cardName = new Label(card.getName());
        cardName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cardName.setTextFill(Color.WHITE);
        cardName.setWrapText(true);
        cardName.setMaxWidth(150);
        cardName.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0.5, 0, 1);");

        Label cardType = new Label(card.getType().toString());
        cardType.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        cardType.setTextFill(Color.web("#ecf0f1"));
        cardType.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.2); " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 2 8 2 8;"
        );

        infoBox.getChildren().addAll(cardName, cardType);
        cardBox.getChildren().addAll(portraitCircle, infoBox);

        cardStack.getChildren().addAll(shadowBox, borderBox, cardBox);

        return cardStack;
    }

    private void onPlayerSuggestion() {
        if (!isHumanTurn()) {
            log("WARNING: It's not your turn.");
            return;
        }

        Suggestion suggestion = new Suggestion(selectedSuspect, selectedWeapon, selectedRoom);
        log("SUGGESTION: You suggest: " + suggestion);

        // Update AI choice display to show player's choice
        updateAIChoiceDisplay("YOUR CHOICE: " + selectedSuspect.getName() + " + " +
                selectedWeapon.getName() + " + " + selectedRoom.getName(), false);

        Optional<Card> refute = game.resolveSuggestion(human, suggestion);
        if (refute.isPresent()) log("REFUTED: Someone refutes with: " + refute.get());
        else log("SUCCESS: No one could refute your suggestion!");

        game.advanceTurn();
        updateTurnLabel();
        runTurnsUntilHuman();
    }

    private void onPlayerAccusation() {
        if (!isHumanTurn()) {
            log("WARNING: It's not your turn.");
            return;
        }

        Suggestion accusation = new Suggestion(selectedSuspect, selectedWeapon, selectedRoom);
        log("ACCUSATION: You accuse: " + accusation);

        // Update AI choice display to show player's accusation
        updateAIChoiceDisplay("YOUR ACCUSATION: " + selectedSuspect.getName() + " + " +
                selectedWeapon.getName() + " + " + selectedRoom.getName(), false);

        boolean correct = game.checkAccusation(accusation);
        if (correct) {
            log("CONGRATULATIONS: Your accusation is correct! You win!");
            endGame(true);
        } else {
            log("INCORRECT: Wrong accusation. You are eliminated from the game.");
            human.setEliminated(true);
            if (game.activePlayersCount() <= 1) {
                endGame(false);
                return;
            }
            game.advanceTurn();
            updateTurnLabel();
            runTurnsUntilHuman();
        }
    }

    private void updateAIChoiceDisplay(String text, boolean isAI) {
        aiChoiceLabel.setText(text);
        if (isAI) {
            aiChoiceLabel.setStyle(
                    "-fx-background-color: rgba(231, 76, 60, 0.8); " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.3, 0, 2);"
            );
        } else {
            aiChoiceLabel.setStyle(
                    "-fx-background-color: rgba(46, 204, 113, 0.8); " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 5, 0.3, 0, 2);"
            );
        }
    }

    private boolean isHumanTurn() {return game.getCurrentPlayer() == human;}

    private void runTurnsUntilHuman() {
        suggestBtn.setDisable(true);
        accuseBtn.setDisable(true);
        while (true) {
            Player current = game.getCurrentPlayer();
            if (current == null) break;
            if (current.isEliminated()) {
                game.advanceTurn();
                updateTurnLabel();
                continue;
            }
            if (current == human) {
                suggestBtn.setDisable(false);
                accuseBtn.setDisable(false);
                updateTurnLabel();
                break;
            }
            AIPlayer ai = (AIPlayer) current;
            log("AI TURN: " + ai.getName() + "'s turn");
            if (ai.wantsToAccuse()) {
                Suggestion accusation = ai.makeRandomSuggestion(game);
                log("AI ACCUSATION: " + ai.getName() + " accuses: " + accusation);

                // Display AI's accusation
                updateAIChoiceDisplay(ai.getName() + " ACCUSES: " +
                        accusation.getSuspect().getName() + " + " +
                        accusation.getWeapon().getName() + " + " +
                        accusation.getRoom().getName(), true);

                if (game.checkAccusation(accusation)) {
                    log("AI WINS: " + ai.getName() + " accused correctly and wins!");
                    endGame(false);
                    return;
                } else {
                    log("AI ELIMINATED: " + ai.getName() + " accused incorrectly.");
                    ai.setEliminated(true);

                    if (game.activePlayersCount() <= 1) {
                        endGame(true);
                        return;
                    }
                    game.advanceTurn();
                    updateTurnLabel();
                    continue;
                }
            }
            Suggestion suggestion = ai.makeRandomSuggestion(game);
            log("AI SUGGESTION: " + ai.getName() + " suggests: " + suggestion);

            // Display AI's suggestion
            updateAIChoiceDisplay(ai.getName() + " SUGGESTS: " +
                    suggestion.getSuspect().getName() + " + " +
                    suggestion.getWeapon().getName() + " + " +
                    suggestion.getRoom().getName(), true);

            Optional<Card> refute = game.resolveSuggestion(ai, suggestion);

            if (refute.isPresent()) log("REFUTED: Someone refutes " + ai.getName() + "'s suggestion with: " + refute.get());
            else log("NO REFUTE: No one could refute " + ai.getName() + "'s suggestion.");
            game.advanceTurn();
            updateTurnLabel();
            if (game.activePlayersCount() <= 1) {
                Player winner = null;
                for (Player p : game.getPlayers()) {
                    if (!p.isEliminated()) winner = p;
                }
                endGame(winner == human);
                return;
            }
        }
    }

    private void updateTurnLabel() {
        Player cur = game.getCurrentPlayer();
        if (cur == null) {
            turnLabel.setText("No current player");
            turnLabel.setTextFill(Color.web("#95a5a6"));
        } else {
            if (cur == human) {
                turnLabel.setText("YOUR TURN!");
                turnLabel.setTextFill(Color.web("#2ecc71"));
                turnLabel.setStyle(
                        "-fx-font-weight: bold; " +
                                "-fx-font-size: 20px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(46, 204, 113, 0.8), 10, 0.5, 0, 2);"
                );
            } else {
                turnLabel.setText("Current turn: " + cur.getName());
                turnLabel.setTextFill(Color.web("#e74c3c"));
                turnLabel.setStyle(
                        "-fx-font-weight: bold; " +
                                "-fx-font-size: 20px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.6), 8, 0.4, 0, 2);"
                );
            }
        }
    }

    private void log(String text) {
        logArea.appendText("> " + text + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    private void endGame(boolean humanWon) {
        suggestBtn.setDisable(true);
        accuseBtn.setDisable(true);
        String solution = game.getSolution().get(Card.CardType.SUSPECT) + ", "
                + game.getSolution().get(Card.CardType.WEAPON) + ", "
                + game.getSolution().get(Card.CardType.ROOM);

        String message = humanWon
                ? "VICTORY! You won! The solution was: " + solution
                : "DEFEAT! You lost. The solution was: " + solution;

        log("GAME OVER - " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(humanWon ? "VICTORY!" : "DEFEAT");

        Label content = new Label(humanWon
                ? "Congratulations! You solved the mystery!\n\nThe solution was:\n" + solution
                : "Better luck next time!\n\nThe solution was:\n" + solution);
        content.setWrapText(true);
        content.setFont(Font.font("Arial", 14));

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle(
                "-fx-background-color: " + (humanWon ? "#d5f4e6" : "#fadbd8") + ";"
        );

        alert.showAndWait();
    }

    private String playersListString(List<Player> players) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i).getName());
            if (i < players.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {launch(args);}
}