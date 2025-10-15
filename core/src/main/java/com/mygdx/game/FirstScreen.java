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
import com.badlogic.gdx.math.Vector3;

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
    private float victoryTimer = 0;
    private Rectangle tryAgainButton;
    private Rectangle quitButton;
    private Vector3 touchPoint;

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
        tryAgainButton = new Rectangle(300, 150, 200, 60);
        quitButton = new Rectangle(300, 70, 200, 60);
        touchPoint = new Vector3();

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
            if (gameResult.equals("VICTORY")) {
                drawVictoryScreen(delta);
            } else {
                drawGameOver();
            }
            drawButtons();
        }

        batch.end();

        if (!gameFinished) {
            updateGame(delta);
        } else {
            handleInput();
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (tryAgainButton.contains(touchPoint.x, touchPoint.y)) {
                restartGame();
            } else if (quitButton.contains(touchPoint.x, touchPoint.y)) {
                Gdx.app.exit();
            }
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
                System.out.println("ไอติมโดนผู้เล่นHPลด" + icecream.getDamage());
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
        drawHealthBar(20, 550, player.getHealth(), 500, Color.GREEN);
        font.draw(batch, "PLAYER HP: " + player.getHealth(), 20, 530);

        drawHealthBar(600, 550, boss.getHealth(), 500, Color.RED);
        font.draw(batch, "BOSS HP: " + boss.getHealth(), 600, 530);

        font.draw(batch, "WEAPON: " + player.getCurrentWeapon(), 300, 550);
        drawWeaponSelection();
        font.draw(batch, "A/D: Move, SPACE: Attack", 20, 40);
        font.draw(batch, "1/2: Switch Weapon", 20, 20);
    }

    private void drawWeaponSelection() {
        float iconX = 450;
        float iconY = 540;

        if (player.getCurrentWeapon().equals("BOOK")) {
            batch.draw(player.bookTexture, iconX, iconY, 40, 30);
        } else {
            batch.setColor(Color.YELLOW);
            batch.draw(player.gearTexture, iconX, iconY, 40, 40);
            batch.setColor(Color.WHITE);
        }

        font.draw(batch, "[1] BOOK   [2] GEAR", 300, 520);
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

    private void drawVictoryScreen(float delta) {
        victoryTimer += delta;

        // พื้นหลัง
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(whitePixel, 0, 0, 800, 600);
        batch.setColor(1, 1, 1, 1);
        font.getData().setScale(3.0f);
        batch.setColor(1, 0.9f, 0.2f, 1);
        font.draw(batch, "VICTORY!", 250, 450);
        batch.setColor(1, 1, 1, 1);
        font.getData().setScale(1.8f);
        batch.setColor(0.2f, 1, 0.2f, 1);
        font.draw(batch, "YOU DEFEATED THE ELEPHANT BOSS!", 120, 380);
        batch.setColor(1, 1, 1, 1);
        font.getData().setScale(1.5f);
        batch.setColor(1, 1, 0.3f, 1);
        font.draw(batch, "CONGRATULATIONS!", 240, 320);
        batch.setColor(1, 1, 1, 1);
    }

    private void drawGameOver() {
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(whitePixel, 0, 0, 800, 600);
        batch.setColor(1, 1, 1, 1);

        font.getData().setScale(2.5f);
        batch.setColor(1, 0.2f, 0.2f, 1);
        font.draw(batch, "GAME OVER", 280, 450);

        font.getData().setScale(1.5f);
        batch.setColor(1, 0.6f, 0.2f, 1);
        font.draw(batch, "The elephant was too strong!", 220, 380);

        font.getData().setScale(1.2f);
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, "Better luck next time!", 300, 320);

        font.getData().setScale(1.3f);
    }

    private void drawButtons() {
        boolean tryAgainHover = isButtonHovered(tryAgainButton);
        batch.setColor(tryAgainHover ? 0.3f : 0.2f, 0.7f, 0.3f, 0.9f);
        batch.draw(whitePixel, tryAgainButton.x, tryAgainButton.y, tryAgainButton.width, tryAgainButton.height);
        batch.setColor(0.1f, 0.5f, 0.1f, 1);
        batch.draw(whitePixel, tryAgainButton.x, tryAgainButton.y, tryAgainButton.width, 3);
        batch.draw(whitePixel, tryAgainButton.x, tryAgainButton.y + tryAgainButton.height - 3, tryAgainButton.width, 3);
        batch.draw(whitePixel, tryAgainButton.x, tryAgainButton.y, 3, tryAgainButton.height);
        batch.draw(whitePixel, tryAgainButton.x + tryAgainButton.width - 3, tryAgainButton.y, 3, tryAgainButton.height);

        font.getData().setScale(1.5f);
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, "TRY AGAIN",
                tryAgainButton.x + 40,
                tryAgainButton.y + tryAgainButton.height / 2 + 10);
        boolean quitHover = isButtonHovered(quitButton);
        batch.setColor(quitHover ? 0.9f : 0.8f, 0.3f, 0.3f, 0.9f);
        batch.draw(whitePixel, quitButton.x, quitButton.y, quitButton.width, quitButton.height);
        batch.setColor(0.5f, 0.1f, 0.1f, 1);
        batch.draw(whitePixel, quitButton.x, quitButton.y, quitButton.width, 3);
        batch.draw(whitePixel, quitButton.x, quitButton.y + quitButton.height - 3, quitButton.width, 3);
        batch.draw(whitePixel, quitButton.x, quitButton.y, 3, quitButton.height);
        batch.draw(whitePixel, quitButton.x + quitButton.width - 3, quitButton.y, 3, quitButton.height);

        batch.setColor(1, 1, 1, 1);
        font.draw(batch, "QUIT",
                quitButton.x + 70,
                quitButton.y + quitButton.height / 2 + 10);

        font.getData().setScale(1.3f);
        batch.setColor(1, 1, 1, 1);
    }

    private boolean isButtonHovered(Rectangle button) {
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        return button.contains(touchPoint.x, touchPoint.y);
    }

    private void drawStatBox(float x, float y, float width, float height) {
        batch.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        batch.draw(whitePixel, x, y, width, height);

        batch.setColor(1, 0.9f, 0.3f, 0.5f);
        batch.draw(whitePixel, x, y, width, 3);
        batch.draw(whitePixel, x, y + height - 3, width, 3);
        batch.draw(whitePixel, x, y, 3, height);
        batch.draw(whitePixel, x + width - 3, y, 3, height);

        batch.setColor(1, 1, 1, 1);
    }

    private void restartGame() {
        player.dispose();
        boss.dispose();
        player = new Player(50, 50);
        boss = new ElephantBoss(550, 50);
        gameFinished = false;
        victoryTimer = 0;
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
