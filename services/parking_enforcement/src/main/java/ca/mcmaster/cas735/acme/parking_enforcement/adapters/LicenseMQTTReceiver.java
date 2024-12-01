package ca.mcmaster.cas735.acme.parking_enforcement.adapters;

import ca.mcmaster.cas735.acme.parking_enforcement.business.EnforcementSystemService;
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

@Service
public class LicenseMQTTReceiver {

    @Value("${app.custom.mqtt.host}")   private String host;
    @Value("${app.custom.mqtt.port}")   private Integer port;
    @Value("${app.custom.mqtt.topic}")  private String topic;

    @Autowired
    private EnforcementSystemService enforcementSystemService;

    @Bean @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String licenseNumber = (String) message.getPayload();
            System.out.println("Received license number: " + licenseNumber);
            enforcementSystemService.sendFine(licenseNumber);
        };
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound(@Qualifier("mqttInputChannel") @Lazy MessageChannel channel) {
        String url = "tcp://" + host + ":" + port;
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(url, "parking_receiver", topic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(channel);
        return adapter;
    }
}
