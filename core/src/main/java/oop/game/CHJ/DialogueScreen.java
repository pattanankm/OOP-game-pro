package oop.game.CHJ;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DialogueScreen implements Screen {
    private final Main game;
    private final NPC npc;
    private SpriteBatch batch;
    private Texture bg;
    private Texture chatBox;
    private BitmapFont font;
    private Texture npcImage;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private DialogueState[] states;
    private int dialogueIndex = 0;

    public DialogueScreen(Main game, NPC npc) {
        this.game = game;
        this.npc = npc;
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

        // โหลดภาพกล่องแชท
        chatBox = new Texture("BG/Chat_BB.png");

        switch (npc.name) {
            case "Penguin":
                bg = new Texture("BG/Lib_BG.png");
                npcImage = new Texture("NPC/Penguin_Stand.png");
                states = new DialogueState[]{
                    new DialogueState("Brrr... It's cold today!", new Texture("NPC/Penguin_Stand.png"), 200, 120),
                    new DialogueState("Wanna fish with me?", new Texture("NPC/Giraffe_Stand.png"), 250, 250),
                    new DialogueState("Be careful, the ice is slippery!", new Texture("NPC/Penguin_Stand.png"), 220, 120)
                };
                game.questManager.startQuest1(); //เริ่มเควส 1 หลังคุยเพนกวิน
                break;

            case "Giraffe":
                bg = new Texture("BG/FoE_BG.png");
                npcImage = new Texture("NPC/Giraffe_Stand.png");
                states = new DialogueState[]{
                    new DialogueState("Brrr... It's cold today!", new Texture("NPC/Penguin_Stand.png"), 200, 120),
                    new DialogueState("Wanna fish with me?", new Texture("NPC/Giraffe_Stand.png"), 250, 120),
                    new DialogueState("Be careful, the ice is slippery!", new Texture("NPC/Penguin_Stand.png"), 220, 120)
                };
                game.questManager.startQuest2(); //เริ่มเควส 2 หลังคุยยีราฟ
                break;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาดพื้นหลังแบบ cover (เต็มจอไม่มีขอบดำ)
        float viewportWidth = viewport.getWorldWidth();
        float viewportHeight = viewport.getWorldHeight();
        float bgRatio = (float) bg.getWidth() / bg.getHeight();
        float viewportRatio = viewportWidth / viewportHeight;

        float drawWidth, drawHeight, drawX, drawY;

        if (viewportRatio > bgRatio) {
            // viewport กว้างกว่า bg → ขยายความกว้างให้เต็ม
            drawWidth = viewportWidth;
            drawHeight = drawWidth / bgRatio;
            drawX = 0;
            drawY = (viewportHeight - drawHeight) / 2;
        } else {
            // viewport สูงกว่า bg → ขยายความสูงให้เต็ม
            drawHeight = viewportHeight;
            drawWidth = drawHeight * bgRatio;
            drawX = (viewportWidth - drawWidth) / 2;
            drawY = 0;
        }

        batch.draw(bg, drawX, drawY, drawWidth, drawHeight);

        // 🔹 วาดกล่องแชท (ขยับตำแหน่งเอง)
        float chatBoxX = 40;
        float chatBoxY = -30;
        float chatBoxWidth = 1200;
        float chatBoxHeight = 230;

        // 🔹 วาดข้อความปัจจุบัน
        DialogueState current = states[dialogueIndex];
        batch.draw(current.image, current.x, current.y,
            current.image.getWidth()/2,
            current.image.getHeight()/2);
        batch.draw(chatBox, chatBoxX, chatBoxY, chatBoxWidth, chatBoxHeight);
        font.draw(batch, current.text, 80, 150);

        // 🔹 วาดข้อความ “Continue” ด้านล่าง
        font.getData().setScale(1.2f);
        font.draw(batch, "[Tap or Press ENTER to continue]", chatBoxX + 880, chatBoxY + 60);
        font.getData().setScale(2f);

        batch.end();

        // ====== ตรวจคลิกบน chat icon ======
        if (Gdx.input.justTouched()) {
            // หาตำแหน่งคลิกในแกน world
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click); // แปลงพิกัดจากจอ -> world

            if (click.x >= chatBoxX && click.x <= chatBoxX + chatBoxWidth &&
                click.y >= chatBoxY && click.y <= chatBoxY + chatBoxHeight) {
                dialogueIndex++;
                if (dialogueIndex >= states.length) {
                    game.setScreen(game.firstScreen);
                }
            }
        }

        //กด Enter เพื่อไปบรรทัดถัดไป
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            dialogueIndex++;
            if (dialogueIndex >= states.length) {
                game.setScreen(game.firstScreen); // จบบทสนทนา → กลับไปหน้าหลัก
            }
        }

        // ปิดหน้าพูดคุย
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(game.firstScreen);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        bg.dispose();
        if (npcImage != null) npcImage.dispose();
        font.dispose();
        chatBox.dispose();
    }
}
