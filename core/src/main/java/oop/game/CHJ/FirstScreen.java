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

public class FirstScreen implements Screen {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
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

    private Music backgroundMusic;
    private Sound walkSound;
    private boolean wasMoving = false;

    private final Main game;
    private InventoryUI inventory;
    private TopLeftHUD topLeftHUD;
    private HintTextUI hintTextUI;
    private PauseMenu pauseMenu;

    private final Rectangle gearZone = new Rectangle(2250, 2380, 400, 400);
    private final Rectangle bookZone = new Rectangle(2330, 2380, 400, 400);

    private Rectangle elephantShrineZone;
    private float shrineIdleTimer = 0f;
    private final float shrineIdleToEnter = 0.8f;

    private Texture[] previousWalkFrames = null;

    // ตัวแปรสำหรับติดตามว่าเคยแสดงข้อความไปแล้วหรือยัง
    private boolean hasShownStartHint = false;
    private boolean hasShownGiraffeAreaHint = false;
    private boolean hasShownGearHint = false;
    private boolean hasShownLibraryHint = false;
    private boolean hasShownShrineHint = false;

    //เพิ่มตัวแปรเช็คว่า initialized แล้วหรือยัง (แก้บัคเกิดที่เดิม)
    private boolean isInitialized = false;

    private float isPressedTime = 0f; //แก้บัคตัวละครติด
    private boolean disableCollision = false;
//    private boolean isQuest2Completed;

    public FirstScreen(Main game) {
        this(game, null);
    }

    public FirstScreen(Main game, InventoryUI inventory) {
        this.game = game;
        this.inventory = inventory;
    }

    @Override
    public void show() {
        //สร้าง camera ก่อนเสมอ
        if (camera == null) {
            camera = new OrthographicCamera();
            camera.setToOrtho(false,
                Gdx.graphics.getWidth() * unitScale,
                Gdx.graphics.getHeight() * unitScale
            );
        }

        if (uiCamera == null) {
            uiCamera = new OrthographicCamera();
            uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        //เช็ค isInitialized
        if (isInitialized) {
            camera.position.set(playerX, playerY, 0);
            camera.update();
            uiCamera.update();
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            }

            return;
        }

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

        // สร้าง NPC
        NPC penguin = new NPC(2358, 2340, "NPC/Penguin_Stand.png", "Today is very cold~", "Penguin");
        npcs.add(penguin);
        NPC giraffe = new NPC(1400, 1700, "NPC/Giraffe_Stand.png", "We are all Entaneer!", "Giraffe");
        npcs.add(giraffe);
        NPC elephant = new NPC(3000, 1700, "NPC/elephant_Stand.PNG", "Welcome to Elephant Shrine!", "Elephant");
        elephant.width = 100;
        elephant.height = 130;
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

        // โหลดท่าเดิน
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

        // สร้าง UI components
        topLeftHUD = new TopLeftHUD("Botton/SaveBT.png", "Botton/HomeBT.png");
        if (inventory == null) {
            inventory = new InventoryUI(16f, 16f, 48f, 8f);
        }
        hintTextUI = new HintTextUI();
        pauseMenu = new PauseMenu();

        // โหลด elephant shrine zone
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

        if (elephantShrineZone == null) {
            elephantShrineZone = new Rectangle(1680, 900, 220, 160);
        }

        showContextualHint();
        float spawnX = (mapWidth * tilePixel+30) / 2.5f;
        float spawnY = (mapHeight * tilePixel * 0.5f) / 2.5f;
        playerX = spawnX;
        playerY = spawnY;

        camera.position.set(playerX, playerY, 0);
        camera.update();
        uiCamera.update();

        // บันทึกว่า initialized แล้ว
        isInitialized = true;
    }

    private void saveGame() {
        game.questManager.updatePlayerPosition("FirstScreen", playerX, playerY);
        SaveManager.SaveState s = SaveManager.load();
        s.x = playerX;
        s.y = playerY;
        s.screen = "FirstScreen";
        SaveManager.save(s);
        Gdx.app.log("SAVE", "Game saved at (" + playerX + ", " + playerY + ")");
    }

    private void showContextualHint() {
        int step = game.questManager.getQuestStep();

        if (step == 0 && !hasShownStartHint) {
            hintTextUI.showHint("Where is this place?, i need to look around");
            hasShownStartHint = true;
        } else if (step == 0 && isNearGiraffe() && !hasShownGiraffeAreaHint) {
            hintTextUI.showHint("Who is that giraffe?, maybe i should go talk to her");
            hasShownGiraffeAreaHint = true;
        } else if (step == 2 && !hasShownGearHint) {
            hintTextUI.showHint("i need to find a gear around here");
            hasShownGearHint = true;
        } else if (step == 3 && !hasShownLibraryHint) {
            hintTextUI.showHint("I should go to the library to close the air-con and find a book");
            hasShownLibraryHint = true;
        } else if (step == 5 && !hasShownShrineHint) {
            hintTextUI.showHint("Maybe i should make a wish at the elephant shrine");
            hasShownShrineHint = true;
        }
    }

