package ai.lab.inlive.entities.enums;

// todo: отрефакторить все использования toString(), исползовагние tOString как значение очень плохо лучше геттеры
public enum TokenType {
    ACCESS_TOKEN, REFRESH_TOKEN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}