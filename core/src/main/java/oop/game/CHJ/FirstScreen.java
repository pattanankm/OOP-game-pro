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

public class FirstScreen implements Screen {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    // create char
    private SpriteBatch batch;
    private Texture frontTex, backTex, leftTex, rightTex;
    private Texture currentTex;

    // char position
    private float playerX, playerY;
    private float speed = 250f; // walk speed

    // ขอบเขตที่เราต้องการให้ตัวละครเดินได้
    private final float MIN_X = 800; //ขอบซ้ายสุด
    private final float MAX_X = 3300; //ขอบขวาสุด
    private final float MIN_Y = 560; //ขอบล่างสุด
    private final float MAX_Y = 2740; //ขอบบนสุด

    @Override
    public void show() {
        map = new TmxMapLoader().load("Map/Map.tmx");

        float unitScale = 1f /2.5f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() * unitScale,
            Gdx.graphics.getHeight() * unitScale
        );
        camera.update();

        // char moving
        frontTex  = new Texture("CharMove/Girl_Front.png");
        backTex   = new Texture("CharMove/Girl_Back.png");
        leftTex   = new Texture("CharMove/Girl_Left.png");
        rightTex  = new Texture("CharMove/Girl_Right.png");

        // start with front view
        currentTex = frontTex;

        batch = new SpriteBatch();

        // character in the center
        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tilePixel = map.getProperties().get("tilewidth", Integer.class);

        float spawnX = (mapWidth * tilePixel * 0.5f) / 2.5f;
        float spawnY = (mapHeight * tilePixel * 0.5f) / 2.5f;

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
        renderer.render(new int[]{2, 3,4,5}); // Ground + Outsider + Grass + Street

        // วาดตัวละคร
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float tileSize = 8f;
        batch.draw(currentTex, playerX, playerY,
            currentTex.getWidth() / tileSize,
            currentTex.getHeight() / tileSize
        );
        batch.end();

        // วาด layer ด้านบนทับตัวละคร
        renderer.render(new int[]{6});
    }

    private void handleInput(float delta) {
        boolean moved = false;

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += speed * delta;
            currentTex = backTex;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= speed * delta;
            currentTex = frontTex;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= speed * delta;
            currentTex = leftTex;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += speed * delta;
            currentTex = rightTex;
            moved = true;
        }

        // character camera update
        if (moved) {
            camera.position.set(playerX, playerY, 0);
            camera.update();
        }

        // ป้องกันไม่ให้ตัวละครออกจากขอบที่กำหนดเอง
        if (playerX < MIN_X) playerX = MIN_X;
        if (playerY < MIN_Y) playerY = MIN_Y;
        if (playerX > MAX_X) playerX = MAX_X;
        if (playerY > MAX_Y) playerY = MAX_Y;
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
        backTex.dispose();
        leftTex.dispose();
        rightTex.dispose();
    }
}