    private boolean isNearGiraffe() {
        for (NPC npc : npcs) {
            if (npc.name.equals("Giraffe") && npc.isPlayerNear(playerX, playerY, 200f)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(float delta) {
        // ถ้า pause menu เปิดอยู่ ไม่ให้เกมทำงาน
        if (pauseMenu.isVisible()) {
            renderPauseMenu();
            return;
        }

        handleInput(delta);
        hintTextUI.update(delta);
        showContextualHint();

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

            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                font.setColor(Color.WHITE);
                font.draw(batch, npc.dialogue, npc.x, npc.y + npc.height + 20);
                batch.draw(chatIcon, npc.x + 55, npc.y + 60, 24, 24);
            }
        }

        // Quest 1
        Rectangle triggerArea = new Rectangle(2200, 2400, 100, 100);
        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);

       // ตรวจสอบว่า Quest 1 เริ่มแล้วก่อน
        if (game.questManager.isQuest1Started() && playerRect.overlaps(triggerArea)) {
            font.draw(batch, "Tap to get inside!", triggerArea.x + 40, triggerArea.y + 40);

            if (Gdx.input.justTouched()) {
                game.setScreen(new LibraryScreen(game,inventory));
            }
        }
        // Quest 2
        Rectangle handRect = new Rectangle(1600, 800, 50, 50);
        if (game.questManager.isQuest2Started()) {
            batch.draw(handIcon, handRect.x, handRect.y, handRect.width, handRect.height);
            font.draw(batch, "Touch!", handRect.x, handRect.y + handRect.height + 20);
        }

        // ตรวจคลิก
        if (Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click); // แปลงจาก screen -> world

            if (handRect.contains(click.x, click.y)) {
                objectVisible = true; // ให้ object ใหม่โผล่
            }
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

        //Elephant Shrine Quest (ต้องเก็บ Gear + Book เสร็จก่อน)
        if (game.questManager.isGearCollected() && game.questManager.isBookCollected()) {
            // วาดข้อความที่ตำแหน่ง Elephant
            NPC elephant = null;
            for (NPC npc : npcs) {
                if ("Elephant".equals(npc.name)) {
                    elephant = npc;
                    break;
                }
            }

            if (elephant != null) {
                font.setColor(Color.YELLOW);
                font.getData().setScale(2f);
                font.draw(batch, "Make a wish!", elephant.x + 10, elephant.y + elephant.height + 40);
                font.getData().setScale(1f);
                font.setColor(Color.WHITE);

                // ตรวจคลิก
                if (Gdx.input.justTouched()) {
                    Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                    camera.unproject(click);

                    Rectangle elephantClickZone = new Rectangle(elephant.x, elephant.y, elephant.width, elephant.height + 50);
                    if (elephantClickZone.contains(click.x, click.y)) {
                        // Complete quest 1 & 2
                        game.questManager.completeQuest1();
                        game.questManager.completeQuest2();

                        // ไปหน้า Make a Wish
                        game.setScreen(new MakeAWish(game));
                        return;
                    }
                }
            }
        }

        // วาดชื่อผู้เล่นใต้ตัวละคร
        if (!game.playerName.isEmpty()) {
            nameLayout.setText(font, game.playerName);
            float nameX = playerX + (currentTex.getWidth() / tileSize - nameLayout.width) / 2;
            float nameY = playerY - 10;

            batch.setColor(0, 0, 0, 0.6f);
            batch.draw(game.pixel, nameX - 5, nameY - nameLayout.height - 2, nameLayout.width + 10, nameLayout.height + 6);
            batch.setColor(Color.WHITE);

            font.setColor(Color.BLACK);
            font.draw(batch, game.playerName, nameX + 1, nameY - 1);
            font.setColor(Color.WHITE);
            font.draw(batch, game.playerName, nameX, nameY);
        }

        batch.end();

        renderer.render(new int[]{6});

        // วาด UI
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        topLeftHUD.render(batch);
        inventory.render(batch);
        hintTextUI.render(batch, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.end();

        // เช็คปุ่ม HUD
        TopLeftHUD.Clicked action = topLeftHUD.updateAndHandleInput(uiCamera);
        if (action == TopLeftHUD.Clicked.SAVE) {
            saveGame();
            hintTextUI.showHint("Game is already recoded!");
        } else if (action == TopLeftHUD.Clicked.HOME) {
            pauseMenu.show();
        }

        // ตรวจสอบการคลิก
        if (Gdx.input.justTouched()) {
            Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click);

            if (game.questManager.isQuest2Started() && handRect.contains(click.x, click.y)) {
                objectVisible = true;
                game.questManager.onTouchHand();
            }

            for (NPC npc : npcs) {
                if (npc.isPlayerNear(playerX, playerY, 80f)) {
                    Rectangle chatRect = new Rectangle(npc.x + 55, npc.y + 60, 24, 24);
                    if (chatRect.contains(click.x, click.y)) {
                        game.setScreen(new DialogueScreen(game, npc));
                        return;
                    }
                }
            }

            if (game.questManager.isQuest1Started() && playerRect.overlaps(triggerArea)) {
                game.setScreen(new LibraryScreen(game,inventory));
            }
        }
    }

