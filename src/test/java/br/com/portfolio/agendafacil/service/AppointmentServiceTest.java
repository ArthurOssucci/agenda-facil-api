package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.AppointmentStatus;
import br.com.portfolio.agendafacil.domain.Availability;
import br.com.portfolio.agendafacil.domain.Professional;
import br.com.portfolio.agendafacil.domain.ServiceOffering;
import br.com.portfolio.agendafacil.domain.User;
import br.com.portfolio.agendafacil.domain.UserRole;
import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentRequest;
import br.com.portfolio.agendafacil.exception.BusinessException;
import br.com.portfolio.agendafacil.repository.AppointmentRepository;
import br.com.portfolio.agendafacil.repository.AvailabilityRepository;
import br.com.portfolio.agendafacil.repository.ProfessionalRepository;
import br.com.portfolio.agendafacil.repository.ServiceOfferingRepository;
import br.com.portfolio.agendafacil.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceTest {

    private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
    private final ProfessionalRepository professionalRepository = mock(ProfessionalRepository.class);
    private final ServiceOfferingRepository serviceRepository = mock(ServiceOfferingRepository.class);
    private final AvailabilityRepository availabilityRepository = mock(AvailabilityRepository.class);
    private final AppointmentService service = new AppointmentService(
            appointmentRepository,
            professionalRepository,
            serviceRepository,
            availabilityRepository,
            new Mapper()
    );

    @Test
    void shouldRejectAppointmentWhenProfessionalAlreadyHasScheduledAppointment() {
        var startsAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        var endsAt = startsAt.plusMinutes(30);
        var professional = new Professional();
        ReflectionTestUtils.setField(professional, "id", 1L);
        var serviceOffering = new ServiceOffering();
        ReflectionTestUtils.setField(serviceOffering, "id", 1L);
        serviceOffering.setName("Consulta");
        serviceOffering.setDurationMinutes(30);
        serviceOffering.setPrice(BigDecimal.valueOf(100));

        var availability = new Availability();
        availability.setProfessional(professional);
        availability.setDayOfWeek(startsAt.getDayOfWeek());
        availability.setStartsAt(LocalTime.of(9, 0));
        availability.setEndsAt(LocalTime.of(18, 0));

        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceOffering));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(1L, startsAt.getDayOfWeek()))
                .thenReturn(List.of(availability));
        when(appointmentRepository.existsConflict(1L, startsAt, endsAt, AppointmentStatus.SCHEDULED))
                .thenReturn(true);

        var client = new User();
        client.setName("Cliente");
        client.setEmail("cliente@email.com");
        client.setPassword("secret");
        client.setRole(UserRole.CLIENT);

        assertThatThrownBy(() -> service.schedule(
                new AppointmentRequest(1L, 1L, startsAt),
                new AuthenticatedUser(client)
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Profissional ja possui agendamento neste horario");

        verify(appointmentRepository, never()).save(any());
        verify(appointmentRepository).existsConflict(eq(1L), eq(startsAt), eq(endsAt), eq(AppointmentStatus.SCHEDULED));
    }
}
