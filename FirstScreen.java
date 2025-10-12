package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Pixmap;

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
    private Texture whitePixel;

    public FirstScreen() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.3f);
        font.setColor(Color.WHITE);

        background = new Texture(Gdx.files.internal("images/background.PNG"));
        audioManager = new AudioManager();

        whitePixel = createWhitePixel();

        player = new Player(50, 50);
        boss = new ElephantBoss(550, 50);

        audioManager.playBGM();
    }

    private Texture createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
            boss.render(batch);
            player.render(batch);
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

        for (int i = player.getProjectiles().size() - 1; i >= 0; i--) {
            Player.Projectile projectile = player.getProjectiles().get(i);
            if (projectile.getBounds().overlaps(boss.getBounds())) {
                boss.takeDamage(projectile.getDamage());
                player.getProjectiles().remove(i);
                audioManager.playSound("shoot");
            }
        }

        for (int i = boss.getIcecreams().size() - 1; i >= 0; i--) {
            ElephantBoss.IcecreamProjectile icecream = boss.getIcecreams().get(i);
            if (icecream.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(icecream.getDamage());
                boss.getIcecreams().remove(i);
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
        drawHealthBar(20, 580, player.getHealth(), 500, Color.GREEN);
        font.draw(batch, "PLAYER HP: " + player.getHealth(), 20, 560);

        drawHealthBar(500, 580, boss.getHealth(), 500, Color.RED);
        font.draw(batch, "BOSS HP: " + boss.getHealth(), 500, 560);

        font.draw(batch, "WEAPON: " + player.getCurrentWeapon(), 300, 580);
        drawWeaponSelection();

        font.draw(batch, "A/D: Move, SPACE:Attack", 20, 40);
        font.draw(batch, "1/2: Switch Weapon", 20, 20);
    }

    private void drawWeaponSelection() {
        float iconX = 450;
        float iconY = 570;

        if (player.getCurrentWeapon().equals("BOOK")) {
            batch.draw(player.bookTexture, iconX, iconY, 40, 30);
        } else {
            batch.setColor(Color.YELLOW);
            batch.draw(player.gearTexture, iconX, iconY, 40, 40);
            batch.setColor(Color.WHITE);
        }

        font.draw(batch, "[1]BOOK   [2]GEAR", 300, 550);
    }

    private void drawHealthBar(float x, float y, int currentHealth, int maxHealth, Color color) {
        float percentage = (float) currentHealth / maxHealth;
        int barWidth = (int) (180 * percentage);

        batch.setColor(Color.DARK_GRAY);
        batch.draw(whitePixel, x, y, 180, 20);

        batch.setColor(color);
        batch.draw(whitePixel, x, y, barWidth, 20);
        batch.setColor(Color.WHITE);

        batch.setColor(Color.BLACK);
        batch.draw(whitePixel, x, y, 180, 2);
        batch.draw(whitePixel, x, y + 18, 180, 2);
        batch.draw(whitePixel, x, y, 2, 20);
        batch.draw(whitePixel, x + 178, y, 2, 20);
        batch.setColor(Color.WHITE);
    }

    private void drawGameOver() {
        font.getData().setScale(1.8f);

        if (gameResult.equals("VICTORY")) {
            font.setColor(Color.GOLD);
            font.draw(batch, "VICTORY!", 320, 400);
            font.setColor(Color.YELLOW);
            font.draw(batch, "You defeated the elephant boss!", 220, 350);
        } else {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 320, 400);
            font.setColor(Color.ORANGE);
            font.draw(batch, "The elephant was too strong!", 240, 350);
        }

        font.getData().setScale(1.3f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Press R to Restart", 300, 250);

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R)) {
            restartGame();
        }
    }

    private void restartGame() {
        player.dispose();
        boss.dispose();
        player = new Player(50, 50);
        boss = new ElephantBoss(550, 50);
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
        whitePixel.dispose();
        audioManager.dispose();
        player.dispose();
        boss.dispose();
    }
}
