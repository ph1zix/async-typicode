package com.example.asynctypicode.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Post {
    private int userId;
    private int id;
    private String title;
    private String body;
}
