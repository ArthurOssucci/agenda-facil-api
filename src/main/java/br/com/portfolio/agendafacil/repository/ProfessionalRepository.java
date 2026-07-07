package br.com.portfolio.agendafacil.repository;

import br.com.portfolio.agendafacil.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    @Query("""
            select p
            from Professional p
            join fetch p.user u
            where p.active = true
            order by u.name
            """)
    List<Professional> findActiveWithUserOrderByName();

    Optional<Professional> findByUserId(Long userId);
}
