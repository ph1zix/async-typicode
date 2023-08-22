package com.example.asynctypicode.api;

import com.example.asynctypicode.UserPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class UserPostController {

    private final UserPostService service;

    @GetMapping("/user/{userId}")
    public Mono<UserPostDto> getUserPostsBy(@PathVariable String userId) {
        return service.getUserPostsBy(userId);
    }

    @GetMapping("/users")
    public Flux<UserPostDto> getAllUsersWithPosts() {
        return service.getAllUsersWithPosts();
    }
}