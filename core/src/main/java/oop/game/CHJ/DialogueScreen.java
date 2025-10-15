package oop.game.CHJ;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;

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
            case "Giraffe":
                bg = new Texture("BG/FoE_BG.png");
                states = new DialogueState[]{
                    new DialogueState("Giraffe: Hello! Welcome to the Engineering Faculty!", new Texture("NPC/Giraffe_Stand.png"), 200, 120),
                    new DialogueState("Student: Hi! This place looks amazing!", new Texture("NPC/Student1.png"), 800, 120),
                    new DialogueState("Giraffe: That's right! Here, we have the 'Gear' as our symbol.", new Texture("NPC/Giraffe1.png"), 200, 120),
                    new DialogueState("Student: The Gear? What does it represent?", new Texture("NPC/Student2.png"), 800, 120),
                    new DialogueState("Giraffe: It represents engineering spirit and teamwork!", new Texture("NPC/Giraffe2.png"), 200, 120),
                    new DialogueState("Student: That sounds inspiring!", new Texture("NPC/Student3.png"), 800, 120),
                    new DialogueState("Giraffe: If you're ready, let me show you around!", new Texture("NPC/Giraffe3.png"), 200, 120),
                    new DialogueState("Student: I'd love that! Let's go!", new Texture("NPC/Student4.png"), 800, 120)
                };
                game.questManager.startQuest1();
                break;

            case "Penguin":
                bg = new Texture("BG/Lib_BG.png");
                states = new DialogueState[]{
                    new DialogueState("Penguin: Brrrr It's so cold today!", new Texture("NPC/Penguin_Stand.png"), 200, 120),
                    new DialogueState("Student: You're right! It's freezing.", new Texture("NPC/Student1.png"), 800, 120),
                    new DialogueState("Penguin: Exactly! Please turn off the AC, I'm freezing!", new Texture("NPC/Penguin3.png"), 200, 120),
                    new DialogueState("Student: Okay, I'll go turn it off.", new Texture("NPC/Student2.png"), 800, 120),
                };
                game.questManager.startQuest2();
                break;

            case "Elephant":
                bg = new Texture("BG/samchang.png");
                states = new DialogueState[]{
                    new DialogueState("Elephant: Welcome to the Elephant Shrine!", new Texture("NPC/elephant_Stand.PNG"), 200, 120),
                    new DialogueState("Student: Wow! This is the famous Elephant Shrine!", new Texture("NPC/Student1.png"), 800, 120),
                    new DialogueState("Elephant: Yes, this shrine has a long history.", new Texture("NPC/elephant2.PNG"), 200, 120),
                    new DialogueState("Student: I heard it's very sacred here.", new Texture("NPC/Student2.png"), 800, 120),
                    new DialogueState("Elephant: Students often come here to make wishes about their studies.", new Texture("NPC/elephant3.PNG"), 200, 120),
                    new DialogueState("Student: Really? Do the wishes come true?", new Texture("NPC/Student3.png"), 800, 120),
                    new DialogueState("Elephant: Absolutely! Everyone who makes wishes passes their exams.", new Texture("NPC/elephant4.PNG"), 200, 120),
                    new DialogueState("Student: Then I should make a wish too!", new Texture("NPC/Student4.png"), 800, 120),
                    new DialogueState("Elephant: Great! You can start making your wish now. Good luck!", new Texture("NPC/elephant_Stand.PNG"), 200, 120)
                };
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
        // ตรวจสอบว่าเป็นภาพช้างหรือไม่
        if (current.text.startsWith("Elephant:")) {
            // วาดช้างขนาดใหญ่ 400x400
            batch.draw(current.image, current.x, current.y, 400, 400);
        } else {
            // วาด NPC อื่นขนาดปกติ
            batch.draw(current.image, current.x, current.y,
                current.image.getWidth()/2,
                current.image.getHeight()/2);
        }
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

    public static class AudioManager {
        private HashMap<String, Sound> sounds;
        private Music bgm;

        public AudioManager() {
            sounds = new HashMap<>();
            loadSounds();
        }

        private void loadSounds() {
            try {
                sounds.put("shoot", Gdx.audio.newSound(Gdx.files.internal("audio/Shoot.mp3")));
                sounds.put("win", Gdx.audio.newSound(Gdx.files.internal("audio/Win.mp3")));
                sounds.put("lose", Gdx.audio.newSound(Gdx.files.internal("audio/Lose.mp3")));
                bgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Hero.mp3"));
            } catch (Exception e) {
                System.out.println("Audio files not found, continuing without audio");
            }
        }

        public void playSound(String soundName) {
            if (sounds.containsKey(soundName)) {
                sounds.get(soundName).play(0.7f);
            }
        }

        public void playBGM() {
            if (bgm != null) {
                bgm.setLooping(true);
                bgm.setVolume(0.3f);
                bgm.play();
            }
        }

        public void stopBGM() {
            if (bgm != null) {
                bgm.stop();
            }
        }

        public void dispose() {
            for (Sound sound : sounds.values()) {
                sound.dispose();
            }
            if (bgm != null) {
                bgm.dispose();
            }
        }
    }

    public static class Gear {
        private Texture texture;
        private Vector2 position;
        private float rotation;
        private float scale;
        private float rotationSpeed;

        public Gear(float x, float y) {
            try {
                texture = new Texture(Gdx.files.internal("images/gear.PNG"));
                position = new Vector2(x, y);
                rotation = 0;
                scale = 1.0f;
                rotationSpeed = 30 + (float)Math.random() * 60;
            } catch (Exception e) {
                Gdx.app.error("Gear", "Cannot load gear texture: " + e.getMessage());
            }
        }

        public void update(float delta) {
            rotation += delta * rotationSpeed;
            scale = 0.8f + (float)Math.sin(Gdx.graphics.getFrameId() * 0.05f + position.x) * 0.3f;
        }

        public void render(SpriteBatch batch) {
            if (texture == null) return;

            float width = texture.getWidth();
            float height = texture.getHeight();

            batch.draw(texture,
                position.x - width/2, position.y - height/2,
                width/2, height/2,
                width, height,
                scale, scale,
                rotation,
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false
            );
        }

        public void dispose() {
            if (texture != null) texture.dispose();
        }
    }
}
