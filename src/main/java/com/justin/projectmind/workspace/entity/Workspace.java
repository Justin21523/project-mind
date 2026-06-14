package com.justin.projectmind.workspace.entity;

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
 * A workspace groups projects, notes, prompts, and resources for a single owner.
 * Rows are soft-deleted: Hibernate filters out deleted rows automatically.
 */
@Entity
@Table(name = "workspaces")
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Workspace extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
