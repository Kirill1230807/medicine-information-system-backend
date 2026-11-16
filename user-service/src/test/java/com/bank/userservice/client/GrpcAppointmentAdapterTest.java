package com.bank.userservice.client;

import com.bank.grpc.appointment.*;
import com.bank.userservice.client.grpc.GrpcAppointmentAdapter;
import com.bank.userservice.config.AppointmentIntegrationProperties.GrpcProperties;
import com.bank.userservice.exception.RemoteResourceNotFoundException;
import com.bank.userservice.exception.RemoteServiceUnavailableException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GrpcAppointmentAdapterTest {

    private Server server;
    private GrpcAppointmentAdapter adapter;

    private void startTestServer(AppointmentQueryServiceGrpc.AppointmentQueryServiceImplBase serviceImpl, Duration deadline) throws Exception {
        server = ServerBuilder.forPort(0)
                .addService(serviceImpl)
                .build()
                .start();

        adapter = new GrpcAppointmentAdapter(new GrpcProperties("localhost", server.getPort(), deadline));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (adapter != null) adapter.destroy();
        if (server != null) server.shutdownNow();
    }

    @Test
    void countAppointments_returnsTotalCount() throws Exception {
        startTestServer(new AppointmentQueryServiceGrpc.AppointmentQueryServiceImplBase() {
            @Override
            public void countAppointments(CountAppointmentsRequest request, StreamObserver<CountAppointmentsResponse> responseObserver) {
                responseObserver.onNext(CountAppointmentsResponse.newBuilder().setTotalCount(42L).build());
                responseObserver.onCompleted();
            }
        }, Duration.ofSeconds(2));

        assertEquals(42L, adapter.countAppointments());
    }

    @Test
    void getDoctorStats_whenNotFound_throwsRemoteResourceNotFoundException() throws Exception {
        startTestServer(new AppointmentQueryServiceGrpc.AppointmentQueryServiceImplBase() {
            @Override
            public void getDoctorStats(DoctorIdRequest request, StreamObserver<DoctorAppointmentsResponse> responseObserver) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Not found").asRuntimeException());
            }
        }, Duration.ofSeconds(2));

        UUID doctorId = UUID.randomUUID();
        assertThrows(RemoteResourceNotFoundException.class, () -> adapter.getActiveAppointmentsCount(doctorId));
    }

    @Test
    void countAppointments_whenDeadlineExceeded_throwsRemoteServiceUnavailableException() throws Exception {
        startTestServer(new AppointmentQueryServiceGrpc.AppointmentQueryServiceImplBase() {
            @Override
            public void countAppointments(CountAppointmentsRequest request, StreamObserver<CountAppointmentsResponse> responseObserver) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                responseObserver.onNext(CountAppointmentsResponse.newBuilder().setTotalCount(1L).build());
                responseObserver.onCompleted();
            }
        }, Duration.ofMillis(50));

        assertThrows(RemoteServiceUnavailableException.class, () -> adapter.countAppointments());
    }
}