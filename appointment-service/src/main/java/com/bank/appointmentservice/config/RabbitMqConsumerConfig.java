package com.bank.appointmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConsumerConfig {

    public static final String MEDICAL_EXCHANGE = "medical.events.exchange";
    public static final String APPOINTMENT_QUEUE = "appointment.medical.events.queue";

    @Bean
    public TopicExchange medicalExchange() {
        return new TopicExchange(MEDICAL_EXCHANGE, true, false);
    }

    @Bean
    public Queue appointmentEventsQueue() {
        // durable = true -> повідомлення зберігаються на диск і не зникнуть, поки сервіс лежить
        return QueueBuilder.durable(APPOINTMENT_QUEUE).build();
    }

    @Bean
    public Binding bindingMedicalEvents(Queue appointmentEventsQueue, TopicExchange medicalExchange) {
        return BindingBuilder.bind(appointmentEventsQueue)
                .to(medicalExchange)
                .with("medical.#");
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}