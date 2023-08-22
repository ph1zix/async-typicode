package com.example.asynctypicode.api;

import com.example.asynctypicode.domain.Post;
import com.example.asynctypicode.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserPostController {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.api.base-uri}")
    private String baseUri;

    @GetMapping("/user/{userId}")
    public Mono<UserPostDto> getUserPostsBy(@PathVariable String userId) {
        Mono<User> userMono = webClientBuilder.build()
            .get()
            .uri(baseUri + "/users/{userId}", userId)
            .retrieve()
            .bodyToMono(User.class)
            .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                log.warn("Oopsy: User with ID '{}' not found!", userId);
                return Mono.empty();
            });

        Mono<List<Post>> postsMono = webClientBuilder.build()
            .get()
            .uri(baseUri + "/posts?userId={userId}", userId)
            .retrieve()
            .bodyToFlux(Post.class)
            .collectList();

        return userMono.flatMap(user ->
            postsMono
                .map(posts -> new UserPostDto(user, posts))
        );
    }

    @GetMapping("/users")
    public Flux<UserPostDto> getAllUsers() {
        Flux<User> usersFlux = webClientBuilder.build()
            .get()
            .uri(baseUri + "/users")
            .retrieve()
            .bodyToFlux(User.class);

        return usersFlux.flatMap(user ->
            webClientBuilder.build()
                .get()
                .uri(baseUri + "/posts?userId={userId}", user.getId())
                .retrieve()
                .bodyToFlux(Post.class)
                .collectList()
                .map(posts -> new UserPostDto(user, posts))
        );
    }
}