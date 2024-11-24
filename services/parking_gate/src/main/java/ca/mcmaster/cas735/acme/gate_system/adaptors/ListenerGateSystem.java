package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.business.GateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@Slf4j
public class ListenerGateSystem {

    @Value("${app.custom.mqtt.host}")
    private String host;
    @Value("${app.custom.mqtt.port}")
    private Integer port;
    @Value("${app.custom.mqtt.transponderTopic}")
    private String transponderTopic;
    @Value("${app.custom.mqtt.qrTopic}")
    private String qrTopic;
    @Value("${app.custom.mqtt.buttonClickTopic}")
    private String buttonClickTopic;

    @Autowired
    private GateService gateService;

    @Bean
    @ServiceActivator(inputChannel = "mqttTransponderInputChannel")
    public MessageHandler transponderHandler() {
        return message -> {
            try {
                // Parse JSON payload
                String payload = (String) message.getPayload();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);

                // Extract variables
                String transponderNumber = jsonNode.get("transponderNumber").asText();
                String isExit = jsonNode.get("isExit").asText();

                System.out.println("Received transponder number: " + transponderNumber);
                System.out.println("Received isExit: " + isExit);

                // Call gate service with extracted variables
                gateService.enterExitParkingLotWithTransponder(new BigInteger(transponderNumber), isExit.equals("true"));
            } catch (Exception e) {
                System.err.println("Error parsing MQTT message payload: " + e.getMessage());
            }
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttQrInputChannel")
    public MessageHandler qrHandler() {
        return message -> {
            try {
                // Parse JSON payload
                String payload = (String) message.getPayload();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);

                // Extract variables
                Long qrCode = Long.parseLong(jsonNode.get("qrCode").asText());
                String isExit = jsonNode.get("isExit").asText();

                System.out.println("Received qrCode: " + qrCode);
                System.out.println("Received isExit: " + isExit);

                // Call gate service with extracted variables
                gateService.exitParkingLotWithQR(qrCode);
            } catch (Exception e) {
                System.err.println("Error parsing MQTT message payload: " + e.getMessage());
            }
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttButtonClickInputChannel")
    public MessageHandler buttonClickHandler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);

                String licensePlate = jsonNode.get("licensePlate").asText();

                System.out.println("Received licensePlate: " + licensePlate);

                // Call gate service to generate QR code
                gateService.enterParkingLotWithoutTransponder(licensePlate);
            } catch (Exception e) {
                System.err.println("Error processing button click event: " + e.getMessage());
            }
        };
    }

    @Bean
    public MessageChannel mqttTransponderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttQrInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttButtonClickInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer transponderInbound(@Qualifier("mqttTransponderInputChannel") @Lazy MessageChannel channel) {
        String url = "tcp://" + host + ":" + port;
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(url, "transponder_receiver", transponderTopic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(channel);
        return adapter;
    }

    @Bean
    public MessageProducer qrInbound(@Qualifier("mqttQrInputChannel") @Lazy MessageChannel channel) {
        String url = "tcp://" + host + ":" + port;
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(url, "qr_receiver", qrTopic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(channel);
        return adapter;
    }

    @Bean
    public MessageProducer buttonClickInbound(@Qualifier("mqttButtonClickInputChannel") @Lazy MessageChannel channel) {
        String url = "tcp://" + host + ":" + port;
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(url, "button_click_receiver", buttonClickTopic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(channel);
        return adapter;
    }
}