package com.trodix.duckcloud.presentation.controllers.websocket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate template;

    @PostMapping("/hello")
    public void greeting(@RequestBody @Valid HelloMessageRequest notification) throws Exception {
        HelloMessage message = new HelloMessage(notification.getNotification());
        template.convertAndSendToUser(notification.getDestinataire(), "/queue/notifications", message);
        //template.convertAndSend("/user/" + "user3" + "/queue/notifications", message);
        //template.convertAndSend("/topic/greetings", message);
    }

    @Data
    public static class HelloMessageRequest {
        @NotEmpty
        private String notification;

        @NotEmpty
        private String destinataire;
    }

    @Data
    @AllArgsConstructor
    public static class HelloMessage {
        @NotEmpty
        private String name;
    }
}
