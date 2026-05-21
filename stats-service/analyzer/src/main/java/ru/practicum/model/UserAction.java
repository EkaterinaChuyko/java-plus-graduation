package ru.practicum.model;

import com.netflix.appinfo.InstanceInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private InstanceInfo.ActionType action;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "created")
    private Instant created;
}
