package com.scrapper.apartmentscraper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID użytkownika (pochodzi z tokena JWT lub innego mikroserwisu)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "price", nullable = false)
    private String price;

    @Column(name = "initial_price", nullable = false)
    private String initialPrice;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

}

