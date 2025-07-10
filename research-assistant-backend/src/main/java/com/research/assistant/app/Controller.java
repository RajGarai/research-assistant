package com.research.assistant.app;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


//api/research/process
@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class Controller {
    private final Services services;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody Request request) {
        String result = services.processContent(request);
        return ResponseEntity.ok(result);
    }
}
