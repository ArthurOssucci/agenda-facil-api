package br.com.portfolio.agendafacil.dto;

import br.com.portfolio.agendafacil.domain.AppointmentStatus;
import br.com.portfolio.agendafacil.domain.UserRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record UserResponse(Long id, String name, String email, UserRole role) {
    }

    public record ServiceRequest(
            @NotBlank String name,
            String description,
            @NotNull @Min(15) Integer durationMinutes,
            @NotNull @DecimalMin("0.00") BigDecimal price
    ) {
    }

    public record ServiceResponse(Long id, String name, String description, Integer durationMinutes, BigDecimal price) {
    }

    public record ProfessionalRequest(@NotNull Long userId, @NotBlank String specialty) {
    }

    public record ProfessionalResponse(Long id, UserResponse user, String specialty) {
    }

    public record AvailabilityRequest(
            @NotNull Long professionalId,
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime startsAt,
            @NotNull LocalTime endsAt
    ) {
    }

    public record AvailabilityResponse(Long id, Long professionalId, DayOfWeek dayOfWeek, LocalTime startsAt, LocalTime endsAt) {
    }

    public record AppointmentRequest(
            @NotNull Long professionalId,
            @NotNull Long serviceId,
            @NotNull @Future LocalDateTime startsAt
    ) {
    }

    public record AppointmentResponse(
            Long id,
            UserResponse client,
            ProfessionalResponse professional,
            ServiceResponse service,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            AppointmentStatus status
    ) {
    }
}
