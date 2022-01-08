package nl.tudelft.sem.hiring.procedure.contracts;

import lombok.Data;

@Data
public class Position {
    private int posX;
    private int posY;

    public Position(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }
}
