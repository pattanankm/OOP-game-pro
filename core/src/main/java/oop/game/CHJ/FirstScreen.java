package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout; // ✅ เพิ่ม
import com.badlogic.gdx.audio.Music;              // ✅ เพิ่ม


public class FirstScreen implements Screen {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Texture frontTex;
    private Texture currentTex;
    private Texture gearIcon;      // <-- ย้ายมาประกาศไว้
    private Texture chatIcon;
    private Texture handIcon;

    private float playerX, playerY;
    private float speed = 160f;

    private final float MIN_X = 800;
    private final float MAX_X = 3300;
    private final float MIN_Y = 560;
    private final float MAX_Y = 2740;

    private Array<Rectangle> collisionRects;
    private Array<NPC> npcs;

    private final float unitScale = 1f / 2.5f;

    private int currentFrame = 0;
    private float walkTime = 0f;
    private float frameDuration = 0.2f;
    private Texture[] frontWalk, backWalk, leftWalk, rightWalk, Jump;

    private boolean isJumping = false;

    private BitmapFont font;
    private boolean objectVisible = false;
    // ✅ เพลงคลอและเสียงเดิน
    private Music bgmFirst;            // เพลงคลอ FirstScreen.mp3
    private Music walkMusic;           // เสียงเดิน Walk.mp3
    private boolean walkingSoundPlaying = false;

    // ✅ สถานะกำลังเดิน (ไว้ใช้วาดชื่อเฉพาะตอนวิ่ง)
    private boolean isMoving = false;

    // ✅ ใช้จัดกึ่งกลางชื่อ
    private GlyphLayout nameLayout = new GlyphLayout();

    private final Main game;

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
        gearIcon = new Texture("Icons/gear.PNG"); // <-- โหลดครั้งเดียวที่นี่

        collisionRects = new Array<>();
        npcs = new Array<>();

        // สร้าง NPC
        NPC penguin = new NPC(2358, 2340, "NPC/Penguin_Stand.png", "Today is very cold~", "Penguin");
        npcs.add(penguin);
        NPC giraffe = new NPC(1400, 1700, "NPC/Giraffe_Stand.png", "We are all Entaneer!", "Giraffe");
        npcs.add(giraffe);

