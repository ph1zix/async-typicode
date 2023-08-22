package com.example.asynctypicode.api;

import com.example.asynctypicode.domain.Post;
import com.example.asynctypicode.domain.User;
import lombok.Getter;

import java.util.List;

@Getter
public class UserPostDto {
    private User user;
    private List<Post> posts;

    public UserPostDto(User user, List<Post> posts) {
        this.user = user;
        this.posts = posts;
    }
}