package timeescape.model;

/** 게임에 존재하는 세 시간대. */
public enum Era {
    PAST("과거", "past.png", "낡은 시계공 연구실"),
    PRESENT("현재", "present.png", "산업형 전자 연구실"),
    FUTURE("미래", "future.png", "봉쇄된 시간 연구실");

    private final String title;
    private final String imageName;
    private final String description;

    Era(String title, String imageName, String description) {
        this.title = title;
        this.imageName = imageName;
        this.description = description;
    }

    public String title() { return title; }
    public String imageName() { return imageName; }
    public String description() { return description; }
}
