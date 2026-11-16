package com.bank.userservice.config;

import com.bank.userservice.client.BearerTokenInterceptor;
import com.bank.userservice.client.grpc.GrpcAppointmentAdapter;
import com.bank.userservice.client.http.AppointmentHttpExchangeClient;
import com.bank.userservice.client.http.HttpAppointmentAdapter;
import com.bank.userservice.client.port.AppointmentCountPort;
import com.bank.userservice.client.port.DoctorAppointmentStatsPort;
import com.bank.userservice.client.port.PatientNextAppointmentPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;

@Configuration
public class AppointmentClientConfig {

    @Bean
    public AppointmentCountPort appointmentCountPort(AppointmentIntegrationProperties props) {
        return createAdapter(props);
    }

    @Bean
    public DoctorAppointmentStatsPort doctorAppointmentStatsPort(AppointmentIntegrationProperties props) {
        return createAdapter(props);
    }

    @Bean
    public PatientNextAppointmentPort patientNextAppointmentPort(AppointmentIntegrationProperties props) {
        return createAdapter(props);
    }

    @SuppressWarnings("unchecked")
    private <T> T createAdapter(AppointmentIntegrationProperties props) {
        String transport = props.transport().trim().toLowerCase();

        Object adapter = switch (transport) {
            case "http" -> buildHttpAdapter(props.http());
            case "grpc" -> new GrpcAppointmentAdapter(props.grpc());
            default -> throw new IllegalStateException(
                    "Непідтримуваний тип транспорту для appointment-service: " + props.transport());
        };
        return (T) adapter;
    }

    private HttpAppointmentAdapter buildHttpAdapter(AppointmentIntegrationProperties.HttpProperties httpProps) {
        HttpClient jdkClient = HttpClient.newBuilder()
                .connectTimeout(httpProps.connectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkClient);
        requestFactory.setReadTimeout(httpProps.readTimeout());

        RestClient restClient = RestClient.builder()
                .baseUrl(httpProps.url())
                .requestFactory(requestFactory)
                .requestInterceptor(new BearerTokenInterceptor())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        AppointmentHttpExchangeClient declarativeClient = factory.createClient(AppointmentHttpExchangeClient.class);
        return new HttpAppointmentAdapter(declarativeClient);
    }
}