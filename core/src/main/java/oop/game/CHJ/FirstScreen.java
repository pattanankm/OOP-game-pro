package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FirstScreen implements Screen {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private SpriteBatch batch;
    private Texture frontTex;
    private Texture currentTex;

    private float playerX, playerY; //ตำแหน่งตัวละครเป็นแกน X,Y
    private float speed = 160f; // walk speed

    // ขอบเขตที่เราต้องการให้ตัวละครเดินได้
    private final float MIN_X = 800; //ขอบซ้ายสุด
    private final float MAX_X = 3300; //ขอบขวาสุด
    private final float MIN_Y = 560; //ขอบล่างสุด
    private final float MAX_Y = 2740; //ขอบบนสุด


    private Array<Rectangle> collisionRects;
    private Array<NPC> npcs;

    // unitScale เพื่อให้ใช้ได้ทั้งคลาส
    private final float unitScale = 1f / 2.5f;

    //ใช้กับท่าเดิน
    private int currentFrame = 0;      // เฟรมปัจจุบัน
    private float walkTime = 0f;       // เวลาเดินสะสม
    private float frameDuration = 0.2f; // เวลาต่อเฟรม (วินาที)
    private Texture[] frontWalk, backWalk, leftWalk, rightWalk, Jump;

    // สำหรับการกระโดด
    private boolean isJumping = false;  // เช็คว่ากำลังกระโดดอยู่ไหม

    //ข้อความ
    private BitmapFont font;

    //แชท
    private Texture chatIcon;
    private Texture handIcon;

    private Texture gearIcon;
    private boolean objectVisible = false;

    private Main game; //เพิ่มตัวแปรไว้เก็บเกมหลัก (แก้ตอนใช้game.ไม่ได้)

    public FirstScreen(Main game) { //สร้าง constructor ใหม่
        this.game = game;
    }


    @Override
    public void show() {
        map = new TmxMapLoader().load("Map/Map.tmx");
        font = new BitmapFont(); // สร้าง font
        chatIcon = new Texture("Icons/Chat_Icon.png");
        handIcon = new Texture("Icons/Hand_Icon.png");
        collisionRects = new Array<>();
        npcs = new Array<>();

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

        renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() * unitScale,
            Gdx.graphics.getHeight() * unitScale
        );

        camera.update();

        //สร้าง npc
        NPC penguin = new NPC(
            2358,  // กลางแมพ X
            2340,  // กลางแมพ Y
            "NPC/Penguin_Stand.png",
            "Today is very cold~",
            "Penguin"
        );
        npcs.add(penguin);

        NPC giraffe = new NPC(1400,
            1700,
            "NPC/Giraffe_Stand.png",
            "We are all Entaneer!",
            "Giraffe"
        );
        npcs.add(giraffe);

        MapLayer objectLayer = map.getLayers().get("Collision");
        if (objectLayer != null) {
            for (MapObject obj : objectLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    rect.set(rect.x * unitScale, rect.y * unitScale, rect.width * unitScale, rect.height * unitScale);
                    collisionRects.add(rect);
                }
            }
            System.out.println("Loaded " + objectLayer.getObjects().getCount() + " collision rectangles");
        }

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
        Jump = new Texture[]{
            new Texture("CharMove/Girl_Jump.png")
        };

        // start with front view
        frontTex  = new Texture("CharMove/Girl_Front.png");
        currentTex = frontTex;

        batch = new SpriteBatch();

        // character in the center
        float spawnX = (mapWidth * tilePixel * 0.495f) / 2.5f;
        float spawnY = (mapHeight * tilePixel * 0.48f) / 2.5f;
        playerX = spawnX;
        playerY = spawnY;

        // set camera
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }



    @Override
    public void render(float delta) {
        handleInput(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // clear the screen
        camera.update();
        renderer.setView(camera);

//        เอาไว้เช็คชื่อเลเยอร์
//        for (int i = 0; i < map.getLayers().getCount(); i++) {
//            System.out.println("Layer " + i + ": " + map.getLayers().get(i).getName());
//        }

        // วาดพื้นก่อน
        renderer.render(new int[]{2,3,4,5}); // Ground + Outsider + Grass + Street

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // วาด NPC
        for (NPC npc : npcs) {
            batch.draw(npc.texture, npc.x, npc.y, npc.width, npc.height);

            if (npc.isPlayerNear(playerX, playerY, 80f)) {
                font.draw(batch, npc.dialogue, npc.x, npc.y + npc.height + 20);
                batch.draw(chatIcon, npc.x + 55 , npc.y + 60, 24, 24);
            }
        }

        // ถ้าเควส 1 เริ่มแล้ว
        Rectangle triggerArea = new Rectangle(2200, 2400, 100, 100);// สร้าง rectangle ของ player (hitbox)
        Rectangle playerRect = new Rectangle(playerX, playerY, 100, 150);// ตรวจว่า player อยู่ใกล้ trigger area และผู้เล่นกดหน้าจอ

        // x, y, width, height ปรับตามตำแหน่งข้อความจริง
        if (game.questManager.isQuest1Started()) {
            if (playerRect.overlaps(triggerArea)) {
                font.draw(batch, "Tap to get inside!", 2240, 2440);
                if (Gdx.input.justTouched()) {
                    game.setScreen(new LibraryScreen(game));
                }
            }
        }

        // ถ้าเควส 2 เริ่มแล้ว
        Rectangle handRect = new Rectangle(1600, 800, 50, 50);
        if (game.questManager.isQuest2Started()) {
            batch.draw(handIcon, handRect.x, handRect.y, handRect.width, handRect.height);
            font.draw(batch, "loob!", handRect.x, handRect.y + handRect.height + 20);
        }
        // ตรวจคลิก
        if (Gdx.input.justTouched()) {
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click); // แปลงจาก screen -> world

            if (handRect.contains(click.x, click.y)) {
                objectVisible = true; // ให้ object ใหม่โผล่
            }
        }

        gearIcon = new Texture("Icons/gear.PNG");
        float gearIconX = 1650;
        float gearIconY = 860;

        // วาด object ใหม่ถ้าเปิดแล้ว
        if (objectVisible) {
            batch.draw(gearIcon, gearIconX, gearIconY,40,40);
        }

        // วาดตัวละคร
        float tileSize = 10f;
        batch.draw(currentTex, playerX, playerY,
            currentTex.getWidth() / tileSize,
            currentTex.getHeight() / tileSize
        );


        batch.end();

        // วาด layer ด้านบนทับตัวละคร
        renderer.render(new int[]{6});

        // ====== ตรวจคลิกบน chat icon ======
        if (Gdx.input.justTouched()) {
            // หาตำแหน่งคลิกในแกน world
            com.badlogic.gdx.math.Vector3 click = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(click); // แปลงพิกัดจากจอ -> world

            // วนเช็คทุก NPC ที่อยู่ใกล้
            for (NPC npc : npcs) {
                if (npc.isPlayerNear(playerX, playerY, 80f)) {
                    // สร้างกรอบ hitbox ของ chat icon
                    Rectangle chatRect = new Rectangle(npc.x + 55, npc.y + 60, 24, 24);
                    if (chatRect.contains(click.x, click.y)) {
                        game.setScreen(new DialogueScreen(game, npc)); // ไปหน้าพูดคุย
                        break;
                    }
                }
            }
        }
    }

    private Texture[] previousWalkFrames = null; // เก็บ walkFrames เดิม

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            isJumping = true;
            currentTex = Jump[0];

            // ตั้งเวลาให้กลับมาท่ายืนหลังจาก 0.3 วินาที
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    isJumping = false;

                    // ถ้าไม่ได้กดเดินอยู่ ให้กลับมาภาพยืน
                    if (!Gdx.input.isKeyPressed(Input.Keys.UP) &&
                        !Gdx.input.isKeyPressed(Input.Keys.DOWN) &&
                        !Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
                        !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                        currentTex = frontTex;
                    }
                }
            }, 0.3f); // ระยะเวลากระโดด (วินาที)
        }

        for (NPC npc : npcs) {
            if (npc.isPlayerNear(playerX, playerY, 80F)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    game.setScreen(new DialogueScreen(game, npc)); // ไปหน้าพูดคุย
                }
            }
        }

        // ถ้าเปลี่ยนทิศทาง รีเซ็ต currentFrame (แก้ปัญหาจอปิดเอง)
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
                // หมุนวนเฟรม
                if (currentFrame >= walkFrames.length) currentFrame = 0; // สมมติทุกทิศมีจำนวนเฟรมเท่ากัน
            }
            currentTex = walkFrames[currentFrame];

        } else {
            // ไม่เดิน → รีเซ็ตเฟรม
            currentFrame = 0;
        }

        // สร้างกล่อง hitbox ของตัวละคร
        float playerWidth = currentTex.getWidth() / 10f * 1f;
        float playerHeight = currentTex.getHeight() / 10f * 0.4f;

        // ปรับ offset เพื่อให้ hitbox อยู่ตรงกลางและด้านล่างของตัวละคร
        float offsetX = (currentTex.getWidth() / 10f - playerWidth) / 2f;
        float offsetY = 0; // hitbox อยู่ที่เท้า

        // แยกแกน X
        Rectangle rectX = new Rectangle(
            nextX + offsetX,
            playerY + offsetY,
            playerWidth,
            playerHeight
        );
        boolean collidedX = false;
        for (Rectangle r : collisionRects) {
            if (rectX.overlaps(r)) {
                collidedX = true;
                break;
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
        for (Rectangle r : collisionRects) {
            if (rectY.overlaps(r)) {
                collidedY = true;
                break;
            }
        }
        if (!collidedY) playerY = nextY;



        // ป้องกันไม่ให้ตัวละครออกจากขอบที่กำหนดเอง
        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;

        // character camera update
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        float unitScale = 1f / 1f;
        camera.setToOrtho(false, //start camera in the center
            width * unitScale,
            height * unitScale
        );
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        frontTex.dispose();
        chatIcon.dispose();
        handIcon.dispose();

        // dispose NPC textures
        for (NPC npc : npcs) {
            npc.dispose();
        }
    }
}
