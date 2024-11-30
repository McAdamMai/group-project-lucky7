package ca.mcmaster.cas735.acme.gate_system.adaptors;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Define queues
    public static final String VALIDATION_REQUEST_QUEUE = "validationRequestQueue";
    public static final String VALIDATION_RESPONSE_QUEUE = "validationResponseQueue";
    public static final String PAYMENT_REQUEST_QUEUE = "paymentRequestQueue";
    public static final String AVAILABILITY_REQUEST_QUEUE = "availabilityRequestQueue";
    public static final String QRCODE_REQUEST_QUEUE = "QRCodeRequestQueue";

    @Bean
    public Queue validationRequestQueue() {
        return new Queue(VALIDATION_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue validationResponseQueue() {
        return new Queue(VALIDATION_RESPONSE_QUEUE, false);
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(PAYMENT_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue QRcodeRequest() {
        return new Queue(QRCODE_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue availabilityRequestQueue() {
        return new Queue(AVAILABILITY_REQUEST_QUEUE, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("parkingExchange");
    }

    @Bean
    public Binding bindingValidationRequest(Queue validationRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(validationRequestQueue).to(exchange).with("validationRequestKey");
    }

    @Bean
    public Binding bindingValidationResponse(Queue validationResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(validationResponseQueue).to(exchange).with("validationResponseKey");
    }

    @Bean
    public Binding bindingPaymentRequest(Queue paymentRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(paymentRequestQueue).to(exchange).with("paymentRequestKey");
    }

    @Bean
    public Binding bindingQRCodeRequest(Queue QRcodeRequest, DirectExchange exchange) {
        return BindingBuilder.bind(QRcodeRequest).to(exchange).with("QRCodeRequestKey");
    }

    @Bean
    public Binding bindingAvailabilityRequest(Queue availabilityRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(availabilityRequestQueue).to(exchange).with("availabilityRequestKey");
    }
}
