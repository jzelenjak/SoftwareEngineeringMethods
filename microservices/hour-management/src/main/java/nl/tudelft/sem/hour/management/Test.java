package nl.tudelft.sem.hour.management;

import lombok.Data;

@Data
public class Test {
    private int id;

    private String sadness;

    public Test(int id, String sadness) {
        this.id = id;
        this.sadness = sadness;
    }
}
