package io.disposechat.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;

@Service
@Slf4j
public class RedisMessagingService implements MessagingService {

    private final ChannelTopic channelTopic = new ChannelTopic("broadcast");
    private final ReactiveRedisTemplate<String, Message> reactiveTemplate;
    private final ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    private final RedisSerializationContext.SerializationPair<String> redisKeyType;
    private final RedisSerializationContext.SerializationPair<Message> redisValueType;

    public RedisMessagingService(ReactiveRedisTemplate<String, Message> reactiveTemplate,
                                 ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer) {
        this.reactiveMsgListenerContainer = reactiveMsgListenerContainer;
        this.reactiveTemplate = reactiveTemplate;
        this.redisKeyType = reactiveTemplate.getSerializationContext().getKeySerializationPair();
        this.redisValueType = reactiveTemplate.getSerializationContext().getValueSerializationPair();
    }

    @Override
    public void send(Message message) {
        this.reactiveTemplate.convertAndSend(channelTopic.getTopic(), message).subscribe();
    }

    @Override
    public Flux<Message> receive() {
        return reactiveMsgListenerContainer
                .receive(Collections.singletonList(channelTopic), redisKeyType, redisValueType)
                .map(ReactiveSubscription.Message::getMessage);
    }
}