package io.github.dbwhd5566.trafficqueuelab.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Account() {
    }

    private Account(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }

    public static Account create(String email, String nickname) {
        return new Account(email, nickname);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
