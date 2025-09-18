package info.jtrac.ui;

import info.jtrac.domain.History;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryMapper {

    public HistoryDto toDto(History history) {
        HistoryDto dto = new HistoryDto();
        dto.setId(history.getId());
        dto.setComment(history.getComment());
        dto.setTimeStamp(history.getTimeStamp()); // Предполагается, что в History есть поле timeStamp

        // Маппинг информации о пользователе
        if (history.getLoggedBy() != null) {
            HistoryDto.UserInfoDto userInfo = new HistoryDto.UserInfoDto();
            userInfo.setId(history.getLoggedBy().getId());
            userInfo.setName(history.getLoggedBy().getName()); // Предполагается, что у User есть метод getName()
            dto.setLoggedBy(userInfo);
        }

        // Маппинг деталей изменения (если они есть)
        if (history.getChange() != null) { // Предполагается, что в History есть поле change типа History.Change
            HistoryDto.ChangeDetail changeDetail = new HistoryDto.ChangeDetail();
            changeDetail.setFieldName(history.getChange().getFieldName());
            changeDetail.setValueBefore(history.getChange().getValueBefore());
            changeDetail.setValueAfter(history.getChange().getValueAfter());
            dto.setChange(changeDetail);
        }

        return dto;
    }

    // Метод для преобразования списка History в список HistoryDto
    public List<HistoryDto> toDtoList(List<History> historyList) {
        return historyList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}