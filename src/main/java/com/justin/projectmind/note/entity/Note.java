package com.justin.projectmind.note.entity;

import com.justin.projectmind.common.audit.BaseEntity;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.workspace.entity.Workspace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notes")
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Note extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    /** Markdown body. */
    @Column(columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoteType type;

    // EAGER required: a to-one pointing at a @SoftDelete entity cannot be lazy.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
}
