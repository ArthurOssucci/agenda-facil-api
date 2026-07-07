package br.com.portfolio.agendafacil.repository;

import br.com.portfolio.agendafacil.domain.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findByActiveTrueOrderByName();
}
