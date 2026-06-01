package com.PAWPAW.pawpaw.community.entity;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.group.entity.Group;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Like> likes;

    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}