package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.Appointment;
import br.com.portfolio.agendafacil.domain.Availability;
import br.com.portfolio.agendafacil.domain.Professional;
import br.com.portfolio.agendafacil.domain.ServiceOffering;
import br.com.portfolio.agendafacil.domain.User;
import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.AvailabilityResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.ProfessionalResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.ServiceResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public ServiceResponse toServiceResponse(ServiceOffering service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice()
        );
    }

    public ProfessionalResponse toProfessionalResponse(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                toUserResponse(professional.getUser()),
                professional.getSpecialty()
        );
    }

    public AvailabilityResponse toAvailabilityResponse(Availability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getProfessional().getId(),
                availability.getDayOfWeek(),
                availability.getStartsAt(),
                availability.getEndsAt()
        );
    }

    public AppointmentResponse toAppointmentResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                toUserResponse(appointment.getClient()),
                toProfessionalResponse(appointment.getProfessional()),
                toServiceResponse(appointment.getService()),
                appointment.getStartsAt(),
                appointment.getEndsAt(),
                appointment.getStatus()
        );
    }
}
