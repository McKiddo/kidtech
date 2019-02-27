package game;

import engine.GameEngine;
import engine.IGameLogic;

public class Main {

    public static void main(String[] args) {
        try {
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEng = new GameEngine("kidTech test", 600, 480, true, gameLogic);
            gameEng.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }
    }
}
