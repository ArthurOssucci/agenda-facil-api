package br.com.portfolio.agendafacil.repository;

import br.com.portfolio.agendafacil.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByProfessionalIdOrderByDayOfWeekAscStartsAtAsc(Long professionalId);

    List<Availability> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeek dayOfWeek);
}
