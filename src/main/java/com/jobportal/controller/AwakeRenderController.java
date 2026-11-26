package com.jobportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class AwakeRenderController {
@GetMapping("/awakeRender")
    public ResponseEntity<String> awakeRender(){
    System.out.println("Render awaked");
    return ResponseEntity.status(200).body("Render awaked");
}
}
