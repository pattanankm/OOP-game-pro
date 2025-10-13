package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

public class FirstScreen implements Screen {
    private final Main game;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Texture frontTex, currentTex, gearIcon, chatIcon, handIcon;
    private Texture[] frontWalk, backWalk, leftWalk, rightWalk, Jump;

    private float playerX, playerY;
    private float speed = 160f;

    private final float MIN_X = 800, MAX_X = 3300, MIN_Y = 560, MAX_Y = 2740;

    private Array<Rectangle> collisionRects;
    private Array<NPC> npcs;

    private final float unitScale = 1f / 2.5f;

    private int currentFrame = 0;
    private float walkTime = 0f;
    private float frameDuration = 0.2f;
    private boolean isJumping = false;

    private BitmapFont font;
    private GlyphLayout nameLayout = new GlyphLayout();
    private boolean objectVisible = false;

    // เพลง
    private Music bgmFirst;     // Music/StageMusic/FirstScreen.mp3
    private Music walkMusic;    // Music/Walk/Walk.mp3
    private boolean walkingSoundPlaying = false;
    private boolean isMoving = false;  // ใช้ควบคุมแสดงชื่อเฉพาะตอนวิ่ง

    public FirstScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        map = new TmxMapLoader().load("Map/Map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() * unitScale, Gdx.graphics.getHeight() * unitScale);
        camera.update();

        font = new BitmapFont();
        batch = new SpriteBatch();

        chatIcon = new Texture("Icons/Chat_Icon.png");
        handIcon = new Texture("Icons/Hand_Icon.png");
        gearIcon = new Texture("Icons/gear.png"); // ✅ ให้ตรงกับไฟล์จริง

        collisionRects = new Array<>();
        npcs = new Array<>();

        // ตัวอย่าง NPC
        NPC penguin = new NPC(2358, 2340, "NPC/Penguin_Stand.png", "Today is very cold~", "Penguin");
        npcs.add(penguin);
        NPC giraffe = new NPC(1400, 1700, "NPC/Giraffe_Stand.png", "We are all Entaneer!", "Giraffe");
        npcs.add(giraffe);

        // Collision
        MapLayer objectLayer = map.getLayers().get("Collision");
        if (objectLayer != null) {
            for (MapObject obj : objectLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle r = ((RectangleMapObject) obj).getRectangle();
                    r.set(r.x * unitScale, r.y * unitScale, r.width * unitScale, r.height * unitScale);
                    collisionRects.add(r);
                }
            }
        }

        // โหลดสปรाइटเดิน
        frontWalk = new Texture[] {
            new Texture("CharMove/Girl_FrontLeft1.png"),
            new Texture("CharMove/Girl_FrontStand.png"),
            new Texture("CharMove/Girl_FrontRight1.png")
        };
        backWalk = new Texture[] {
            new Texture("CharMove/Girl_Back.png"),
            new Texture("CharMove/Girl_LeftBack.png")
        };
        rightWalk = new Texture[] {
            new Texture("CharMove/Girl_Right.png"),
            new Texture("CharMove/Girl_RightStand.png"),
            new Texture("CharMove/Girl_Right1.png")
        };
        leftWalk = new Texture[] {
            new Texture("CharMove/Girl_Left.png"),
            new Texture("CharMove/Girl_LeftStand.png"),
            new Texture("CharMove/Girl_Left1.png")
        };
        Jump = new Texture[] { new Texture("CharMove/Girl_Jump.png") };

        frontTex  = new Texture("CharMove/Girl_Front.png");
        currentTex = frontTex;

        // 🔊 เพลงคลอฉาก
        bgmFirst = Gdx.audio.newMusic(Gdx.files.internal("Music/StageMusic/FirstScreen.mp3"));
        bgmFirst.setLooping(true);
        bgmFirst.setVolume(0.28f);
        bgmFirst.play();

        // 👣 เสียงเดิน
        walkMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Walk/Walk.mp3"));
        walkMusic.setLooping(true);
        walkMusic.setVolume(0.55f);

