package com.bank.appointmentservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "grpc.server", name = "enabled", havingValue = "true")
public class GrpcServerLifecycle implements SmartLifecycle {

    private final int port;
    private final AppointmentGrpcServiceImpl grpcService;
    private Server server;
    private volatile boolean running = false;

    public GrpcServerLifecycle(@Value("${grpc.server.port:9093}") int port,
                               AppointmentGrpcServiceImpl grpcService) {
        this.port = port;
        this.grpcService = grpcService;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(grpcService)
                    .build()
                    .start();
            running = true;
            log.info("gRPC server started on port {}", port);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося запустити gRPC сервер на порту " + port, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            log.info("Shutting down gRPC server...");
            server.shutdown();
            try {
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}