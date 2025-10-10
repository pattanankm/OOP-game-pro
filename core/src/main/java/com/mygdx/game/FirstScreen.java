package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class FirstScreen implements Screen {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture background;
    private BitmapFont font;

    private Player player;
    private ElephantBoss boss;
    private AudioManager audioManager;

    private boolean gameFinished = false;
    private String gameResult = "";

    public FirstScreen() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        font = new BitmapFont();

        background = new Texture(Gdx.files.internal("images/background.PNG"));
        audioManager = new AudioManager();

        // ตัวละครทั้งหมดอยู่ด้านล่าง
        player = new Player(100, 100);    // ผู้เล่นอยู่ซ้ายล่าง
        boss = new ElephantBoss(600, 100); // บอสอยู่ขวาล่าง

        audioManager.playBGM();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, 800, 600);

        if (!gameFinished) {
            player.render(batch);
            boss.render(batch);
            drawUI();
        } else {
            drawGameOver();
        }

        batch.end();

        if (!gameFinished) {
            updateGame(delta);
        }
    }

    private void updateGame(float delta) {
        player.update(delta);
        boss.update(delta, player.getBounds().x, player.getBounds().y);

        // ตรวจสอบการชน
        for (int i = player.getBooks().size() - 1; i >= 0; i--) {
            Player.BookProjectile book = player.getBooks().get(i);
            if (book.getBounds().overlaps(boss.getBounds())) {
                boss.takeDamage(10);
                player.getBooks().remove(i);
                audioManager.playSound("shoot");
            }
        }

        // ตรวจสอบการชน
        for (int i = boss.getBooks().size() - 1; i >= 0; i--) {
            ElephantBoss.BossBook book = boss.getBooks().get(i);
            if (book.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(15);
                boss.getBooks().remove(i);
                audioManager.playSound("shoot");
            }
        }

        if (!boss.isAlive()) {
            gameFinished = true;
            gameResult = "VICTORY";
            audioManager.playSound("win");
            audioManager.stopBGM();
        }

        if (!player.isAlive()) {
            gameFinished = true;
            gameResult = "GAME OVER";
            audioManager.playSound("lose");
            audioManager.stopBGM();
        }
    }

    private void drawUI() {
        font.draw(batch, "PLAYER HP: " + player.getHealth(), 20, 580);
        font.draw(batch, "BOSS HP: " + boss.getHealth(), 600, 580);
        font.draw(batch, "A/D: Move, SPACE: Throw Books", 20, 30);
    }

    private void drawGameOver() {
        if (gameResult.equals("VICTORY")) {
            font.draw(batch, "🎉 VICTORY! 🎉", 300, 400);
            font.draw(batch, "You defeated the elephant boss!", 250, 350);
        } else {
            font.draw(batch, "💀 GAME OVER 💀", 300, 400);
            font.draw(batch, "The elephant was too strong!", 260, 350);
        }
        font.draw(batch, "Press R to Restart", 320, 250);

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R)) {
            restartGame();
        }
    }

    private void restartGame() {
        player.dispose();
        boss.dispose();
        player = new Player(100, 100);
        boss = new ElephantBoss(600, 100);
        gameFinished = false;
        audioManager.playBGM();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, 800, 600);
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
        audioManager.dispose();
        player.dispose();
        boss.dispose();
    }
}
