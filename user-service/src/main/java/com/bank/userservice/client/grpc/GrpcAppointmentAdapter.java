package com.bank.userservice.client.grpc;

import com.bank.grpc.appointment.*;
import com.bank.userservice.client.port.AppointmentCountPort;
import com.bank.userservice.client.port.DoctorAppointmentStatsPort;
import com.bank.userservice.client.port.PatientNextAppointmentPort;
import com.bank.userservice.config.AppointmentIntegrationProperties;
import com.bank.userservice.exception.RemoteResourceNotFoundException;
import com.bank.userservice.exception.RemoteServiceUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.DisposableBean;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class GrpcAppointmentAdapter implements AppointmentCountPort, DoctorAppointmentStatsPort, PatientNextAppointmentPort, DisposableBean {

    private final ManagedChannel channel;
    private final AppointmentQueryServiceGrpc.AppointmentQueryServiceBlockingStub baseStub;
    private final Duration deadline;

    public GrpcAppointmentAdapter(AppointmentIntegrationProperties.GrpcProperties props) {
        this.deadline = props.deadline();
        this.channel = ManagedChannelBuilder
                .forAddress(props.host(), props.port())
                .usePlaintext()
                .build();
        this.baseStub = AppointmentQueryServiceGrpc.newBlockingStub(channel);
    }

    private AppointmentQueryServiceGrpc.AppointmentQueryServiceBlockingStub stubWithDeadline() {
        return baseStub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public long countAppointments() {
        return execute(() -> stubWithDeadline()
                .countAppointments(CountAppointmentsRequest.getDefaultInstance())
                .getTotalCount());
    }

    @Override
    public long getActiveAppointmentsCount(UUID doctorId) {
        return execute(() -> stubWithDeadline()
                .getDoctorStats(DoctorIdRequest.newBuilder().setDoctorId(doctorId.toString()).build())
                .getActiveAppointmentsCount());
    }

    @Override
    public NextAppointmentInfo getNextAppointment(UUID patientId) {
        return execute(() -> {
            NextAppointmentResponse resp = stubWithDeadline()
                    .getNextPatientAppointment(PatientIdRequest.newBuilder().setPatientId(patientId.toString()).build());
            return new NextAppointmentInfo(
                    UUID.fromString(resp.getAppointmentId()),
                    UUID.fromString(resp.getDoctorId()),
                    Instant.parse(resp.getAppointmentTime()),
                    resp.getStatus()
            );
        });
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new RemoteResourceNotFoundException("Ресурс у appointment-service не знайдено (gRPC NOT_FOUND)");
            }
            throw new RemoteServiceUnavailableException(
                    "appointment-service недоступний по gRPC (" + e.getStatus().getCode() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}