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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        var professional = professional(1L);
        var serviceOffering = serviceOffering(1L, 30);
        var availability = availability(professional, startsAt, LocalTime.of(9, 0), LocalTime.of(18, 0));

        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceOffering));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(1L, startsAt.getDayOfWeek()))
                .thenReturn(List.of(availability));
        when(appointmentRepository.existsConflict(1L, startsAt, endsAt, AppointmentStatus.SCHEDULED))
                .thenReturn(true);

        assertThatThrownBy(() -> service.schedule(
                new AppointmentRequest(1L, 1L, startsAt),
                new AuthenticatedUser(user(UserRole.CLIENT))
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Profissional ja possui agendamento neste horario");

        verify(appointmentRepository, never()).save(any());
        verify(appointmentRepository).existsConflict(eq(1L), eq(startsAt), eq(endsAt), eq(AppointmentStatus.SCHEDULED));
    }

    @Test
    void shouldRejectAppointmentWhenAuthenticatedUserIsNotClient() {
        var startsAt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        assertThatThrownBy(() -> service.schedule(
                new AppointmentRequest(1L, 1L, startsAt),
                new AuthenticatedUser(user(UserRole.ADMIN))
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Apenas usuarios com perfil CLIENT podem criar agendamentos");

        verifyNoInteractions(professionalRepository, serviceRepository, availabilityRepository, appointmentRepository);
    }

    @Test
    void shouldRejectAppointmentInThePast() {
        var startsAt = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        assertThatThrownBy(() -> service.schedule(
                new AppointmentRequest(1L, 1L, startsAt),
                new AuthenticatedUser(user(UserRole.CLIENT))
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Agendamento deve ser feito para uma data futura");

        verifyNoInteractions(professionalRepository, serviceRepository, availabilityRepository, appointmentRepository);
    }

    @Test
    void shouldRejectAppointmentOutsideProfessionalAvailability() {
        var startsAt = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        var professional = professional(1L);
        var serviceOffering = serviceOffering(1L, 30);
        var availability = availability(professional, startsAt, LocalTime.of(9, 0), LocalTime.of(18, 0));

        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceOffering));
        when(availabilityRepository.findByProfessionalIdAndDayOfWeek(1L, startsAt.getDayOfWeek()))
                .thenReturn(List.of(availability));

        assertThatThrownBy(() -> service.schedule(
                new AppointmentRequest(1L, 1L, startsAt),
                new AuthenticatedUser(user(UserRole.CLIENT))
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Horario fora da disponibilidade do profissional");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldListClientAppointmentsFilteredByDateRange() {
        var startDate = LocalDate.of(2026, 7, 1);
        var endDate = LocalDate.of(2026, 7, 31);
        var client = user(UserRole.CLIENT);
        ReflectionTestUtils.setField(client, "id", 10L);

        when(appointmentRepository.findByClientIdAndStartsAtBetweenOrderByStartsAtDesc(
                10L,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        )).thenReturn(List.of());

        var response = service.listMine(new AuthenticatedUser(client), startDate, endDate);

        assertThat(response).isEmpty();
        verify(appointmentRepository).findByClientIdAndStartsAtBetweenOrderByStartsAtDesc(
                10L,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );
    }

    @Test
    void shouldRejectDateFilterWhenOnlyStartDateIsProvided() {
        assertThatThrownBy(() -> service.listMine(
                new AuthenticatedUser(user(UserRole.CLIENT)),
                LocalDate.of(2026, 7, 1),
                null
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Informe startDate e endDate juntos");
    }

    @Test
    void shouldRejectDateFilterWhenStartDateIsAfterEndDate() {
        assertThatThrownBy(() -> service.listMine(
                new AuthenticatedUser(user(UserRole.CLIENT)),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 7, 1)
        )).isInstanceOf(BusinessException.class)
                .hasMessage("startDate deve ser anterior ou igual a endDate");
    }

    private User user(UserRole role) {
        var user = new User();
        user.setName(role.name());
        user.setEmail(role.name().toLowerCase() + "@email.com");
        user.setPassword("secret");
        user.setRole(role);
        return user;
    }

    private Professional professional(Long id) {
        var professional = new Professional();
        ReflectionTestUtils.setField(professional, "id", id);
        professional.setUser(user(UserRole.PROFESSIONAL));
        professional.setSpecialty("Clinico geral");
        return professional;
    }

    private ServiceOffering serviceOffering(Long id, int durationMinutes) {
        var serviceOffering = new ServiceOffering();
        ReflectionTestUtils.setField(serviceOffering, "id", id);
        serviceOffering.setName("Consulta");
        serviceOffering.setDurationMinutes(durationMinutes);
        serviceOffering.setPrice(BigDecimal.valueOf(100));
        return serviceOffering;
    }

    private Availability availability(
            Professional professional,
            LocalDateTime date,
            LocalTime startsAt,
            LocalTime endsAt
    ) {
        var availability = new Availability();
        availability.setProfessional(professional);
        availability.setDayOfWeek(date.getDayOfWeek());
        availability.setStartsAt(startsAt);
        availability.setEndsAt(endsAt);
        return availability;
    }
}
