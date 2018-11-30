package GameEngine;

import java.io.Serializable;
import java.util.*;

public class GameManager implements Serializable
{
    private eGameMode gameMode;
    private HashMap<eDiscType, Player> discTypeToPlayer;
    private TurnHistory turnHistory;
    private List<Player> playersList;
    private int activePlayerIndex;
    private Player activePlayer;
    private Board board;
    private TurnHistory.Turn currTurn;
    private boolean isGameActive;
    private Map<Point, Integer> PointToFlipPotential;

    public GameManager(eGameMode gameMode, List<Player> playersList, Board board)
    {
        turnHistory = new TurnHistory();
        this.playersList = new ArrayList<>(playersList);
        activePlayerIndex = 0;
        mapDiscTypesToPlayers();
        this.gameMode = gameMode;
        activePlayer = playersList.get(0);
        this.board = board;
        isGameActive = false;
        calcFlipPotential();
//        currTurn = getCurrentTurn(); // ##
    }

    public void calcFlipPotential(){
        Point currPoint;
        int flipPotential;

        PointToFlipPotential = new HashMap<>();
        for(int row = 0; row < board.getHeight(); ++row){
            for(int col = 0; col < board.getWidth(); ++col){
                currPoint = new Point(row, col);
                if(board.get(row, col) != null){
                    flipPotential = 0;
                }
                else {
                    flipPotential = board.checkFlipPotential(currPoint, getActivePlayer().getDiscType());
                }

                PointToFlipPotential.put(currPoint, flipPotential);
            }
        }
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public List<Player> getPlayersList()
    {
        return playersList;
    }

    public eGameMode getGameMode() {
        return gameMode;
    }

    private void setActivePlayerToBeNextPlayer()
    {
        activePlayerIndex = (activePlayerIndex + 1)%(playersList.size());
        activePlayer = playersList.get(activePlayerIndex);
    }

    public Board getInitialBoard()
    {
        int boardHeight = board.getHeight();
        int boardWidth = board.getWidth();
        HashMap<Player, List<Point>> initialDiscPositionsOfPlayer = board.getInitialDiscPositionOfPlayers();
        return new Board(boardHeight, boardWidth, initialDiscPositionsOfPlayer, gameMode);
    }

    // Call this only after game is over.
    // should be test again when more than 2 players are allowed in a game.
    public List<Player> getHighestScoringPlayers()
    {
        List<Player> highestScoringPlayers = new ArrayList<>();
        int maxPlayerPoints = playersList.get(0).getScore();
        highestScoringPlayers.add(playersList.get(0));

        for(Player player : playersList)
        {
            if(player.getScore() > maxPlayerPoints)
            {
                maxPlayerPoints = player.getScore();
                highestScoringPlayers.clear(); // everybody that is in the list is not relevant anymore..
                highestScoringPlayers.add(player);
            }
            else if(player.getScore() == maxPlayerPoints)
            {
                if(!highestScoringPlayers.contains(player)) // maybe it's the first
                    highestScoringPlayers.add(player);
            }
        }

        return highestScoringPlayers;
    }

    public boolean isGameOver(){
        return !board.areThereAnyMovesForPlayers(playersList) || playersList.size() == 1;
    }

    public Board getBoard()
    {
        return board;
    }

    public void resetGame()
    {
        if(!turnHistory.turnHistoryStack.isEmpty())
        {
            List<TurnHistory.Turn> turnsList = new ArrayList<>(turnHistory.turnHistoryStack);
            goBackToTurn(turnsList.get(0));
            turnHistory.turnHistoryStack.clear();
            isGameActive = false;
        }

        playersList.forEach(player -> player.getStatistics().resetStatistics());
    }

    public void changeTurn()
    {
        addTurnToHistory(currTurn);
        setActivePlayerToBeNextPlayer();

        updateGameScore();
        calcFlipPotential();
        currTurn = getCurrentTurn();
    }

    public void retirePlayerFromGame(Player quitter) // in ex2 it is only possible to quit when it is your turn
    {
        if(activePlayer == quitter){ // in ex 3 - a player can retire at any time.
            currTurn.retiredPlayer = quitter;
            addTurnToHistory(currTurn);
            discTypeToPlayer.remove(quitter.getDiscType());
            playersList.remove(quitter);
            activePlayer = playersList.get(activePlayerIndex % playersList.size());
            currTurn = getCurrentTurn();
        }
    }

    public Player getActivePlayer()
    {
        return activePlayer;
    }

    private void mapDiscTypesToPlayers()
    {
        eDiscType discs[];
        int currentDiscIndex = 0;

        discTypeToPlayer = new HashMap<>();
        discs = eDiscType.values();

        for(Player player : playersList)
        {
            discTypeToPlayer.put(discs[currentDiscIndex], player);
          //  player.SetDiscType(discs[currentDiscIndex]);  Commented because player's disc type is given in Player c'tor
            currentDiscIndex++;
        }
    }

    public void updateGameScore()
    {
        int height = board.getHeight(), width = board.getWidth();
        Disc currDisc;
        Player playerToAddScoreTo;

        for(Player player: playersList) {
            Player.Statistics  statistics = player.getStatistics();

            statistics.resetScore();
        }

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j){
                currDisc = board.get(i,j);

                if(currDisc != null)
                {
                    if(discTypeToPlayer.containsKey(currDisc.getType()))
                    {
                        playerToAddScoreTo = discTypeToPlayer.get(currDisc.getType());
                        playerToAddScoreTo.getStatistics().incScore();
                    }
                }
            }
        }
    }

    public static class TurnHistory implements  Serializable {
        private Stack<Turn> turnHistoryStack;

        public TurnHistory(){
            turnHistoryStack = new Stack<>();
        }

        public static class Turn implements  Serializable {
            private List<Player> playersList;
            private Player activePlayer;
            private Board board;
            private HashMap<eDiscType, Player> discTypeToPlayer;
            private Player retiredPlayer = null;
            private HashMap<Point, Integer> PointToFlipPotential;

            //the method clones the last turn using copy constructors.
            public Turn(Board board, Player activePlayer, List<Player> players,
                        Map<Point,Integer> pointToFlipPotential) {
                playersList = new LinkedList<>();
                this.board = new Board(board);
                this.discTypeToPlayer = new HashMap<>();
                PointToFlipPotential = new HashMap<>();

                for(Player player: players) {
                    Player copiedPlayer = new Player(player);

                    if(activePlayer.equals(player)){
                        this.activePlayer = copiedPlayer;
                    }

                    playersList.add(copiedPlayer);
                    this.discTypeToPlayer.put(copiedPlayer.getDiscType(), copiedPlayer);
                }
                copyPointToFlipPotentialMap(pointToFlipPotential);
            }

            private void copyPointToFlipPotentialMap(Map<Point, Integer> pointToFlipPotential){
                for(Point point : pointToFlipPotential.keySet()){
                    Point pointCopy = new Point(point.getRow(), point.getCol());
                    int flipPotentialCopy = pointToFlipPotential.get(point);

                    PointToFlipPotential.put(pointCopy, flipPotentialCopy);
                }
            }
        }

        public void addHistoryEntry(Turn turn) {
            turnHistoryStack.push(turn);
        }

        //if there are no turns in the stack: returns null
        public Turn getLastTurn() {
            if(turnHistoryStack.isEmpty()) {
                return null;
            }

            return turnHistoryStack.pop();
        }
    }

    private TurnHistory.Turn getCurrentTurn() {
        TurnHistory.Turn turn = new TurnHistory.Turn(board, activePlayer, playersList, PointToFlipPotential);

        return turn;
    }

    public List<Board> getHistoryOfBoardStates()
    {
        List<TurnHistory.Turn> turnsList = new ArrayList<>(turnHistory.turnHistoryStack);
        List<Board> boardsList = new ArrayList<>();

        for(TurnHistory.Turn turn : turnsList)
        {
            boardsList.add(turn.board);
        }

        boardsList.add(board);

        return boardsList;
    }

    private void addTurnToHistory(TurnHistory.Turn turnToAdd){

        turnHistory.addHistoryEntry(turnToAdd);
    }

    //if undo failed (there are no turns to undo): returns false
    public boolean undo(){
        boolean didUndoFailed = false;
        TurnHistory.Turn lastTurn = turnHistory.getLastTurn();

        if(lastTurn == null) {
            didUndoFailed = true;
        }
        else {
            goBackToTurn(lastTurn);
        }

        return didUndoFailed;
    }

    private void goBackToTurn(TurnHistory.Turn turnToChangeTo)
    {
        gameMode = turnToChangeTo.board.getGameMode();
        discTypeToPlayer = turnToChangeTo.discTypeToPlayer;
        playersList = turnToChangeTo.playersList;
        activePlayer = turnToChangeTo.activePlayer;
        activePlayerIndex = playersList.indexOf(activePlayer);
        board = turnToChangeTo.board;
        currTurn = getCurrentTurn();
        currTurn.retiredPlayer = turnToChangeTo.retiredPlayer;
        PointToFlipPotential = turnToChangeTo.PointToFlipPotential;
    }

    public Player getReturnedRetiredPlayer()
    {
        return currTurn.retiredPlayer;
    }

    public enum eGameMode
    {
        Regular, Islands
    }

    public enum eMoveStatus
    {
        OK,
        CELL_IS_ALREADY_TAKEN {            @Override
        public String toString() {
            return new String("Cell is already taken!");
        }},
        ILLEGAL_ISLAND {
            public String toString() {
            return new String("The new disc should be adjacent to other discs on board!");
        }},
        POINT_IS_NOT_IN_RANGE_OF_BOARD{
            public String toString() {
            return new String("The coordinates are not in the board's range!");
        }}
    }

    // call this only after all info about players is gathered.
    public void activateGame()
    {
        isGameActive = true;
        currTurn = getCurrentTurn(); // ##
        updateGameScore();
    }
}
