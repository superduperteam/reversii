package GUI;

import GameEngine.Board;
import GameEngine.CellBoard;
import GameEngine.Disc;
import GameEngine.Point;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class BoardGUI extends ScrollPane {
    private BoardController boardController;
    private final GridPane gridPane;
    private final ColumnConstraints columnConstraints;
    private final RowConstraints rowConstraints;
    private CellBoardButton[][] cellBoardButtons;
    private final int rowsCount;
    private final int columnsCount;

    public BoardGUI(Board gameBoard, AppController appController) {
        boardController = new BoardController(appController, this);
        appController.setBoardController(boardController);
        gridPane = new GridPane();
        columnConstraints = new ColumnConstraints();
        rowConstraints = new RowConstraints();
        this.rowsCount = gameBoard.getHeight();
        this.columnsCount = gameBoard.getWidth();
        cellBoardButtons = new CellBoardButton[rowsCount][columnsCount];
        Disc currDisc;
        CellBoard currCellBoard;
        CellBoardButton currCellBoardButton;

        setFitToHeight(true);
        setFitToWidth(true);
        setMaxHeight(600); // used to be USE_PREF_
        setMaxWidth(600); // used to be USE_PREF_

        gridPane.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        gridPane.setGridLinesVisible(true);
        gridPane.setPrefHeight(USE_COMPUTED_SIZE);
        gridPane.setPrefWidth(USE_COMPUTED_SIZE);
        gridPane.setStyle("-fx-background-color: WHITE;");

        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(30.0);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < columnsCount; j++) {
                cellBoardButtons[i][j] = new CellBoardButton(i, j);
                currCellBoardButton = cellBoardButtons[i][j];

                GridPane.setHalignment(currCellBoardButton, javafx.geometry.HPos.CENTER);
                if (j != 0) {
                    GridPane.setColumnIndex(currCellBoardButton, j);
                }
                if (i != 0) {
                    GridPane.setRowIndex(currCellBoardButton, i);
                }

                currCellBoardButton.setMnemonicParsing(false);
                currCellBoardButton.setPrefHeight(5000.0);
                currCellBoardButton.setPrefWidth(5000.0);
                currCellBoardButton.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                currCellBoardButton.setOnMouseClicked((event) -> {
                    informCellBoardButtonsClicked((CellBoardButton) event.getSource());
                });
            }
        }

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < columnsCount; j++) {
                gridPane.getChildren().add(cellBoardButtons[i][j]);
            }
        }

        for (int i = 0; i < rowsCount; i++) {
            gridPane.getRowConstraints().add(rowConstraints);
        }
        for (int j = 0; j < columnsCount; j++) {
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < columnsCount; j++) {
//                ImageView imageView = new ImageView();
//
//                imageView.setNodeOrientation(javafx.geometry.NodeOrientation.INHERIT);
//                imageView.setImage(new Image(getClass().getResource("/resources/black-disc.png").toExternalForm()));
//                buttons[i][j].setGraphic(imageView);
//
//                imageView.fitHeightProperty().bind(buttons[i][j].heightProperty());
//                imageView.fitWidthProperty().bind(buttons[i][j].widthProperty());
                currDisc = gameBoard.getDisc(i, j);
                currCellBoardButton = cellBoardButtons[i][j];
                currCellBoard = gameBoard.get(i, j);

                // boardController.discTypeToColor(currDisc.getType())

                if (currDisc != null) {
                    Circle circle = new Circle(50, 50, 40, boardController.discTypeToColor(currDisc.getType()));
                    currCellBoardButton.setGraphic(circle);
                    currCellBoardButton.setContentDisplay(ContentDisplay.CENTER);

                    circle.radiusProperty().bind(Bindings.min(currCellBoardButton.heightProperty().divide(4), currCellBoardButton.widthProperty().divide(4)));
                }
//                Image image = new Image("/resources/black-disc.png", buttons[i][j].getWidth(), buttons[i][j].getHeight(), false, true, true);
//                BackgroundImage bImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(buttons[i][j].getWidth(), buttons[i][j].getHeight(), true, true, true, false));
//
//                Background backGround = new Background(bImage);
//                buttons[i][j].setBackground(backGround);


//                BackgroundImage backgroundImage = new BackgroundImage( new Image( getClass().getResource("/resources/black-disc.png").toExternalForm()), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
//                Background background = new Background(backgroundImage);

//                buttons[i][j].setBackground(background);
            }
        }

        setContent(gridPane);
        gridPane.autosize();
    }

    public void informCellBoardButtonsClicked(CellBoardButton clickedButton){
        boardController.CellBoardButtonClicked(new Point(clickedButton.getRow(), clickedButton.getColumn()));
    }

    public void updateBoard(Board gameBoard, boolean isTutorialMode) {
        Disc currDisc;
        CellBoard currCellBoard;
        CellBoardButton currButton;
        Circle currGUIDisc;

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < columnsCount; j++) {
                currDisc = gameBoard.getDisc(i, j);
                currButton = cellBoardButtons[i][j];
                currCellBoard = gameBoard.get(i, j);

                // boardController.discTypeToColor(currDisc.getType())

                if (currDisc != null) {
                    if (currButton.getGraphic() == null) {
                        currGUIDisc = new Circle(50, 50, 40, boardController.discTypeToColor(currDisc.getType()));
                        currButton.setGraphic(currGUIDisc);
                        currButton.setContentDisplay(ContentDisplay.CENTER);
                        currGUIDisc.radiusProperty().bind(Bindings.min(currButton.heightProperty().divide(4), currButton.widthProperty().divide(4)));
                    }
                    else { // the circle already there, just changing it's color..
                        currGUIDisc = (Circle) currButton.getGraphic();
                        currGUIDisc.setFill(boardController.discTypeToColor(currDisc.getType()));
                    }
                }
                else{
                    if(currButton.getGraphic() != null){ // need to remove the circle disc.
                        currButton.setGraphic(null);
                    }
                }

                if(isTutorialMode){
                    if(currCellBoard.getCountOfFlipsPotential() != 0){
                        currButton.setText(String.valueOf(currCellBoard.getCountOfFlipsPotential()));
                    }
                    else{
                        currButton.setText("");
                    }
                }
                else{
                    currButton.setText("");
                }
            }
        }

        currButton = null;
    }
}
