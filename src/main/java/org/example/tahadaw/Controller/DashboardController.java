package org.example.tahadaw.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.OUT.DashboardDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTOOut> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getDashboard(user.getId()));
    }
}
