package info.jtrac.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ItemWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public ItemWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyItemUpdate(Long itemId) {
        messagingTemplate.convertAndSend("/topic/item/" + itemId, "updated");
    }
}
