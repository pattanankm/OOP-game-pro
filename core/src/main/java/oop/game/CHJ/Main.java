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
    public int selectedWish;

    @Override
    public void create() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new FitViewport(8, 5);
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        questManager = new QuestManager();

        // ✅ สร้างหน้าจอที่ต้องใช้
        mainMenuScreen = new MainMenuScreen(this);
        firstScreen    = new FirstScreen(this);

        // ✅ เริ่มที่เมนูหลัก
        setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (pixel != null) pixel.dispose();
    }

    /** เรียกตอนเริ่มเกม */
    public void goToFirstScreen() {
        if (firstScreen == null) firstScreen = new FirstScreen(this);
        setScreen(firstScreen);
    }
}
