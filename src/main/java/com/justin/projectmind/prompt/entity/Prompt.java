package com.justin.projectmind.prompt.entity;

import com.justin.projectmind.common.audit.BaseEntity;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.workspace.entity.Workspace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "prompts")
@SoftDelete(columnName = "deleted")
@Getter
@Setter
@NoArgsConstructor
public class Prompt extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "target_model", length = 100)
    private String targetModel;

    @Column(name = "task_type", length = 100)
    private String taskType;

    /** Optional self-assessment, 1-5. */
    private Integer rating;

    /** Editorial version of the prompt; distinct from the JPA optimistic-lock version. */
    @Column(name = "prompt_version", nullable = false)
    private int promptVersion = 1;

    @Column(length = 2000)
    private String notes;

    // EAGER required: a to-one pointing at a @SoftDelete entity cannot be lazy.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "prompt_tags",
            joinColumns = @JoinColumn(name = "prompt_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
}
