package br.com.portfolio.agendafacil.controller;

import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentRequest;
import br.com.portfolio.agendafacil.dto.ApiDtos.AppointmentResponse;
import br.com.portfolio.agendafacil.security.AuthenticatedUser;
import br.com.portfolio.agendafacil.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse schedule(
            @RequestBody @Valid AppointmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return appointmentService.schedule(request, authenticatedUser);
    }

    @GetMapping("/me")
    public List<AppointmentResponse> listMine(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return appointmentService.listMine(authenticatedUser);
    }

    @PatchMapping("/{appointmentId}/cancel")
    public AppointmentResponse cancel(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return appointmentService.cancel(appointmentId, authenticatedUser);
    }
}
