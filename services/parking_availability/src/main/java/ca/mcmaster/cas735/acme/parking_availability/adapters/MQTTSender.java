package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.business.AvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.integration.mqtt.support.MqttHeaders;

@Service
@Slf4j
@RequiredArgsConstructor
public class MQTTSender {

    @Value("${app.custom.mqtt.host}") private String host;
    @Value("${app.custom.mqtt.port}") private Integer port;
    @Value("${app.custom.mqtt.monitorTopic}") private String topic;
    @Value("${app.custom.mqtt.clientId}") private String clientId;
    private final String url = "tcp://" + host + ":" + port;

    private final MqttPahoMessageHandler mqttOutbound;
    private final ObjectMapper objectMapper;
    private final AvailabilityService availabilityService;

    @Scheduled(fixedRate = 5000) //send a msg every 5s
    public void sendPeriodMessage() {
        String topic = this.topic;
        String payload = availabilityService.monitor();
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, 1) // Quality of Service
                .build();
        mqttOutbound.handleMessage(message);
        log.info("Send period message at " + payload);
    }
}
