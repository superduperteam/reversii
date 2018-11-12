package Exceptions;

public class PlayersInitPositionsOverrideEachOther extends Exception  {
    private final String errMessage = "Players initial positions override each other";

    @Override
    public String toString() {
        return errMessage;
    }
}