package com.example.asynctypicode.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserPostControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void should_have_user_and_post_json_path_when_get_user_posts_by_userid_gets_requested() {
        webTestClient.get().uri("/user/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user.id").isEqualTo(1)
            .jsonPath("$.user.name").isEqualTo("Leanne Graham")
            .jsonPath("$.posts[0].id").isEqualTo(1)
            .jsonPath("$.posts[0].title").isEqualTo("sunt aut facere repellat provident occaecati excepturi optio reprehenderit");
    }

    @Test
    void should_have_body_list_size_of_ten_when_get_all_users_with_posts_gets_requested() {
        webTestClient.get().uri("/users")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(UserPostDto.class)
            .hasSize(10);
    }

    @Test
    void should_respond_with_bad_request_when_invalid_path_variable() {
        webTestClient.get().uri("/user/invalid")
            .exchange()
            .expectStatus().isBadRequest();
    }
}