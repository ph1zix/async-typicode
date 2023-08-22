package com.example.asynctypicode;

import com.example.asynctypicode.api.UserPostDto;
import com.example.asynctypicode.domain.Post;
import com.example.asynctypicode.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPostService {

    @Value("${external.api.base-uri}")
    private String baseUri;

    private final WebClient.Builder webClientBuilder;

    public Mono<UserPostDto> getUserPostsBy(String userId) {
        return getUserBy(userId)
            .flatMap(user -> getPostsFrom(user)
                .collectList()
                .map(posts -> new UserPostDto(user, posts))
            );
    }

    public Flux<UserPostDto> getAllUsersWithPosts() {
        return getAllUsers().flatMap(user -> getPostsFrom(user)
            .collectList()
            .map(posts -> new UserPostDto(user, posts))
        );
    }

    private Mono<User> getUserBy(String userId) {
        try {
            int userIdNumeric = Integer.parseInt(userId);

            return webClientBuilder.build()
                .get()
                .uri(baseUri + "/users/{userId}", userIdNumeric)
                .retrieve()
                .bodyToMono(User.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.warn("Oopsy: User with ID '{}' not found!", userIdNumeric);
                    return Mono.empty();
                });

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Hint hint: '%s' is not a valid user ID, try something numeric", userId),
                exception
            );
        }
    }

    private Flux<Post> getPostsFrom(User user) {
        return webClientBuilder.build()
            .get()
            .uri(baseUri + "/posts?userId={userId}", user.getId())
            .retrieve()
            .bodyToFlux(Post.class);
    }

    private Flux<User> getAllUsers() {
        return webClientBuilder.build()
            .get()
            .uri(baseUri + "/users")
            .retrieve()
            .bodyToFlux(User.class);
    }
}