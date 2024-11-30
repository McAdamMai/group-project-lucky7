package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.business.GateService;
import ca.mcmaster.cas735.acme.gate_system.dtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

import ca.mcmaster.cas735.acme.gate_system.utils.GateSystemUtils;

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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GateService gateService;

    private GateSystemUtils gateSystemUtils;

    private SenderGateSystem senderGateSystem;

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
                String gateNumber = jsonNode.get("gateNumber").asText();

                System.out.println("Received transponder number: " + transponderNumber);
                System.out.println("Received gateNumber: " + gateNumber);

                // Call gate service with extracted variables
                senderGateSystem.sendValidationRequest(Gate2PermitReqDto.builder()
                        .transponderId(Long.parseLong(transponderNumber))
                        .gateId(gateNumber).build());
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
                String gate = jsonNode.get("gate").asText();

                System.out.println("Received qrCode: " + qrCode);
                System.out.println("Received gate: " + gate);

                // Call gate service with extracted variables
                gateService.enterExitParkingLotWithQR(qrCode, gate);
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
                String gate = jsonNode.get("gate").asText();

                System.out.println("Received licensePlate: " + licensePlate);

                // Call gate service to generate QR code
                gateService.enterParkingLotWithoutTransponder(licensePlate, gate);
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

// AMQP listener
    @RabbitListener(queues = "transponder_req.queue")
    public void listenValidTransponder(String message){
        System.out.println(message);
        log.info("receive message from gate_req.queue, {}", message);
        Permit2GateResDto permit2GateResDto = gateSystemUtils.translate(message, Permit2GateResDto.class);
        gateService.enterExitParkingLotWithTransponder(permit2GateResDto.getLicensePlate(), permit2GateResDto.getGateId());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_req.queue" , durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-payment}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*payment2gate"))
    public void listenPaymentConfirmation(String message){
        System.out.println(message);
        log.info("receive message from gate_req.queue, {}", message);
        Payment2GateResDto payment2GateResDto = gateSystemUtils.translate(message, Payment2GateResDto.class);
        gateService.exitingCar(payment2GateResDto);
    }

    @RabbitListener(queues = "charges_req.queue")
    public void listenCharges(String message){
        System.out.println(message);
        log.info("receive message from gate_req.queue, {}", message);
        Enforcement2GateResDto enforcement2GateResDto = gateSystemUtils.translate(message, Enforcement2GateResDto.class);
        gateService.addCharges(enforcement2GateResDto);
    }

}