        // จุดเกิด
        int mapW = map.getProperties().get("width", Integer.class);
        int mapH = map.getProperties().get("height", Integer.class);
        int tilePixel = map.getProperties().get("tilewidth", Integer.class);
        playerX = (mapW * tilePixel * 0.495f) / 2.5f;
        playerY = (mapH * tilePixel * 0.48f ) / 2.5f;

        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        renderer.setView(camera);
        renderer.render(new int[]{2,3,4,5});

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาด NPC
        for (NPC npc : npcs) {
            batch.draw(npc.texture, npc.x, npc.y, npc.width, npc.height);
            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                // แสดงไอคอนคุย
                batch.draw(chatIcon, npc.x + 55, npc.y + 60, 24, 24);
            }
        }

        // วาดวัตถุ (ตัวอย่าง icon เฟือง)
        if (objectVisible) batch.draw(gearIcon, 1650, 860, 40, 40);

        // วาดตัวละคร
        float tileSize = 10f;
        float drawW = currentTex.getWidth() / tileSize;
        float drawH = currentTex.getHeight() / tileSize;
        batch.draw(currentTex, playerX, playerY, drawW, drawH);

        // ชื่อผู้เล่น “เฉพาะตอนกำลังวิ่ง”
        if (isMoving && game.playerName != null && !game.playerName.isEmpty()) {
            nameLayout.setText(font, game.playerName);
            float nameX = playerX + (drawW - nameLayout.width) / 2f;
            float nameY = playerY - 10f;

            // เงาดำบาง ๆ
            font.setColor(0,0,0,0.85f);
            font.draw(batch, game.playerName, nameX + 1, nameY - 1);
            font.draw(batch, game.playerName, nameX - 1, nameY + 1);

            // ตัวอักษรขาว
            font.setColor(1,1,1,1);
            font.draw(batch, game.playerName, nameX, nameY);
        }

        batch.end();

        // เลเยอร์บนสุด (หลังคา/ต้นไม้ชั้นบน)
        renderer.render(new int[]{6});
    }

    private Texture[] previousWalkFrames = null;

    private void handleInput(float delta) {
        float nextX = playerX, nextY = playerY;
        boolean moving = false;
        Texture[] walkFrames = null;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) { nextY += speed * delta; moving = true; walkFrames = backWalk; }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) { nextY -= speed * delta; moving = true; walkFrames = frontWalk; }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) { nextX -= speed * delta; moving = true; walkFrames = leftWalk; }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) { nextX += speed * delta; moving = true; walkFrames = rightWalk; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            isJumping = true;
            currentTex = Jump[0];
            Timer.schedule(new Timer.Task() {
                @Override public void run() {
                    isJumping = false;
                    if (!(Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN)
                        || Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))) {
                        currentTex = frontTex;
                    }
                }
            }, 0.3f);
        }

        // แอนิเมชันเดิน
        if (walkFrames != previousWalkFrames) { currentFrame = 0; walkTime = 0f; previousWalkFrames = walkFrames; }
        if (moving && walkFrames != null && !isJumping) {
            walkTime += delta;
            if (walkTime >= frameDuration) { walkTime = 0f; currentFrame = (currentFrame + 1) % walkFrames.length; }
            currentTex = walkFrames[currentFrame];
        } else {
            currentFrame = 0;
        }

        // ชนสิ่งกีดขวาง
        float playerWidth  = currentTex.getWidth() / 10f;
        float playerHeight = currentTex.getHeight() / 10f * 0.4f;
        float offsetX = (currentTex.getWidth() / 10f - playerWidth) / 2f;
        float offsetY = 0f;

        Rectangle rectX = new Rectangle(nextX + offsetX, playerY + offsetY, playerWidth, playerHeight);
        boolean collidedX = false;
        for (Rectangle r : collisionRects) { if (rectX.overlaps(r)) { collidedX = true; break; } }
        if (!collidedX) playerX = nextX;

        Rectangle rectY = new Rectangle(playerX + offsetX, nextY + offsetY, playerWidth, playerHeight);
        boolean collidedY = false;
        for (Rectangle r : collisionRects) { if (rectY.overlaps(r)) { collidedY = true; break; } }
        if (!collidedY) playerY = nextY;

        // ขอบแผนที่
        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;

        camera.position.set(playerX, playerY, 0);
        camera.update();

        // อัปเดตสถานะ “กำลังเดิน” และควบคุมเสียงเดิน
        isMoving = moving && !isJumping;
        updateWalkingSound(isMoving);
    }

    private void updateWalkingSound(boolean shouldPlay) {
        if (walkMusic == null) return;
        if (shouldPlay) {
            if (!walkingSoundPlaying) { walkMusic.play(); walkingSoundPlaying = true; }
        } else {
            if (walkingSoundPlaying) { walkMusic.stop(); walkingSoundPlaying = false; }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override public void pause()  { if (bgmFirst != null && bgmFirst.isPlaying()) bgmFirst.pause(); if (walkMusic != null && walkMusic.isPlaying()) walkMusic.pause(); }
    @Override public void resume() { if (bgmFirst != null && !bgmFirst.isPlaying()) bgmFirst.play(); }

    @Override public void hide()   { if (bgmFirst != null) bgmFirst.stop(); if (walkMusic != null) walkMusic.stop(); }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (batch != null) batch.dispose();
        if (frontTex != null) frontTex.dispose();
        if (chatIcon != null) chatIcon.dispose();
        if (handIcon != null) handIcon.dispose();
        if (gearIcon != null) gearIcon.dispose();

        disposeArray(frontWalk); disposeArray(backWalk); disposeArray(leftWalk);
        disposeArray(rightWalk); disposeArray(Jump);

        if (bgmFirst != null) bgmFirst.dispose();
        if (walkMusic != null) walkMusic.dispose();
    }

    private void disposeArray(Texture[] arr) {
        if (arr == null) return;
        for (Texture t : arr) if (t != null) t.dispose();
    }
}
