package si.pricescout.stores.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
import si.pricescout.stores.config.RabbitMqProperties;

@Component
public class PriceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PriceEventPublisher.class);

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final RabbitMqProperties rabbitMqProperties;

    public PriceEventPublisher(Sender sender, ObjectMapper objectMapper, RabbitMqProperties rabbitMqProperties) {
        this.sender = sender;
        this.objectMapper = objectMapper;
        this.rabbitMqProperties = rabbitMqProperties;
    }

    public Mono<Void> publishPriceChanged(PriceChangedEvent event) {
        return Mono.fromCallable(() -> new OutboundMessage(
                        rabbitMqProperties.getExchange(),
                        rabbitMqProperties.getRoutingKey(),
                        objectMapper.writeValueAsBytes(event)
                ))
                .flatMap(message -> sender.sendWithPublishConfirms(Mono.just(message))
                        .single()
                        .flatMap(result -> {
                            if (!result.isAck()) {
                                return Mono.error(new IllegalStateException("RabbitMQ did not ACK message"));
                            }
                            log.info("Published price event type={} productId={} storeId={} value={}",
                                    event.eventType(), event.productId(), event.storeId(), event.value());
                            return Mono.<Void>empty();
                        }))
                .onErrorMap(JsonProcessingException.class, ex -> new IllegalStateException("Failed to serialize event", ex));
    }
}
