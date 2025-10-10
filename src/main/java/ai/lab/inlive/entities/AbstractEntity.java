package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

import static ai.lab.inlive.dto.response.ErrorResponse.ZONE_ID;

@EqualsAndHashCode
@Getter
@MappedSuperclass
public abstract class AbstractEntity<T extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    T id;

    @Column(name = "created_at", nullable = false)
//    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
//    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = this.createdAt = LocalDateTime.now(ZONE_ID);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZONE_ID);
    }
}