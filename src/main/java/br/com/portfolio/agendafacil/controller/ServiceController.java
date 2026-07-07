package br.com.portfolio.agendafacil.controller;

import br.com.portfolio.agendafacil.dto.ApiDtos.ServiceRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.ServiceResponse;
import br.com.portfolio.agendafacil.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final CatalogService catalogService;

    public ServiceController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse create(@RequestBody @Valid ServiceRequest request) {
        return catalogService.createService(request);
    }

    @GetMapping
    public List<ServiceResponse> list() {
        return catalogService.listServices();
    }
}
