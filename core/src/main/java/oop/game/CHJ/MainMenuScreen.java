package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MainMenuScreen implements Screen {

    final Main game;
    Texture backgroundTexture;
    Texture nameBoxTexture;
    Texture characterButtonTexture;
    Texture startButtonTexture;
    Texture maleTexture;
    Texture femaleTexture;

    Rectangle nameInputBox;
    Rectangle characterButton;
    Rectangle startButton;
    Rectangle maleButton;
    Rectangle femaleButton;
    Rectangle selectedCharacterDisplay;

    Vector3 touchPos;
    GlyphLayout layout;

    String playerName = "";
    boolean isInputActive = false;
    boolean nameConfirmed = false;
    float cursorBlinkTime = 0;

    boolean showCharacterSelection = false;
    String selectedGender = "";

    boolean isCharacterButtonPressed = false;
    boolean isStartButtonPressed = false;
    boolean isMalePressed = false;
    boolean isFemalePressed = false;

    public MainMenuScreen(final Main game) {
        this.game = game;

        // โหลดภาพ
        backgroundTexture = new Texture("menu.png");
        nameBoxTexture = new Texture("name.png");
        characterButtonTexture = new Texture("character.png");
        startButtonTexture = new Texture("start.png");
        maleTexture = new Texture("male.png");
        femaleTexture = new Texture("female.png");

        // ตำแหน่ง element ต่าง ๆ
        float nameBoxWidth = 2.2f;
        float nameBoxHeight = 0.4f;
        float nameBoxX = (game.viewport.getWorldWidth() - nameBoxWidth) / 2;
        float nameBoxY = 1.5f;
        nameInputBox = new Rectangle(nameBoxX, nameBoxY, nameBoxWidth, nameBoxHeight);

        float charButtonWidth = 2.8f;
        float charButtonHeight = 0.7f;
        characterButton = new Rectangle(1.2f, 0.4f, charButtonWidth, charButtonHeight);

        float startButtonWidth = 2.8f;
        float startButtonHeight = 0.7f;
        float startButtonX = game.viewport.getWorldWidth() - startButtonWidth - 1.1f;
        startButton = new Rectangle(startButtonX, 0.4f, startButtonWidth, startButtonHeight);

        // ตัวละครเลือกกลางจอ
        float characterWidth = 2f;
        float characterHeight = 2.5f;
        float centerX = game.viewport.getWorldWidth() / 2;
        float charY = 1.7f;
        maleButton = new Rectangle(centerX - characterWidth - 0.5f, charY, characterWidth, characterHeight);
        femaleButton = new Rectangle(centerX + 0.5f, charY, characterWidth, characterHeight);

        // ✅ ยกตำแหน่งตัวละครที่เลือกให้สูงขึ้น
        float displayCharWidth = 1.6f;
        float displayCharHeight = 2.2f;
        float displayCharX = 0.5f;
        float displayCharY = 1.5f; // ↑ จาก 0.8f เป็น 1.5f
        selectedCharacterDisplay = new Rectangle(displayCharX, displayCharY, displayCharWidth, displayCharHeight);

        touchPos = new Vector3();
        layout = new GlyphLayout();

        setupInputProcessor();
    }

    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!isInputActive) return false;

                if (character == '\b' && playerName.length() > 0) {
                    playerName = playerName.substring(0, playerName.length() - 1);
                } else if (character == '\r' || character == '\n') {
                    nameConfirmed = true;
                    isInputActive = false;
                } else if (character >= 32 && character < 127) {
                    if (playerName.length() < 15) playerName += character;
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                game.viewport.unproject(touchPos);

                if (nameInputBox.contains(touchPos.x, touchPos.y)) {
                    isInputActive = true;
                    showCharacterSelection = false;
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
        cursorBlinkTime += delta;

        game.batch.begin();

        // พื้นหลัง
        game.batch.draw(backgroundTexture, 0, 0,
            game.viewport.getWorldWidth(), game.viewport.getWorldHeight());

        // ชื่อในกล่อง
        game.font.setColor(Color.BLACK);
        String displayName = playerName;
        if (isInputActive && (int) (cursorBlinkTime * 2) % 2 == 0) displayName += "|";
        game.font.draw(game.batch, displayName, nameInputBox.x + 0.3f,
            nameInputBox.y + nameInputBox.height / 2 + 0.15f);

        // ปุ่ม CHARACTER
        game.batch.setColor(isCharacterButtonPressed ? new Color(0.7f, 0.7f, 0.7f, 1f) : Color.WHITE);
        game.batch.draw(characterButtonTexture, characterButton.x, characterButton.y,
            characterButton.width, characterButton.height);

        // ปุ่ม START
        game.batch.setColor(isStartButtonPressed ? new Color(0.7f, 0.7f, 0.7f, 1f) : Color.WHITE);
        game.batch.draw(startButtonTexture, startButton.x, startButton.y,
            startButton.width, startButton.height);
        game.batch.setColor(Color.WHITE);

        // ---------- หน้าต่างเลือกตัวละคร ----------
        if (showCharacterSelection) {
            game.batch.setColor(0, 0, 0, 0.7f);
            game.batch.draw(game.pixel, 0, 0,
                game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
            game.batch.setColor(Color.WHITE);

            game.font.setColor(Color.YELLOW);
            String selectText = "Select Your Character";
            layout.setText(game.font, selectText);
            game.font.draw(game.batch, selectText,
                (game.viewport.getWorldWidth() - layout.width) / 2f, 4.6f);

            game.batch.setColor(isMalePressed ? new Color(0.75f, 0.75f, 0.75f, 1f) : Color.WHITE);
            game.batch.draw(maleTexture, maleButton.x, maleButton.y, maleButton.width, maleButton.height);

            game.batch.setColor(isFemalePressed ? new Color(0.75f, 0.75f, 0.75f, 1f) : Color.WHITE);
            game.batch.draw(femaleTexture, femaleButton.x, femaleButton.y, femaleButton.width, femaleButton.height);
            game.batch.setColor(Color.WHITE);

            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Male", maleButton.x + maleButton.width / 3f, maleButton.y - 0.2f);
            game.font.draw(game.batch, "Female", femaleButton.x + femaleButton.width / 4f, femaleButton.y - 0.2f);
        }

        // ---------- ตัวละครที่เลือก ----------
        if (!selectedGender.isEmpty()) {
            Texture t = selectedGender.equals("male") ? maleTexture : femaleTexture;
            game.batch.draw(t, selectedCharacterDisplay.x, selectedCharacterDisplay.y,
                selectedCharacterDisplay.width, selectedCharacterDisplay.height);

            if (nameConfirmed && !playerName.isEmpty()) {
                // ✅ วาดพื้นหลังโปร่งแสงรอบชื่อ
                layout.setText(game.font, playerName);
                float pad = 0.15f;
                float boxX = selectedCharacterDisplay.x + (selectedCharacterDisplay.width - layout.width) / 2 - pad;
                float boxY = selectedCharacterDisplay.y - 0.45f;
                float boxW = layout.width + pad * 2;
                float boxH = layout.height + pad * 2;

                game.batch.setColor(0, 0, 0, 0.5f);
                game.batch.draw(game.pixel, boxX, boxY - layout.height, boxW, boxH);
                game.batch.setColor(Color.WHITE);

                // ✅ วาดข้อความสีขาวพร้อมขอบดำ (stroke)
                drawOutlinedText(playerName, boxX + pad, boxY, Color.WHITE, Color.BLACK);
            }
        }

        game.batch.end();

        // ---------- ตรวจจับคลิก ----------
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPos);

            if (showCharacterSelection) {
                if (maleButton.contains(touchPos.x, touchPos.y)) isMalePressed = true;
                else if (femaleButton.contains(touchPos.x, touchPos.y)) isFemalePressed = true;
            } else {
                if (characterButton.contains(touchPos.x, touchPos.y)) isCharacterButtonPressed = true;
                else if (startButton.contains(touchPos.x, touchPos.y)) isStartButtonPressed = true;
            }
        } else {
            if (isCharacterButtonPressed) {
                showCharacterSelection = true;
                isCharacterButtonPressed = false;
            }

            if (isMalePressed) {
                selectedGender = "male";
                showCharacterSelection = false;
                isMalePressed = false;
            }
            if (isFemalePressed) {
                selectedGender = "female";
                showCharacterSelection = false;
                isFemalePressed = false;
            }

            if (isStartButtonPressed) {
                if (!playerName.isEmpty() && !selectedGender.isEmpty()) {
                    game.setScreen(new GameScreen(game));
                    dispose();
                }
                isStartButtonPressed = false;
            }
        }
    }

    // ✅ ฟังก์ชันช่วยวาดข้อความมีขอบดำ
    private void drawOutlinedText(String text, float x, float y, Color fillColor, Color outlineColor) {
        float offset = 0.02f;
        game.font.setColor(outlineColor);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                game.font.draw(game.batch, text, x + dx * offset, y + dy * offset);
            }
        }
        game.font.setColor(fillColor);
        game.font.draw(game.batch, text, x, y);
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        backgroundTexture.dispose();
        nameBoxTexture.dispose();
        characterButtonTexture.dispose();
        startButtonTexture.dispose();
        maleTexture.dispose();
        femaleTexture.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
{
}
