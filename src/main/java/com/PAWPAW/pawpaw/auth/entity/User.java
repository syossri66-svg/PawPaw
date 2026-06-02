package com.PAWPAW.pawpaw.auth.entity;

import com.PAWPAW.pawpaw.chat.entity.Message;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder.Default
    private boolean isVerified = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isBanned = false;

    private String bio;


    private String profilePicture;
    private String coverPhoto;

    private String location;

    @OneToMany(mappedBy = "sender")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Message> sentMessages;


    public String getAvatarUrl() {
        return this.profilePicture;
    }

    public String getCoverUrl() {
        return this.coverPhoto;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.profilePicture = avatarUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverPhoto = coverUrl;
    }
}