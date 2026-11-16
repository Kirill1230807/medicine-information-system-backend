package com.bank.appointmentservice.grpc;

import com.bank.appointmentservice.service.AppointmentQueryDomainService;
import com.bank.grpc.appointment.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "grpc.server", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AppointmentGrpcServiceImpl extends AppointmentQueryServiceGrpc.AppointmentQueryServiceImplBase {

    private final AppointmentQueryDomainService queryService;

    @Override
    public void countAppointments(CountAppointmentsRequest request,
                                  StreamObserver<CountAppointmentsResponse> responseObserver) {
        long count = queryService.countAll();
        responseObserver.onNext(CountAppointmentsResponse.newBuilder().setTotalCount(count).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDoctorStats(DoctorIdRequest request,
                               StreamObserver<DoctorAppointmentsResponse> responseObserver) {
        UUID doctorId = parseUuid(request.getDoctorId(), responseObserver);
        if (doctorId == null) return;

        queryService.findDoctorStats(doctorId).ifPresentOrElse(
                stats -> {
                    responseObserver.onNext(DoctorAppointmentsResponse.newBuilder()
                            .setDoctorId(doctorId.toString())
                            .setActiveAppointmentsCount(stats.activeAppointmentsCount())
                            .build());
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Дані по лікарю " + doctorId + " відсутні")
                        .asRuntimeException())
        );
    }

    @Override
    public void getNextPatientAppointment(PatientIdRequest request,
                                          StreamObserver<NextAppointmentResponse> responseObserver) {
        UUID patientId = parseUuid(request.getPatientId(), responseObserver);
        if (patientId == null) return;

        queryService.findNextPatientAppointment(patientId).ifPresentOrElse(
                next -> {
                    responseObserver.onNext(NextAppointmentResponse.newBuilder()
                            .setAppointmentId(next.appointmentId().toString())
                            .setDoctorId(next.doctorId().toString())
                            .setAppointmentTime(next.appointmentTime().toString())
                            .setStatus(next.status())
                            .build());
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Найближчий запис для пацієнта " + patientId + " не знайдено")
                        .asRuntimeException())
        );
    }

    private UUID parseUuid(String raw, StreamObserver<?> observer) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription("Невалідний UUID: " + raw).asRuntimeException());
            return null;
        }
    }
}