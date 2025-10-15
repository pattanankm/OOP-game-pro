package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MakeAWish implements Screen {
    private final Main game;
    private SpriteBatch batch;
    private Texture bg;
    private Texture wishShrine;
    private Texture chatBox;
    private BitmapFont font;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    // ระบบเลือกพร
    private Rectangle[] wishButtons = new Rectangle[3];
    private String[] wishes = {
        "1. No F grade this semester!",
        "2. Pass all exams with ease!",
        "3. Find true love before graduation!"
    };

    private boolean wishSelected = false;
    private int selectedWish = -1;

    public MakeAWish(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();

        // โหลดภาพพื้นหลังและศาลช้าง
        bg = new Texture("BG/samchang.png");
        wishShrine = new Texture("NPC/elephant_Stand.PNG");
        chatBox = new Texture("BG/Chat_BB.png");

        // ตำแหน่งปุ่มเลือกพร
        for (int i = 0; i < 3; i++) {
            wishButtons[i] = new Rectangle(300, 400 - i * 80, 700, 60);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาดพื้นหลังแบบเต็มจอ
        float viewportWidth = viewport.getWorldWidth();
        float viewportHeight = viewport.getWorldHeight();
        float bgRatio = (float) bg.getWidth() / bg.getHeight();
        float viewportRatio = viewportWidth / viewportHeight;

        float drawWidth, drawHeight, drawX, drawY;

        if (viewportRatio > bgRatio) {
            drawWidth = viewportWidth;
            drawHeight = drawWidth / bgRatio;
            drawX = 0;
            drawY = (viewportHeight - drawHeight) / 2;
        } else {
            drawHeight = viewportHeight;
            drawWidth = drawHeight * bgRatio;
            drawX = (viewportWidth - drawWidth) / 2;
            drawY = 0;
        }

        batch.draw(bg, drawX, drawY, drawWidth, drawHeight);

        // วาดศาลช้าง
        batch.draw(wishShrine, WORLD_WIDTH / 2f - 150, WORLD_HEIGHT / 2f - 100, 300, 300);

        // หัวข้อ
        font.getData().setScale(2.5f);
        font.draw(batch, "MAKE YOUR WISH AT THE SHRINE!", 350, 600);
        font.getData().setScale(1.8f);

        // ปุ่มเลือกพร
        for (int i = 0; i < 3; i++) {
            batch.draw(chatBox, wishButtons[i].x, wishButtons[i].y, wishButtons[i].width, wishButtons[i].height);
            font.draw(batch, wishes[i], wishButtons[i].x + 50, wishButtons[i].y + 35);
        }

        // ข้อความแนะนำ
        font.getData().setScale(1.5f);
        if (!wishSelected) {
            font.draw(batch, "Click on your choice or press 1, 2, 3!", 450, 250);
        } else {
            font.draw(batch, "Wish selected: " + wishes[selectedWish], 350, 250);
            font.draw(batch, "Press ENTER to continue", 500, 200);
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {
        // คลิกเมาส์เลือกพร
        if (Gdx.input.justTouched() && !wishSelected) {
            Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click);
            for (int i = 0; i < 3; i++) {
                if (wishButtons[i].contains(click.x, click.y)) {
                    selectWish(i);
                    break;
                }
            }
        }

        // ปุ่มตัวเลขเลือกพร
        if (!wishSelected) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                selectWish(0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                selectWish(1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
                selectWish(2);
            }
        }

        // ✨ แก้ไขส่วนนี้: หลังเลือกพร กด ENTER ไปหน้า ShootScreen
        if (wishSelected) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.selectedWish = selectedWish + 1;
                System.out.println("Wish confirmed: " + wishes[selectedWish]);
                // เปลี่ยนไปหน้า ShootScreen แทน firstScreen
                game.setScreen(new ShootScreen(game));
            }
        }
    }

    private void selectWish(int wishIndex) {
        selectedWish = wishIndex;
        wishSelected = true;
        System.out.println("Wish selected: " + wishes[wishIndex]);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        bg.dispose();
        font.dispose();
        chatBox.dispose();
        if (wishShrine != null) wishShrine.dispose();
    }
}
