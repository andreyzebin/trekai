package info.jtrac.ui;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class HistoryDto {
    private Long id;
    private ChangeDetail change; // Вложенный DTO для изменения
    private String comment; // Текст комментария
    private UserInfoDto loggedBy; // DTO с информацией о пользователе
    private Date timeStamp; // Время события

    // Вложенный static класс для деталей изменения поля
    @Data
    public static class ChangeDetail {
        private String fieldName; // Название измененного поля
        private String valueBefore; // Значение до изменения
        private String valueAfter; // Значение после изменения
    }

    // Вложенный static класс для информации о пользователе
    @Data
    public static class UserInfoDto {
        private Long id;
        private String name; // Имя пользователя (логин или полное имя)
        // Можно добавить другие поля при необходимости, например, email
    }
}