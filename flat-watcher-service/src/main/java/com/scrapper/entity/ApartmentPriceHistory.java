package com.scrapper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apartment_price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="apartment_id", nullable=false, foreignKey = @ForeignKey(name="fk_aph_apartment"))
    private Apartment apartment;

    @Column(name="price", nullable=false)
    private Integer price;

    @Column(name="collected_at", insertable=false, updatable=false)
    private LocalDateTime collectedAt;
}
