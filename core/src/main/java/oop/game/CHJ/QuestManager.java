package oop.game.CHJ;

public class QuestManager {

    // Quest steps:
    // 0 = เริ่มเกม
    // 1 = คุยช้างแล้ว
    // 2 = คุยยีราฟแล้ว
    // 3 = ลูบมือแล้ว
    // 4 = เก็บเกียร์แล้ว
    // 5 = เข้าห้องสมุด/เก็บหนังสือแล้ว
    // 6 = ปิดแอร์แล้ว (พร้อมไปขอพร)

    private int questStep;
    private boolean gearCollected;
    private boolean bookCollected;
    private boolean airOff;
    private boolean elephantTalked; // เพิ่มตัวแปรตรวจสอบว่าคุยช้างแล้วหรือยัง

    public QuestManager() {
        SaveManager.SaveState s = SaveManager.hasSave() ? SaveManager.load() : new SaveManager.SaveState();
        this.questStep = s.questStep;
        this.gearCollected = s.gear;
        this.bookCollected = s.book;
        this.airOff = s.airOff;
        this.elephantTalked = s.questStep >= 1; // ถ้า questStep >= 1 แสดงว่าคุยช้างแล้ว
    }

    public int getQuestStep() { return questStep; }
    public boolean isGearCollected() { return gearCollected; }
    public boolean isBookCollected() { return bookCollected; }
    public boolean isAirOff() { return airOff; }
    public boolean isElephantTalked() { return elephantTalked; }

    private void persist() {
        SaveManager.setItems(gearCollected, bookCollected);
        SaveManager.setQuestStep(questStep);
        if (airOff) SaveManager.markAirOff();
    }

    public void updatePlayerPosition(String screenName, float x, float y) {
        SaveManager.setScreen(screenName);
        SaveManager.setPosition(x, y);
    }

    // เรียกเมื่อคุยกับช้าง
    public void onTalkElephant() {
        if (questStep < 1) {
            questStep = 1;
            elephantTalked = true;
            persist();
        }
    }

    // เรียกเมื่อคุยกับยีราฟ
    public void onTalkGiraffe() {
        if (questStep < 2) {
            questStep = 2;
            persist();
        }
    }

    // เรียกเมื่อลูบมือ
    public void onTouchHand() {
        if (questStep < 3) {
            questStep = 3;
            persist();
        }
    }

    // เรียกเมื่อเก็บไอเทม
    public void onCollectItem(String id) {
        if ("gear".equalsIgnoreCase(id)) {
            gearCollected = true;
            if (questStep < 4) questStep = 4;
        } else if ("book".equalsIgnoreCase(id)) {
            bookCollected = true;
            if (questStep < 5) questStep = 5;
        }
        persist();
    }

    // เรียกเมื่อเข้าห้องสมุด
    public void onGoLibrary() {
        if (questStep < 5 && gearCollected) {
            // ถ้าเก็บเกียร์แล้ว ให้เข้าห้องสมุดได้
        }
        persist();
    }

    // เรียกเมื่อปิดแอร์ที่ห้องสมุด
    public void onAirOff() {
        airOff = true;
        if (questStep < 6) questStep = 6;
        persist();
    }

    // ตรวจสอบว่าพร้อมไปขอพรหรือยัง
    public boolean canMakeWish() {
        return questStep >= 6 && gearCollected && bookCollected && airOff;
    }

    // เมธอดเพื่อความเข้ากันได้
    public boolean isQuest1Started() { return questStep >= 2; }
    public boolean isQuest1Completed() { return questStep >= 2; }
    public boolean isQuest2Started() { return questStep >= 3; }
    public boolean isQuest2Completed() { return questStep >= 3; }
}
