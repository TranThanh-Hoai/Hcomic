package com.comic.h.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.comic.h.interaction.dto.event.CommentEvent;
import com.comic.h.interaction.dto.event.CommentEventType;
import com.comic.h.interaction.dto.response.CommentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketConfigTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @Test
    @DisplayName("WebSocket STOMP - Connect, Subscribe to topic and receive broadcasted CommentEvent")
    void testWebSocketCommentBroadcast() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        CompletableFuture<CommentEvent> resultKeeper = new CompletableFuture<>();

        StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(10, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();

        session.subscribe("/topic/comics/1/comments", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return CommentEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                resultKeeper.complete((CommentEvent) payload);
            }
        });

        // Wait briefly for broker to register subscription
        Thread.sleep(500);

        CommentResponse commentData = CommentResponse.builder()
                .id(999L)
                .comicId(1L)
                .userId(1L)
                .userDisplayName("TestUser")
                .content("Realtime comment test")
                .build();

        CommentEvent eventToSend = CommentEvent.builder()
                .eventType(CommentEventType.CREATED)
                .comicId(1L)
                .commentId(999L)
                .data(commentData)
                .build();

        messagingTemplate.convertAndSend("/topic/comics/1/comments", eventToSend);

        CommentEvent receivedEvent = resultKeeper.get(10, TimeUnit.SECONDS);

        assertThat(receivedEvent).isNotNull();
        assertThat(receivedEvent.getEventType()).isEqualTo(CommentEventType.CREATED);
        assertThat(receivedEvent.getComicId()).isEqualTo(1L);
        assertThat(receivedEvent.getData().getContent()).isEqualTo("Realtime comment test");

        session.disconnect();
    }
}
