package br.com.santosschein.complexo.infrastructure.persistence.entity;

import br.com.santosschein.complexo.domain.model.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservations_active_slot",
                columnNames = {"active_field_id", "active_reservation_date", "active_slot_id"}
        ),
        indexes = {
                @Index(name = "idx_reservations_user_status_date", columnList = "user_id, status, reservation_date"),
                @Index(name = "idx_reservations_user_date", columnList = "user_id, reservation_date"),
                @Index(name = "idx_reservations_date_status", columnList = "reservation_date, status"),
                @Index(
                        name = "idx_reservations_field_date_slot_status",
                        columnList = "field_id, reservation_date, slot_id, status"
                ),
                @Index(name = "idx_reservations_slot_date", columnList = "slot_id, reservation_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private SoccerFieldEntity field;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private ReservationSlotEntity slot;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * Copia do preco no momento da confirmacao: alterar o preco do horario nao muda o historico.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "active_field_id", insertable = false, updatable = false)
    private Long activeFieldId;

    @Column(name = "active_reservation_date", insertable = false, updatable = false)
    private LocalDate activeReservationDate;

    @Column(name = "active_slot_id", insertable = false, updatable = false)
    private Long activeSlotId;

    public ReservationEntity(
            Long id,
            UserEntity user,
            SoccerFieldEntity field,
            ReservationSlotEntity slot,
            LocalDate reservationDate,
            ReservationStatus status,
            LocalDateTime createdAt,
            LocalDateTime canceledAt,
            BigDecimal price
    ) {
        this.id = id;
        this.user = user;
        this.field = field;
        this.slot = slot;
        this.reservationDate = reservationDate;
        this.status = status;
        this.createdAt = createdAt;
        this.canceledAt = canceledAt;
        this.price = price;
    }
}
