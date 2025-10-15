package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.graphics.OrthographicCamera;

// ⭐ แก้ไขหลัก:
// 1. เพิ่ม GlyphLayout สำหรับวาดชื่อผู้เล่น
// 2. เพิ่มตัวแปร Music และ Sound
// 3. ย้าย gearIcon ไป show()
// 4. รวมการตรวจสอบ justTouched() เป็นครั้งเดียว
// 5. เพิ่มเสียงเดินที่ loop/stop ตามการเคลื่อนไหว

public class FirstScreen implements Screen {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture frontTex;
    private Texture currentTex;
    private float playerX, playerY;
    private final float speed = 160f;

    private final float MIN_X = 800;
    private final float MAX_X = 3300;
    private final float MIN_Y = 560;
    private final float MAX_Y = 2740;

    private Array<Rectangle> collisionRects;
    private Array<NPC> npcs;

    private final float unitScale = 1f / 2.5f;

    private int currentFrame = 0;
    private float walkTime = 0f;
    private final float frameDuration = 0.2f;
    private Texture[] frontWalk, backWalk, leftWalk, rightWalk, Jump;

    private boolean isJumping = false;

    private BitmapFont font;
    private GlyphLayout nameLayout;

    private Texture chatIcon;
    private Texture handIcon;
    private Texture gearIcon;
    private boolean objectVisible = false;

    // เสียง
    private Music backgroundMusic;
    private Sound walkSound;
    private boolean wasMoving = false;

    private final Main game;
    private OrthographicCamera uiCamera;     // กล้องสำหรับ HUD/จอ
    private InventoryUI inventory;           // มุมซ้ายล่าง

    // โซนที่ยืนแล้วกด E เพื่อเก็บของ (พิกัดโลก ปรับได้ตามแผนที่จริง)
    private final Rectangle gearZone  = new Rectangle(2250, 2380, 400, 400);
    private final Rectangle bookZone  = new Rectangle(2330, 2380, 400, 400);

    // === Elephant shrine trigger ===
    private Rectangle elephantShrineZone;   // จะโหลดจากชั้น "Triggers" ชื่อวัตถุ "ElephantShrine"
    private float shrineIdleTimer = 0f;     // เวลาที่ผู้เล่นหยุดนิ่งในโซน
    private final float shrineIdleToEnter = 0.8f; // หยุด 0.8 วินาทีแล้วเข้า ShootScreen

    // ใช้ bounds เดียวกับที่ชนฉาก
    private final Rectangle playerBounds = new Rectangle();
    private float lastX = Float.NaN, lastY = Float.NaN;
    private final float idleEpsilon = 0.5f; // ผู้เล่นขยับน้อยกว่า 0.5 หน่วยถือว่า “นิ่ง”



    public FirstScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("Map/Map.tmx");
        font = new BitmapFont();
        nameLayout = new GlyphLayout();

        chatIcon = new Texture("Icons/Chat_Icon.png");
        handIcon = new Texture("Icons/Hand_Icon.png");
        gearIcon = new Texture("Icons/gear.PNG");

        collisionRects = new Array<>();
        npcs = new Array<>();

