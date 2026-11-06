package br.com.santosschein.complexo.interfaces.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Atalho para a pagina web do administrador (src/main/resources/static/admin/index.html).
 */
@RestController
public class AdminPageController {

    @GetMapping({"/admin", "/admin/"})
    public ResponseEntity<Void> adminPage() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/admin/index.html")
                .build();
    }
}
