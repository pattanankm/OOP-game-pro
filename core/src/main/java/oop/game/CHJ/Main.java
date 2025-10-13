package oop.game.CHJ;

import com.badlogic.gdx.Game;

public class Main extends Game {

    public QuestManager questManager;
    public FirstScreen firstScreen;

    @Override
    public void create() {
        questManager = new QuestManager();
        firstScreen = new FirstScreen(this); // สร้างไว้ครั้งเดียว
        setScreen(firstScreen); //this = ส่งเกมเข้ามา
    }

    @Override
    public void render() {
        super.render(); // อย่าลืมเรียกของ Game ด้วย
    }
}
