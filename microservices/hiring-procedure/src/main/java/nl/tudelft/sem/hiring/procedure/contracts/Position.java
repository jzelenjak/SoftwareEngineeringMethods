package nl.tudelft.sem.hiring.procedure.contracts;

import lombok.Data;

@Data
public class Position {
    private int xPos;
    private int yPos;

    public Position(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }
}
