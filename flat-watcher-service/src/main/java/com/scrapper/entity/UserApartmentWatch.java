package com.scrapper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_apartment_watch",
        uniqueConstraints = @UniqueConstraint(name="uk_uaw_user_apartment", columnNames = {"user_id","apartment_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApartmentWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId; // auth-service user id

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false, foreignKey = @ForeignKey(name="fk_uaw_apartment"))
    private Apartment apartment;

    @Column(name="min_price")
    private Integer minPrice;

    @Column(name="active", nullable=false)
    private boolean active = true;

    @Column(name="created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private LocalDateTime updatedAt;

    @Column(name="last_notified_at")
    private LocalDateTime lastNotifiedAt;
}
