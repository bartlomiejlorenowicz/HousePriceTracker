package com.scrapper.entity;

import com.scrapper.dto.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "initial_price", nullable = false)
    private BigDecimal initialPrice;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

}

