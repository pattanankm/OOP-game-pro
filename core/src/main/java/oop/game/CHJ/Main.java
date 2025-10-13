package oop.game.CHJ;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends Game {
    public QuestManager questManager;
    public FirstScreen firstScreen;
    public MainMenuScreen mainMenuScreen;
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;

    public String playerName = "";
    public String selectedGender = "";
    public Texture pixel;


    @Override
    public void create() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        batch = new SpriteBatch();
        // use libGDX's default font
        font = new BitmapFont();
        viewport = new FitViewport(8, 5);

        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        this.setScreen(new MainMenuScreen(this));


        questManager = new QuestManager();

        // สร้างหน้าจอที่ต้องใช้
        mainMenuScreen = new MainMenuScreen(this);
        firstScreen    = new FirstScreen(this);

        // เริ่มที่เมนูหลัก
        setScreen(mainMenuScreen);
    }

//    public Texture pixel;

    public void render() {
        super.render(); // important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    /** เรียกตอนเงื่อนไขสำเร็จจาก MainMenuScreen */
    public void goToFirstScreen() {
        if (firstScreen == null) firstScreen = new FirstScreen(this);
        setScreen(firstScreen);
    }
}