        // อ่าน collision layer
        MapLayer objectLayer = map.getLayers().get("Collision");
        if (objectLayer != null) {
            for (MapObject obj : objectLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    rect.set(rect.x * unitScale, rect.y * unitScale, rect.width * unitScale, rect.height * unitScale);
                    collisionRects.add(rect);
                }
            }
        }

        // โหลดภาพท่าเดิน
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
        Jump = new Texture[]{ new Texture("CharMove/Girl_Jump.png") };

        frontTex  = new Texture("CharMove/Girl_Front.png");
        currentTex = frontTex;

        if (bgmFirst == null) {
            bgmFirst = Gdx.audio.newMusic(Gdx.files.internal("Music/FirstScreen.mp3"));
            bgmFirst.setLooping(true);
            bgmFirst.setVolume(0.28f); // คลอเบาๆ
        }
        bgmFirst.play();

        // ✅ เสียงเดิน (ให้ loop ได้)
        if (walkMusic == null) {
            walkMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Walk/Walk.mp3"));
            walkMusic.setLooping(true);
            walkMusic.setVolume(0.55f); // เด่นกว่าบีจีบางนิด
        }

        // จุดเกิด
        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tilePixel = map.getProperties().get("tilewidth", Integer.class);
        playerX = (mapWidth * tilePixel * 0.495f) / 2.5f;
        playerY = (mapHeight * tilePixel * 0.48f ) / 2.5f;

        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        renderer.setView(camera);

        // วาดพื้น
        renderer.render(new int[]{2,3,4,5});

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาด NPC + ไอคอนคุย
        for (NPC npc : npcs) {
            batch.draw(npc.texture, npc.x, npc.y, npc.width, npc.height);
            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                font.draw(batch, npc.dialogue, npc.x, npc.y + npc.height + 20);
                batch.draw(chatIcon, npc.x + 55 , npc.y + 60, 24, 24);
            }
        }

        // เควส 1: เข้า Library
        Rectangle triggerArea = new Rectangle(2200, 2400, 100, 100);
        Rectangle playerRect  = new Rectangle(playerX, playerY, 100, 150);
        if (game.questManager.isQuest1Started()) {
            if (playerRect.overlaps(triggerArea)) {
                font.draw(batch, "Tap to get inside!", 2240, 2440);
                if (Gdx.input.justTouched()) {
                    game.setScreen(new LibraryScreen(game));
                }
            }
        }

        // เควส 2: มือ/เฟือง
        Rectangle handRect = new Rectangle(1600, 800, 50, 50);
        if (game.questManager.isQuest2Started()) {
            batch.draw(handIcon, handRect.x, handRect.y, handRect.width, handRect.height);
            font.draw(batch, "loob!", handRect.x, handRect.y + handRect.height + 20);
        }

        if (Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click);
            if (handRect.contains(click.x, click.y)) {
                objectVisible = true;
            }
        }

        if (objectVisible) {
            batch.draw(gearIcon, 1650, 860, 40, 40); // <-- ใช้ gearIcon ที่โหลดใน show()
        }

        // วาดตัวละคร
        float tileSize = 10f;
        batch.draw(currentTex, playerX, playerY,
            currentTex.getWidth() / tileSize,
            currentTex.getHeight() / tileSize
        );

        batch.end();

        // วาดเลเยอร์บนสุด
        renderer.render(new int[]{6});

        // คลิก chat icon
        if (Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click);
            for (NPC npc : npcs) {
                if (npc.isPlayerNear(playerX, playerY, 80f)) {
                    Rectangle chatRect = new Rectangle(npc.x + 55, npc.y + 60, 24, 24);
                    if (chatRect.contains(click.x, click.y)) {
                        game.setScreen(new DialogueScreen(game, npc));
                        break;
                    }
                }
            }
        }
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
            com.badlogic.gdx.utils.Timer.schedule(new Timer.Task() {
                @Override public void run() {
                    isJumping = false;
                    if (!(Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ||
                        Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))) {
                        currentTex = frontTex;
                    }
                }
            }, 0.3f);
        }

        for (NPC npc : npcs) {
            if (npc.isPlayerNear(playerX, playerY, 80f) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                game.setScreen(new DialogueScreen(game, npc));
            }
        }

        if (walkFrames != previousWalkFrames) { currentFrame = 0; walkTime = 0f; previousWalkFrames = walkFrames; }

        if (moving && walkFrames != null && !isJumping) {
            walkTime += delta;
            if (walkTime >= frameDuration) { walkTime = 0f; currentFrame++; if (currentFrame >= walkFrames.length) currentFrame = 0; }
            currentTex = walkFrames[currentFrame];
        } else {
            currentFrame = 0;
        }

        float playerWidth  = currentTex.getWidth() / 10f * 1f;
        float playerHeight = currentTex.getHeight() / 10f * 0.4f;
        float offsetX = (currentTex.getWidth() / 10f - playerWidth) / 2f;
        float offsetY = 0;

        Rectangle rectX = new Rectangle(nextX + offsetX, playerY + offsetY, playerWidth, playerHeight);
        boolean collidedX = false;
        for (Rectangle r : collisionRects) { if (rectX.overlaps(r)) { collidedX = true; break; } }
        if (!collidedX) playerX = nextX;

        Rectangle rectY = new Rectangle(playerX + offsetX, nextY + offsetY, playerWidth, playerHeight);
        boolean collidedY = false;
        for (Rectangle r : collisionRects) { if (rectY.overlaps(r)) { collidedY = true; break; } }
        if (!collidedY) playerY = nextY;

        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;

        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (batch != null) batch.dispose();
        if (frontTex != null) frontTex.dispose();
        if (chatIcon != null) chatIcon.dispose();
        if (handIcon != null) handIcon.dispose();
        if (gearIcon != null) gearIcon.dispose();

        if (npcs != null) for (NPC npc : npcs) npc.dispose();
        disposeArray(frontWalk); disposeArray(backWalk); disposeArray(leftWalk);
        disposeArray(rightWalk); disposeArray(Jump);
    }

    private void disposeArray(Texture[] arr) {
        if (arr == null) return;
        for (Texture t : arr) if (t != null) t.dispose();
    }
}