        // โหลดเสียง
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/StageMusic/FirstScreen.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.3f);
            backgroundMusic.play();
        } catch (Exception e) {
            System.out.println("ไม่สามารถโหลดเพลงพื้นหลังได้");
        }

        try {
            walkSound = Gdx.audio.newSound(Gdx.files.internal("Music/Walk/Walk.mp3"));
        } catch (Exception e) {
            System.out.println("ไม่สามารถโหลดเสียงเดินได้");
        }

        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tilePixel = map.getProperties().get("tilewidth", Integer.class);

        renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() * unitScale,
            Gdx.graphics.getHeight() * unitScale
        );
        camera.update();

        // สร้าง NPC
        NPC penguin = new NPC(2358, 2340, "NPC/Penguin_Stand.png", "Today is very cold~", "Penguin");
        npcs.add(penguin);
        NPC giraffe = new NPC(1400, 1720, "NPC/Giraffe_Stand.png", "We are all Entaneer!", "Giraffe");
        npcs.add(giraffe);
        // เพิ่มช้างใหม่
        NPC elephant = new NPC(3000, 1700, "NPC/elephant_Stand.PNG", "Welcome to Elephant Shrine!", "Elephant");
        elephant.width = 100;  // ความกว้าง (ค่าเดิมประมาณ 100)
        elephant.height = 130; // ความสูง (ค่าเดิมประมาณ 150)
        npcs.add(elephant);

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

        // โหลดภาพเคลื่อนไหว
        frontWalk = new Texture[] {
            new Texture("CharMove/Girl_FrontLeft1.png"),
            new Texture("CharMove/Girl_FrontStand.png"),
            new Texture("CharMove/Girl_FrontRight1.png"),
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

        frontTex = new Texture("CharMove/Girl_Front.png");
        currentTex = frontTex;
        batch = new SpriteBatch();

        float spawnX = (mapWidth * tilePixel * 0.495f) / 2.5f;
        float spawnY = (mapHeight * tilePixel * 0.48f) / 2.5f;
        playerX = spawnX;
        playerY = spawnY;

        camera.position.set(playerX, playerY, 0);
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        // === NEW: อินเวนทอรีมุมซ้ายล่างของ “จอ” ===
        // margin 16px, ช่อง 48px, ระยะห่าง 8px
        inventory = new InventoryUI(16f, 16f, 48f, 8f);

        elephantShrineZone = null;
        MapLayer triggers = map.getLayers().get("Triggers");
        if (triggers != null) {
            for (MapObject obj : triggers.getObjects()) {
                if (obj instanceof RectangleMapObject && "ElephantShrine".equals(obj.getName())) {
                    Rectangle r = ((RectangleMapObject) obj).getRectangle();
                    elephantShrineZone = new Rectangle(
                            r.x * unitScale, r.y * unitScale,
                            r.width * unitScale, r.height * unitScale
                    );
                    break;
                }
            }
        }
// ประมาณศาลจ๊ะอยู่ที่ (1680, 900) ขนาด (220, 160)
        if (elephantShrineZone == null) {
            elephantShrineZone = new Rectangle(1680, 900, 220, 160);
        }
}

    @Override
    public void render(float delta) {
        handleInput(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        renderer.setView(camera);

        renderer.render(new int[]{2,3,4,5});

        batch.setProjectionMatrix(camera.combined);
        batch.begin();


        // วาด NPC
        for (NPC npc : npcs) {
            batch.draw(npc.texture, npc.x, npc.y, npc.width, npc.height);

            float distance = (float) Math.sqrt(Math.pow(playerX - npc.x, 2) + Math.pow(playerY - npc.y, 2));

            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                // ข้อความ NPC เป็นสีขาว
                font.setColor(Color.WHITE);
                font.draw(batch, npc.dialogue, npc.x, npc.y + npc.height + 20);
                batch.draw(chatIcon, npc.x + 55, npc.y + 60, 24, 24);
            }
        }

        // Quest 1
        Rectangle triggerArea = new Rectangle(2200, 2400, 100, 100);
        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);

        if (game.questManager.isQuest1Started()) {
            if (playerRect.overlaps(triggerArea)) {
                font.draw(batch, "Tap to get inside!", 2240, 2440);
            }
        }

        // Quest 2
        Rectangle handRect = new Rectangle(1600, 800, 50, 50);
        if (game.questManager.isQuest2Started()) {
            batch.draw(handIcon, handRect.x, handRect.y, handRect.width, handRect.height);
            font.draw(batch, "loob!", handRect.x, handRect.y + handRect.height + 20);
        }

        float gearIconX = 1650;
        float gearIconY = 860;
        if (objectVisible) {
            batch.draw(gearIcon, gearIconX, gearIconY, 40, 40);
        }

        // วาดตัวละคร
        float tileSize = 10f;
        batch.draw(currentTex, playerX, playerY,
            currentTex.getWidth() / tileSize,
            currentTex.getHeight() / tileSize
        );

        // วาดชื่อผู้เล่นใต้ตัวละคร
        if (!game.playerName.isEmpty()) {
            nameLayout.setText(font, game.playerName);
            float nameX = playerX + (currentTex.getWidth() / tileSize - nameLayout.width) / 2;
            float nameY = playerY - 10;

            // พื้นหลังสีดำโปร่งใส
            batch.setColor(0, 0, 0, 0.6f);
            batch.draw(game.pixel, nameX - 5, nameY - nameLayout.height - 2, nameLayout.width + 10, nameLayout.height + 6);
            batch.setColor(Color.WHITE);

            // วาดข้อความสีขาวพร้อมเงาสีดำ
            font.setColor(Color.BLACK);
            font.draw(batch, game.playerName, nameX + 1, nameY - 1);
            font.setColor(Color.WHITE);
            font.draw(batch, game.playerName, nameX, nameY);
        }

        batch.end();

        renderer.render(new int[]{6});

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        inventory.render(batch);   // เงาเทา = ยังไม่เก็บ, สีจริง = เก็บแล้ว
        batch.end();

        // ตรวจสอบการคลิก (รวมเป็นครั้งเดียว)
        if (Gdx.input.justTouched()) {
            Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click);

            // คลิก handRect
            if (game.questManager.isQuest2Started() && handRect.contains(click.x, click.y)) {
                objectVisible = true;
            }

            // คลิก chat icon
            for (NPC npc : npcs) {
                if (npc.isPlayerNear(playerX, playerY, 80f)) {
                    Rectangle chatRect = new Rectangle(npc.x + 55, npc.y + 60, 24, 24);
                    if (chatRect.contains(click.x, click.y)) {
                        game.setScreen(new DialogueScreen(game, npc));
                        return;
                    }
                }
            }

            // คลิก trigger area
            if (game.questManager.isQuest1Started() && playerRect.overlaps(triggerArea)) {
                game.setScreen(new LibraryScreen(game));
            }
        }
    }

    private Texture[] previousWalkFrames = null;

    private void handleInput(float delta) {
        float nextX = playerX;
        float nextY = playerY;
        boolean moving = false;
        Texture[] walkFrames = null;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            nextY += speed * delta;
            moving = true;
            walkFrames = backWalk;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            nextY -= speed * delta;
            moving = true;
            walkFrames = frontWalk;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            nextX -= speed * delta;
            moving = true;
            walkFrames = leftWalk;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            nextX += speed * delta;
            moving = true;
            walkFrames = rightWalk;
        }

        // เสียงเดิน
        if (moving && !wasMoving && walkSound != null) {
            walkSound.loop(1f);
        } else if (!moving && wasMoving && walkSound != null) {
            walkSound.stop();
        }
        wasMoving = moving;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            isJumping = true;
            currentTex = Jump[0];
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    isJumping = false;
                    if (!Gdx.input.isKeyPressed(Input.Keys.UP) &&
                        !Gdx.input.isKeyPressed(Input.Keys.DOWN) &&
                        !Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
                        !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                        currentTex = frontTex;
                    }
                }
            }, 0.3f);
        }

        for (NPC npc : npcs) {
            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    game.setScreen(new DialogueScreen(game, npc));
                }
            }
        }

        if (walkFrames != previousWalkFrames) {
            currentFrame = 0;
            walkTime = 0f;
            previousWalkFrames = walkFrames;
        }

        if (moving && walkFrames != null && !isJumping) {
            walkTime += delta;
            if (walkTime >= frameDuration) {
                walkTime = 0f;
                currentFrame++;
                if (currentFrame >= walkFrames.length) currentFrame = 0;
            }
            currentTex = walkFrames[currentFrame];
        } else {
            currentFrame = 0;
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150); // ใช้กล่องเดิมที่คุณคำนวณอยู่ก็ได้
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (playerRect.overlaps(gearZone)) inventory.collectItem("gear");
            if (playerRect.overlaps(bookZone)) inventory.collectItem("book");
        }

        float playerWidth = currentTex.getWidth() / 10f;
        float playerHeight = currentTex.getHeight() / 10f * 0.4f;
        float offsetX = (currentTex.getWidth() / 10f - playerWidth) / 2f;
        float offsetY = 0;

        Rectangle rectX = new Rectangle(nextX + offsetX, playerY + offsetY, playerWidth, playerHeight);
        boolean collidedX = false;
        for (Rectangle r : collisionRects) {
            if (rectX.overlaps(r)) {
                collidedX = true;
                break;
            }
        }
        if (!collidedX) playerX = nextX;

        Rectangle rectY = new Rectangle(playerX + offsetX, nextY + offsetY, playerWidth, playerHeight);
        boolean collidedY = false;
        for (Rectangle r : collisionRects) {
            if (rectY.overlaps(r)) {
                collidedY = true;
                break;
            }
        }
        if (!collidedY) playerY = nextY;

        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;

        camera.position.set(playerX, playerY, 0);
        camera.update();

        // ตรวจว่าผู้เล่นอยู่ในโซนศาลช้างและหยุดนิ่ง
        Rectangle playerRectForTrigger = new Rectangle(playerX, playerY, 100, 150);
        boolean inShrine = playerRectForTrigger.overlaps(elephantShrineZone);

        if (inShrine && !moving) {
            shrineIdleTimer += delta;
            if (shrineIdleTimer >= shrineIdleToEnter) {
                goToShootScreen();   // → เปลี่ยนฉาก
                return;              // หยุดทำงานต่อในเฟรมนี้
            }
        } else {
            shrineIdleTimer = 0f;
        }

    }

    private void goToShootScreen() {
        if (backgroundMusic != null) backgroundMusic.stop();
        if (walkSound != null) walkSound.stop();
        game.setScreen(new ShootScreen()); // ใช้คอนสตรัคเตอร์แบบที่คุณให้มา
    }

    @Override
    public void resize(int width, int height) {
        float unitScale = 1f;
        camera.setToOrtho(false, width * unitScale, height * unitScale);
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void pause() {
        if (backgroundMusic != null) backgroundMusic.pause();
    }

    @Override
    public void resume() {
        if (backgroundMusic != null) backgroundMusic.play();
    }

    @Override
    public void hide() {
        if (backgroundMusic != null) backgroundMusic.stop();
        if (walkSound != null) walkSound.stop();
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        frontTex.dispose();
        chatIcon.dispose();
        handIcon.dispose();
        gearIcon.dispose();

        if (backgroundMusic != null) backgroundMusic.dispose();
        if (walkSound != null) walkSound.dispose();

        for (Texture[] frames : new Texture[][]{frontWalk, backWalk, leftWalk, rightWalk, Jump}) {
            for (Texture t : frames) t.dispose();
        }

        for (NPC npc : npcs) {
            npc.dispose();
        }
    }
}
