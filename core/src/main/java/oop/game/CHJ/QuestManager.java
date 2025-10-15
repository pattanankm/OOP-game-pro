package oop.game.CHJ;

public class QuestManager {

    // Quest1 = ไปคุยยีราฟ, Quest2 = ลูบมือ
    private boolean quest1Started = false, quest1Completed = false;
    private boolean quest2Started = false, quest2Completed = false;

    public void startQuest1() { quest1Started = true; onTalkGiraffe(); }
    public void completeQuest1() { quest1Completed = true; onTalkGiraffe(); }

    public void startQuest2() { quest2Started = true; onTouchHand(); }
    public void completeQuest2() { quest2Completed = true; onTouchHand(); }

    public boolean isQuest1Started() { return quest1Started || questStep >= 1; }
    public boolean isQuest1Completed() { return quest1Completed || questStep >= 1; }
    public boolean isQuest2Started() { return quest2Started || questStep >= 2; }
    public boolean isQuest2Completed() { return quest2Completed || questStep >= 2; }

    // ค่ากลาง: 0 เริ่มเกม, 1 คุยยีราฟ, 2 ลูบมือ, 3 เก็บเกียร์, 4 ไปห้องสมุด, 5 ปิดแอร์แล้ว (ไปขอพร)
    private int questStep;
    private boolean gearCollected;
    private boolean bookCollected;
    private boolean airOff;

    public QuestManager() {
        // โหลดจากเซฟทันที
        SaveManager.SaveState s = SaveManager.hasSave() ? SaveManager.load() : new SaveManager.SaveState();
        this.questStep = s.questStep;
        this.gearCollected = s.gear;
        this.bookCollected = s.book;
        this.airOff = s.airOff;
    }

    // ---------- สถานะหลัก ----------
    public int getQuestStep() { return questStep; }
    public boolean isGearCollected() { return gearCollected; }
    public boolean isBookCollected() { return bookCollected; }
    public boolean isAirOff() { return airOff; }

    // ---------- อินทิเกรตกับเซฟ ----------
    private void persist() {
        SaveManager.setItems(gearCollected, bookCollected);
        SaveManager.setQuestStep(questStep);
        if (airOff) SaveManager.markAirOff();
    }

    public void updatePlayerPosition(String screenName, float x, float y) {
        SaveManager.setScreen(screenName);
        SaveManager.setPosition(x, y);
    }

    // ---------- เหตุการณ์สำคัญของเกม (เรียกจาก Screens) ----------
    public void onTalkGiraffe() {
        if (questStep < 1) questStep = 1;
        persist();
    }

    public void onTouchHand() {
        if (questStep < 2) questStep = 2;
        persist();
    }

    /** id: "gear" หรือ "book" */
    public void onCollectItem(String id) {
        if ("gear".equalsIgnoreCase(id)) {
            gearCollected = true;
            if (questStep < 3) questStep = 3;
        } else if ("book".equalsIgnoreCase(id)) {
            bookCollected = true;
            // ไม่บังคับ step แต่ถ้าอยาก: Math.max(questStep, 3);
        }
        persist();
    }

    /** เรียกเมื่อเข้าชั้นห้องสมุด (หรือใบ้ให้ไปห้องสมุดแล้ว) */
    public void onGoLibrary() {
        if (questStep < 4) questStep = 4;
        persist();
    }

    /** เรียกเมื่อ "ปิดแอร์ที่ห้องสมุด" สำเร็จ */
    public void onAirOff() {
        airOff = true;
        if (questStep < 5) questStep = 5;
        persist();
    }

    // ---------- เมธอดเดิมเพื่อความเข้ากันได้ (mapping) ----------
//    // Quest1 = ไปคุยยีราฟ, Quest2 = ลูบมือ
//    private boolean quest1Started = false, quest1Completed = false;
//    private boolean quest2Started = false, quest2Completed = false;
//
//    public void startQuest1() { quest1Started = true; onTalkGiraffe(); }
//    public void completeQuest1() { quest1Completed = true; onTalkGiraffe(); }
//
//    public void startQuest2() { quest2Started = true; onTouchHand(); }
//    public void completeQuest2() { quest2Completed = true; onTouchHand(); }
//
//    public boolean isQuest1Started() { return quest1Started || questStep >= 1; }
//    public boolean isQuest1Completed() { return quest1Completed || questStep >= 1; }
//    public boolean isQuest2Started() { return quest2Started || questStep >= 2; }
//    public boolean isQuest2Completed() { return quest2Completed || questStep >= 2; }
}
