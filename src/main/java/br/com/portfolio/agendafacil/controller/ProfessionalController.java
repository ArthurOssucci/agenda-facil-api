package br.com.portfolio.agendafacil.controller;

import br.com.portfolio.agendafacil.dto.ApiDtos.AvailabilityRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.AvailabilityResponse;
import br.com.portfolio.agendafacil.dto.ApiDtos.ProfessionalRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.ProfessionalResponse;
import br.com.portfolio.agendafacil.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final CatalogService catalogService;

    public ProfessionalController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProfessionalResponse create(@RequestBody @Valid ProfessionalRequest request) {
        return catalogService.createProfessional(request);
    }

    @GetMapping
    public List<ProfessionalResponse> list() {
        return catalogService.listProfessionals();
    }

    @PostMapping("/availability")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROFESSIONAL')")
    public AvailabilityResponse createAvailability(@RequestBody @Valid AvailabilityRequest request) {
        return catalogService.createAvailability(request);
    }

    @GetMapping("/{professionalId}/availability")
    public List<AvailabilityResponse> listAvailabilities(@PathVariable Long professionalId) {
        return catalogService.listAvailabilities(professionalId);
    }
}