    private void renderPauseMenu() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาดตัวละครและ NPC (แช่แข็ง)
        for (NPC npc : npcs) {
            batch.draw(npc.texture, npc.x, npc.y, npc.width, npc.height);
        }
        float tileSize = 10f;
        batch.draw(currentTex, playerX, playerY,
            currentTex.getWidth() / tileSize,
            currentTex.getHeight() / tileSize);
        batch.end();

        // วาด Pause Menu
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        pauseMenu.render(batch, uiCamera);
        batch.end();

        // ตรวจสอบการคลิกปุ่ม
        PauseMenu.Action menuAction = pauseMenu.checkInput(uiCamera);
        if (menuAction == PauseMenu.Action.BEGIN) {
            pauseMenu.hide();
            if (backgroundMusic != null) backgroundMusic.stop();
            if (walkSound != null) walkSound.stop();
            game.setScreen(new MainMenuScreen(game));
        } else if (menuAction == PauseMenu.Action.RESUME) {
            pauseMenu.hide();
        } else if (menuAction == PauseMenu.Action.EXIT) {
            Gdx.app.exit();
        }
    }

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
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
//            nextY -= speed * delta;
//            moving = true;
//            walkFrames = frontWalk;
//        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            isPressedTime += delta; //เพิ่มเวลาที่กดค้าง
            nextY -= speed * delta; //เดินลงปกติ
            moving = true;
            walkFrames = frontWalk;

            if (isPressedTime >= 2f) {
                disableCollision = true; //ปิด collision หลัง 2 วินาที
            }
        } else {
            isPressedTime = 0f;       //รีเซ็ตเวลา
            disableCollision = false; //เปิด collision ปกติ
            moving = false;
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);

            if (playerRect.overlaps(gearZone)) {
                if (inventory.collectItem("gear")) {
                    game.questManager.onCollectItem("gear");
                    hintTextUI.showHint("Collected the gear!");
                }
            }
        }

        //ขนาดภาพที่วาดจริง
        float characterDrawWidth = currentTex.getWidth() / 10f;
        float characterDrawHeight = currentTex.getHeight() / 10f;

        //hitbox
        float playerWidth = characterDrawWidth * 0.8f;
        float playerHeight = characterDrawHeight * 0.5f;

        //offset อยู่ตรงกลางแนวนอน และอยู่ที่เท้า
        float offsetX = (characterDrawWidth - playerWidth) / 2f;  // วางตรงกลาง
        float offsetY = 0; // เริ่มจากเท้า (พื้นฐาน)

        // แยกแกน X
        Rectangle rectX = new Rectangle(
            nextX + offsetX,
            playerY + offsetY,
            playerWidth,
            playerHeight
        );
        boolean collidedX = false;
        if (!disableCollision) {
            for (Rectangle r : collisionRects) {
                if (rectX.overlaps(r)) {
                    collidedX = true;
                    break;
                }
            }
        }
        if (!collidedX) playerX = nextX;

        // แยกแกน Y
        Rectangle rectY = new Rectangle(
            playerX + offsetX,
            nextY + offsetY,
            playerWidth,
            playerHeight
        );
        boolean collidedY = false;
        if (!disableCollision) {
            for (Rectangle r : collisionRects) {
                if (rectY.overlaps(r)) {
                    collidedY = true;
                    break;
                }
            }
        }
        if (!collidedY) playerY = nextY;

        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;

        camera.position.set(playerX, playerY, 0);
        camera.update();

        // ตรวจศาลช้าง
        Rectangle playerRectForTrigger = new Rectangle(playerX, playerY, 100, 150);
        boolean inShrine = playerRectForTrigger.overlaps(elephantShrineZone);

        if (inShrine && !moving) {
            shrineIdleTimer += delta;
            if (shrineIdleTimer >= shrineIdleToEnter) {
                goToShootScreen();
                return;
            }
        } else {
            shrineIdleTimer = 0f;
        }
    }

    private void goToShootScreen() {
        if (backgroundMusic != null) backgroundMusic.stop();
        if (walkSound != null) walkSound.stop();
        game.setScreen(new MakeAWish(game));
    }

    @Override
    public void resize(int width, int height) {

        camera.setToOrtho(false, width * unitScale * 2.5f,   // ← เพิ่ม * 2.5f
            height * unitScale * 2.5f);
        camera.position.set(playerX, playerY, 0);
        camera.update();

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        if (topLeftHUD != null) topLeftHUD.onResize(width, height);
        if (pauseMenu != null) pauseMenu.onResize(width, height);
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
        if (topLeftHUD != null) topLeftHUD.dispose();
        if (hintTextUI != null) hintTextUI.dispose();
        if (pauseMenu != null) pauseMenu.dispose();
        if (inventory != null) inventory.dispose();

        for (Texture[] frames : new Texture[][]{frontWalk, backWalk, leftWalk, rightWalk, Jump}) {
            for (Texture t : frames) t.dispose();
        }

        for (NPC npc : npcs) {
            npc.dispose();
        }
    }
}


