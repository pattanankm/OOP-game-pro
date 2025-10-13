package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MainMenuScreen implements Screen {

    final Main game;
    Texture backgroundTexture;
    Texture startButtonTexture;

    Rectangle startButton;
    Vector3 touchPos;
    boolean isPressed = false;

    // สำหรับ input ชื่อ
    String playerName = "";
    Rectangle nameInputBox;
    GlyphLayout layout;
    boolean isInputActive = false;
    float cursorBlinkTime = 0;

    public MainMenuScreen(final Main game) {
        this.game = game;

        // โหลด background และปุ่ม
        backgroundTexture = new Texture("LobbyScreen.png");
        startButtonTexture = new Texture("character.png");

        // กำหนดขนาดและตำแหน่งปุ่ม
        float buttonWidth = 2.4f;
        float buttonHeight = 1.2f;
        float buttonX = (game.viewport.getWorldWidth() - buttonWidth) / 2;
        float buttonY = game.viewport.getWorldHeight() / 7;

        startButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        // กำหนดขนาดและตำแหน่งกล่อง input ชื่อ
        float inputWidth = 5f;
        float inputHeight = 0.8f;
        float inputX = (game.viewport.getWorldWidth() - inputWidth) / 2;
        float inputY = buttonY + buttonHeight + 0.2f;

        nameInputBox = new Rectangle(inputX, inputY, inputWidth, inputHeight);

        touchPos = new Vector3();
        layout = new GlyphLayout();

        // ตั้งค่า Input Processor
        setupInputProcessor();
    }

    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!isInputActive) return false;

                if (character == '\b' && playerName.length() > 0) {
                    // Backspace - ลบตัวอักษร
                    playerName = playerName.substring(0, playerName.length() - 1);
                } else if (character == '\r' || character == '\n') {
                    // Enter - ปิด input
                    isInputActive = false;
                } else if (character >= 32 && character < 127) {
                    // ตัวอักษรปกติ (จำกัดความยาว 15 ตัว)
                    if (playerName.length() < 15) {
                        playerName += character;
                    }
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                game.viewport.unproject(touchPos);

                // เช็คว่ากดที่กล่อง input หรือไม่
                if (nameInputBox.contains(touchPos.x, touchPos.y)) {
                    isInputActive = true;
                    return true;
                } else {
                    isInputActive = false;
                }

                return false;
            }
        });
    }

    @Override
    public void show() {
        setupInputProcessor();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        // อัพเดท cursor blink
        cursorBlinkTime += delta;

        game.batch.begin();

        // วาด background
        game.batch.draw(backgroundTexture,
            0, 0,
            game.viewport.getWorldWidth(),
            game.viewport.getWorldHeight());

        // วาดกล่อง input ชื่อ
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // พื้นหลังกล่อง input - เปลี่ยนสีถ้ากำลังพิมพ์
        if (isInputActive) {
            game.batch.setColor(0.3f, 0.3f, 0.5f, 0.9f);
        } else {
            game.batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        }
        game.batch.draw(game.pixel,
            nameInputBox.x, nameInputBox.y,
            nameInputBox.width, nameInputBox.height);
        game.batch.setColor(Color.WHITE);

        // ข้อความ "Enter Your Name:"
        game.font.setColor(Color.YELLOW);
        String labelText = "Enter Your Name:";
        layout.setText(game.font, labelText);
        game.font.draw(game.batch, labelText,
            nameInputBox.x,
            nameInputBox.y + nameInputBox.height + 0.5f);

        // แสดงชื่อที่พิมพ์
        game.font.setColor(Color.WHITE);
        String displayName = playerName.isEmpty() ? "Click here to type" : playerName;

        // แสดง cursor กระพริบถ้ากำลังพิมพ์
        if (isInputActive && (int)(cursorBlinkTime * 2) % 2 == 0) {
            displayName += "|";
        }

        game.font.draw(game.batch, displayName,
            nameInputBox.x + 0.1f,
            nameInputBox.y + nameInputBox.height / 2 + 0.15f);

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // วาดปุ่ม START - เปลี่ยนสีตามการกด
        if (isPressed) {
            // เมื่อกดปุ่ม - สีจะทึบขึ้น (มืดลง)
            game.batch.setColor(0.7f, 0.7f, 0.7f, 1f);
        } else {
            // ปกติ - สีปกติ
            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        game.batch.draw(startButtonTexture,
            startButton.x, startButton.y,
            startButton.width, startButton.height);

        // รีเซ็ตสีกลับเป็นปกติ
        game.batch.setColor(Color.WHITE);

        game.batch.end();

        // ตรวจสอบการกดปุ่ม START
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPos);

            // ถ้ากดที่ปุ่ม START
            if (startButton.contains(touchPos.x, touchPos.y)) {
                isPressed = true;
            } else {
                isPressed = false;
            }
        } else {
            // เมื่อปล่อยปุ่ม
            if (isPressed && !playerName.isEmpty()) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
            isPressed = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        startButtonTexture.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
