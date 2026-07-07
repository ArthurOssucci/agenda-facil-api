package br.com.portfolio.agendafacil.repository;

import br.com.portfolio.agendafacil.domain.Appointment;
import br.com.portfolio.agendafacil.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientIdOrderByStartsAtDesc(Long clientId);

    List<Appointment> findByProfessionalIdOrderByStartsAtDesc(Long professionalId);

    @Query("""
            select count(a) > 0
            from Appointment a
            where a.professional.id = :professionalId
              and a.status = :status
              and a.startsAt < :endsAt
              and a.endsAt > :startsAt
            """)
    boolean existsConflict(Long professionalId, LocalDateTime startsAt, LocalDateTime endsAt, AppointmentStatus status);
}
