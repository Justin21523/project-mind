package com.justin.projectmind.tag.entity;

import com.justin.projectmind.common.audit.BaseEntity;
import com.justin.projectmind.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

/**
 * A reusable label owned by a user. Intended to be attached to projects, notes,
 * prompts, and resources in later phases.
 */
@Entity
@Table(name = "tags")
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Tag extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
