package me.gnevilkoko.project_manager.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/just-test")
public class TestController {

    @GetMapping
    public String get(){
        return "Hello World";
    }
}
