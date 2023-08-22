package com.example.asynctypicode.api;

import com.example.asynctypicode.domain.Post;
import com.example.asynctypicode.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
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
        try {
            int userIdNumeric = Integer.parseInt(userId);

            Mono<User> userMono = webClientBuilder.build()
                .get()
                .uri(baseUri + "/users/{userId}", userIdNumeric)
                .retrieve()
                .bodyToMono(User.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.warn("Oopsy: User with ID '{}' not found!", userIdNumeric);
                    return Mono.empty();
                });

            Mono<List<Post>> postsMono = webClientBuilder.build()
                .get()
                .uri(baseUri + "/posts?userId={userId}", userIdNumeric)
                .retrieve()
                .bodyToFlux(Post.class)
                .collectList();

            return userMono.flatMap(user ->
                postsMono
                    .map(posts -> new UserPostDto(user, posts))
            );

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Hint hint: '%s' is not a valid user ID, try something numeric", userId),
                e
            );
        }
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