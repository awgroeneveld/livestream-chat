package com.distributedchat.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@Slf4j
public class RedisPubSubService implements PubSubService {

    private final ChannelTopic channelTopic = new ChannelTopic("broadcast");
    private final ReactiveRedisTemplate<String, Message> reactiveTemplate;
    private final ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    public RedisPubSubService(ReactiveRedisTemplate<String, Message> reactiveTemplate,
                              ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer) {
        this.reactiveMsgListenerContainer = reactiveMsgListenerContainer;
        this.reactiveTemplate = reactiveTemplate;

    }

    @Override
    public Mono<Void> publish(Message message) {
        return this.reactiveTemplate
                .convertAndSend(channelTopic.getTopic(), message)
                .then(Mono.empty());
    }

    @Override
    public Flux<Message> subscribe() {
        return reactiveMsgListenerContainer
                .receive(Collections.singletonList(channelTopic),
                        reactiveTemplate.getSerializationContext().getKeySerializationPair(),
                        reactiveTemplate.getSerializationContext().getValueSerializationPair())
                .map(ReactiveSubscription.Message::getMessage);
    }
}