package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.Availability;
import br.com.portfolio.agendafacil.domain.Professional;
import br.com.portfolio.agendafacil.domain.ServiceOffering;
import br.com.portfolio.agendafacil.domain.UserRole;
import br.com.portfolio.agendafacil.dto.ApiDtos.AvailabilityRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.AvailabilityResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.ProfessionalRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.ProfessionalResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.ServiceRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.ServiceResponse;
import br.com.portfolio.agendafacil.exception.BusinessException;
import br.com.portfolio.agendafacil.exception.NotFoundException;
import br.com.portfolio.agendafacil.repository.AvailabilityRepository;
import br.com.portfolio.agendafacil.repository.ProfessionalRepository;
import br.com.portfolio.agendafacil.repository.ServiceOfferingRepository;
import br.com.portfolio.agendafacil.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final ServiceOfferingRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    public CatalogService(
            ServiceOfferingRepository serviceRepository,
            ProfessionalRepository professionalRepository,
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository,
            Mapper mapper
    ) {
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        var service = new ServiceOffering();
        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPrice(request.price());
        return mapper.toServiceResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listServices() {
        return serviceRepository.findByActiveTrueOrderByName().stream()
                .map(mapper::toServiceResponse)
                .toList();
    }

    @Transactional
    public ProfessionalResponse createProfessional(ProfessionalRequest request) {
        var user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
        if (user.getRole() != UserRole.PROFESSIONAL && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Usuario precisa ter perfil PROFESSIONAL ou ADMIN");
        }
        if (professionalRepository.findByUserId(user.getId()).isPresent()) {
            throw new BusinessException("Usuario ja esta cadastrado como profissional");
        }

        var professional = new Professional();
        professional.setUser(user);
        professional.setSpecialty(request.specialty());
        return mapper.toProfessionalResponse(professionalRepository.save(professional));
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponse> listProfessionals() {
        return professionalRepository.findActiveWithUserOrderByName().stream()
                .map(mapper::toProfessionalResponse)
                .toList();
    }

    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request) {
        if (!request.startsAt().isBefore(request.endsAt())) {
            throw new BusinessException("Horario inicial deve ser anterior ao horario final");
        }

        var professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new NotFoundException("Profissional nao encontrado"));

        var availability = new Availability();
        availability.setProfessional(professional);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartsAt(request.startsAt());
        availability.setEndsAt(request.endsAt());
        return mapper.toAvailabilityResponse(availabilityRepository.save(availability));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> listAvailabilities(Long professionalId) {
        return availabilityRepository.findByProfessionalIdOrderByDayOfWeekAscStartsAtAsc(professionalId).stream()
                .map(mapper::toAvailabilityResponse)
                .toList();
    }
}
