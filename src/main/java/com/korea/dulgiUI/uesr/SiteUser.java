package com.korea.dulgiUI.uesr;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String nickname;

    private String password;

    private String gender; // 라디오 버튼 값 받기

    @Email
    @Column(unique = true)
    private String email;

    private String userRole;

    private String mobile;

    private String location;

    private String languages;

    private String birthday;
}
