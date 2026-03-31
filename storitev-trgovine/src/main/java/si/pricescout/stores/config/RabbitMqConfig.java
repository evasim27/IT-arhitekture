package si.pricescout.stores.config;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.ApplicationRunner;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;
import reactor.rabbitmq.QueueSpecification;

@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);

    @Bean
    ConnectionFactory reactorConnectionFactory(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.determineHost());
        connectionFactory.setPort(rabbitProperties.determinePort());
        connectionFactory.setUsername(rabbitProperties.determineUsername());
        connectionFactory.setPassword(rabbitProperties.determinePassword());
        String virtualHost = rabbitProperties.determineVirtualHost();
        connectionFactory.setVirtualHost(virtualHost == null || virtualHost.isBlank() ? "/" : virtualHost);
        return connectionFactory;
    }

    @Bean
    Sender sender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(new SenderOptions().connectionFactory(connectionFactory));
    }

    @Bean
    ApplicationRunner rabbitTopologyInitializer(Sender sender, RabbitMqProperties properties) {
        return args -> sender.declareExchange(ExchangeSpecification.exchange(properties.getExchange()).type("direct").durable(true))
                .then(sender.declareQueue(QueueSpecification.queue(properties.getQueue()).durable(true)))
                .then(sender.bind(BindingSpecification.binding(properties.getExchange(), properties.getRoutingKey(), properties.getQueue())))
                .doOnSuccess(v -> log.info("RabbitMQ topology initialized for exchange={} queue={} routingKey={}",
                        properties.getExchange(), properties.getQueue(), properties.getRoutingKey()))
                .doOnError(ex -> log.error("RabbitMQ topology initialization failed", ex))
                .subscribe();
    }
}