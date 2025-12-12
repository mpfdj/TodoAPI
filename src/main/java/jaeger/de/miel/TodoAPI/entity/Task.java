package jaeger.de.miel.TodoAPI.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "TASK", indexes = {
        @Index(name = "IDX_TASK_LIST_STATUS_DUE", columnList = "LIST_ID, STATUS, DUE_DATE"),
        @Index(name = "IDX_TASK_LIST_ID", columnList = "LIST_ID"),
        @Index(name = "IDX_TASK_STATUS", columnList = "STATUS"),
        @Index(name = "IDX_TASK_DUE_DATE", columnList = "DUE_DATE"),
        @Index(name = "IDX_TASK_COMPLETED_AT", columnList = "COMPLETED_AT")
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "LIST_ID", nullable = false)
    private List list;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "CREATOR_ID", nullable = false)
    private AppUser creator;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "CHARACTER LARGE OBJECT")
    private String description;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "PRIORITY")
    private Integer priority;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

    @Column(name = "COMPLETED_AT")
    private Instant completedAt;

/*
 TODO [Reverse Engineering] create field to map the 'DESCRIPTION' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "DESCRIPTION", columnDefinition = "CHARACTER LARGE OBJECT")
    private Object description;
*/
}