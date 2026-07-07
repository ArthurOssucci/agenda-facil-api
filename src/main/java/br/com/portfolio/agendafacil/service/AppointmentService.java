package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.Appointment;
import br.com.portfolio.agendafacil.domain.AppointmentStatus;
import br.com.portfolio.agendafacil.domain.UserRole;
import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentResponse;
import br.com.portfolio.agendafacil.exception.BusinessException;
import br.com.portfolio.agendafacil.exception.NotFoundException;
import br.com.portfolio.agendafacil.repository.AppointmentRepository;
import br.com.portfolio.agendafacil.repository.AvailabilityRepository;
import br.com.portfolio.agendafacil.repository.ProfessionalRepository;
import br.com.portfolio.agendafacil.repository.ServiceOfferingRepository;
import br.com.portfolio.agendafacil.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final Mapper mapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ProfessionalRepository professionalRepository,
            ServiceOfferingRepository serviceRepository,
            AvailabilityRepository availabilityRepository,
            Mapper mapper
    ) {
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.mapper = mapper;
    }

    @Transactional
    public AppointmentResponse schedule(AppointmentRequest request, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.role() != UserRole.CLIENT) {
            throw new BusinessException("Apenas usuarios com perfil CLIENT podem criar agendamentos");
        }

        if (!request.startsAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Agendamento deve ser feito para uma data futura");
        }

        var professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new NotFoundException("Profissional nao encontrado"));
        var service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Servico nao encontrado"));

        var endsAt = request.startsAt().plusMinutes(service.getDurationMinutes());
        ensureInsideAvailability(professional.getId(), request.startsAt(), endsAt);

        var hasConflict = appointmentRepository.existsConflict(
                professional.getId(),
                request.startsAt(),
                endsAt,
                AppointmentStatus.SCHEDULED
        );
        if (hasConflict) {
            throw new BusinessException("Profissional ja possui agendamento neste horario");
        }

        var appointment = new Appointment();
        appointment.setClient(authenticatedUser.user());
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setStartsAt(request.startsAt());
        appointment.setEndsAt(endsAt);
        return mapper.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMine(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.role() == UserRole.PROFESSIONAL) {
            return professionalRepository.findByUserId(authenticatedUser.id())
                    .map(professional -> appointmentRepository.findByProfessionalIdOrderByStartsAtDesc(professional.getId()))
                    .orElse(List.of())
                    .stream()
                    .map(mapper::toAppointmentResponse)
                    .toList();
        }

        return appointmentRepository.findByClientIdOrderByStartsAtDesc(authenticatedUser.id()).stream()
                .map(mapper::toAppointmentResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancel(Long appointmentId, AuthenticatedUser authenticatedUser) {
        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento nao encontrado"));

        var isOwner = appointment.getClient().getId().equals(authenticatedUser.id());
        var isProfessional = appointment.getProfessional().getUser().getId().equals(authenticatedUser.id());
        var isAdmin = authenticatedUser.role() == UserRole.ADMIN;
        if (!isOwner && !isProfessional && !isAdmin) {
            throw new BusinessException("Usuario nao pode cancelar este agendamento");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return mapper.toAppointmentResponse(appointment);
    }

    private void ensureInsideAvailability(Long professionalId, LocalDateTime startsAt, LocalDateTime endsAt) {
        var availabilities = availabilityRepository.findByProfessionalIdAndDayOfWeek(professionalId, startsAt.getDayOfWeek());
        var insideAvailability = availabilities.stream().anyMatch(availability ->
                !startsAt.toLocalTime().isBefore(availability.getStartsAt())
                        && !endsAt.toLocalTime().isAfter(availability.getEndsAt())
        );

        if (!insideAvailability) {
            throw new BusinessException("Horario fora da disponibilidade do profissional");
        }
    }
}
