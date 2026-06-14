package com.justin.projectmind.modelregistry.entity;

import com.justin.projectmind.common.audit.BaseEntity;
import com.justin.projectmind.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

/**
 * Metadata about a (typically local) AI model. Intentionally does not store real
 * filesystem paths to private model weights.
 */
@Entity
@Table(name = "ai_models")
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class AiModel extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ModelFormat format;

    @Column(length = 50)
    private String quantization;

    @Column(name = "estimated_vram_mb")
    private Integer estimatedVramMb;

    @Column(name = "use_case", length = 500)
    private String useCase;

    @Column(length = 2000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
