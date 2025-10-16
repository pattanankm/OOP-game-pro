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
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class LibraryScreen implements Screen {
    private final Main game;
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private final float unitScale = 1f / 1f;

    // ตัวละคร
    private Texture playerTex;
    private Texture currentTex;
    private float playerX, playerY;
    private float speed = 160f;

    //ใช้กับท่าเดิน
    private int currentFrame = 0;      // เฟรมปัจจุบัน
    private float walkTime = 0f;       // เวลาเดินสะสม
    private float frameDuration = 0.2f; // เวลาต่อเฟรม (วินาที)
    private Texture[] frontWalk, backWalk, leftWalk, rightWalk, Jump;
    private Texture[] previousWalkFrames = null;

    private BitmapFont font;

    private Rectangle airSwitch;
    private Rectangle doorRect;

    private boolean inRange = false;   // เช็คว่าตัวละครอยู่ใกล้ switch หรือไม่
    private boolean acOff = false;      // เช็คว่า air conditioner ปิดแล้วหรือยัง

    private Array<Rectangle> collisionRects;

    private MapLayer normalForeground; // เก็บเลเยอร์ที่จะเรนเดอร์ทับตัวละคร

    private Texture bookIcon;
    private boolean bookVisible = false;

    private final InventoryUI inventory; // เพิ่ม InventoryUI

    private boolean isQuest1Completed = false; // สำหรับ Quest 1

    private float isPressedTime = 0f; //แก้บัคตัวละครติด
    private boolean disableCollision = false;

    public LibraryScreen(Main game, InventoryUI inventory) {
        this.game = game;
        this.inventory = inventory;
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("LibMap/LibMap.tmx");
        font = new BitmapFont();
        collisionRects = new Array<>();
        bookIcon = new Texture("Icons/book.PNG");

        // ขนาดแมพ
        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tilePixel = map.getProperties().get("tilewidth", Integer.class);

        System.out.println("Map width in tiles: " + mapWidth);
        System.out.println("Map height in tiles: " + mapHeight);
        System.out.println("Tile size in pixels: " + tilePixel);

        //พิกัด pixel
        float centerX = mapWidth * tilePixel * 0.5f;
        float centerY = mapHeight * tilePixel * 0.5f;
        System.out.println("Center of map in pixels: (" + centerX + ", " + centerY + ")");

        //unitScale
        float centerX_scaled = centerX * unitScale;
        float centerY_scaled = centerY * unitScale;
        System.out.println("Center of map after unitScale: (" + centerX_scaled + ", " + centerY_scaled + ")");

        // สร้าง camera และ viewport แค่ครั้งเดียว
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() * unitScale *2.5f,
            Gdx.graphics.getHeight() * unitScale *2.5f
        );

        camera = new OrthographicCamera();
        camera.update();
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() * unitScale,
            Gdx.graphics.getHeight() * unitScale
        );

        // char moving
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

        playerTex = new Texture("CharMove/Girl_Front.png");
        currentTex = playerTex;

        // เริ่มกลางแมพ
        playerX = 1400;
        playerY = 1030;

        camera.position.set(playerX, playerY, 0);
        camera.update();

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

        // ดึง Object Layer ชื่อ "Switch"
        MapLayer switchLayer = map.getLayers().get("Switch");
        if (switchLayer != null) {
            for (MapObject obj : switchLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    RectangleMapObject rectObj = (RectangleMapObject) obj;
                    Rectangle rect = rectObj.getRectangle();
                    // สร้าง airSwitch สำหรับตรวจจับคลิก
                    airSwitch = new Rectangle(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }

        // ดึง Object Layer ชื่อ "Door"
        MapLayer doorLayer = map.getLayers().get("Door");
        if (doorLayer != null) {
            for (MapObject obj : doorLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    RectangleMapObject rectObj = (RectangleMapObject) obj;
                    Rectangle rect = rectObj.getRectangle();
                    doorRect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        // อัปเดตว่าตัวละครอยู่ใกล้ switch หรือไม่
        updateInteraction();
        checkClick();
        checkDoorCollision();  // สำหรับ Door

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        renderer.setView(camera);
        renderer.render(new int[]{0,1}); // วาด Map

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(currentTex, playerX, playerY, playerTex.getWidth()/10f, playerTex.getHeight()/10f);

        float bookIconX = 1400;
        float bookIconY = 1200;
        if (bookVisible) {
            batch.draw(bookIcon, bookIconX, bookIconY, 40, 40);
        }

        // แสดงข้อความเฉพาะตอนอยู่ใกล้ switch
        if (inRange) {
            if (!acOff) {
                font.draw(batch, "Turn off air conditioner", airSwitch.x, airSwitch.y + airSwitch.height);
            } else {
                font.draw(batch, "Air conditioner is OFF!", airSwitch.x, airSwitch.y + airSwitch.height);
            }
        }
        batch.end();

        if (normalForeground != null) {
            OrthogonalTiledMapRenderer tempRenderer = new OrthogonalTiledMapRenderer(map, unitScale);
            tempRenderer.setView(camera);

            int index = map.getLayers().getIndex(normalForeground);
            if (index != -1) {
                tempRenderer.render(new int[]{index});
            }

            tempRenderer.dispose();
        }

        renderer.render(new int[]{2});
    }

    private void handleInput(float delta) {
        float nextX = playerX;
        float nextY = playerY;
        boolean moving = false; // เช็คว่ากดปุ่มเดินหรือไม่
        Texture[] walkFrames = null;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            nextY += speed * delta;
            moving = true;
            walkFrames = backWalk;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            isPressedTime += delta; // เพิ่มเวลาที่กดค้าง
            nextY -= speed * delta; // เดินลงปกติ
            moving = true;
            walkFrames = frontWalk;

            if (isPressedTime >= 2f) {
                disableCollision = true; // ปิด collision หลัง 2 วินาที
            }
        } else {
            isPressedTime = 0f;       // รีเซ็ตเวลา
            disableCollision = false; // เปิด collision ปกติ
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

        // ====== อัปเดต animation ======
        if (walkFrames != previousWalkFrames) {
            currentFrame = 0;
            walkTime = 0f;
            previousWalkFrames = walkFrames;
        }

        if (moving && walkFrames != null) {
            walkTime += delta;
            if (walkTime >= frameDuration) {
                walkTime = 0f;
                currentFrame++;
                if (currentFrame >= walkFrames.length) currentFrame = 0;
            }
            currentTex = walkFrames[currentFrame];
        } else {
            // ไม่เดิน → กลับเป็นท่ายืน
            if (walkFrames != null && walkFrames.length > 0) {
                currentTex = walkFrames[1]; // ใช้เฟรมกลางเป็นท่ายืน
            }
        }

        //ขนาดภาพที่วาดจริง
        float characterDrawWidth = currentTex.getWidth() / 10f;
        float characterDrawHeight = currentTex.getHeight() / 10f;

        //hitbox ควรเล็กกว่าภาพ เพื่อไม่ให้หัวชน
        float playerWidth = characterDrawWidth * 0.8f;  // 50% ของความกว้าง (แคบกว่า)
        float playerHeight = characterDrawHeight * 0.5f; // 40% ของความสูง (เฉพาะตัวล่าง)

        //offset ให้ hitbox อยู่ตรงกลางแนวนอน และอยู่ที่เท้า
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

        // กำหนดให้กล้องอยู่กลางแมพตลอด
        float mapCenterX = 1400;
        float mapCenterY = 1360;
        camera.position.set(mapCenterX, mapCenterY, 0);
        camera.update();

        if (bookVisible && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);
            Rectangle bookRect   = new Rectangle(1400, 1200, 40, 40); // ตำแหน่งเดียวกับที่วาด

            if (playerRect.overlaps(bookRect)) {
                // อัปเดตเควส + อินเวนทอรี
                boolean ok = inventory.collectItem("book");   // ← ไอคอนจะหายเทาทันที
                if (ok) {
                    game.questManager.onCollectItem("book");
                    isQuest1Completed = true;
                    // ซ่อนรูป/กันกดซ้ำ (ถ้าต้องการ)
                    bookVisible = false;
                }
            }
        }
    }

    private void updateInteraction() {
        // สร้าง Rectangle ของตัวละคร (สมมติขนาด 32x32)
        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);
        // ตรวจสอบว่า player อยู่ใน range ของ switch
        if (airSwitch != null && playerRect.overlaps(airSwitch)) {
            inRange = true;
        } else {
            inRange = false;
        }
    }

    private void checkClick() {
        if (inRange && Gdx.input.justTouched()) {
            acOff = true;
            bookVisible = true;

            map.dispose();
            map = new TmxMapLoader().load("LibMap/LibMapNormal.tmx");
            renderer = new OrthogonalTiledMapRenderer(map, unitScale);
            // เก็บชื่อเลเยอร์ที่จะวาดทับ
            normalForeground = map.getLayers().get("normalOne");
        }
    }

    private void checkDoorCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);
        if (doorRect != null && playerRect.overlaps(doorRect)) {
            game.setScreen(game.firstScreen);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        playerTex.dispose();
        map.dispose();
        renderer.dispose();
    }
}
