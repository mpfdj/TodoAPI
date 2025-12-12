package jaeger.de.miel.TodoAPI.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "LIST", indexes = {
        @Index(name = "IDX_LIST_OWNER_ID", columnList = "OWNER_ID")
})
public class List {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private AppUser owner;

    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "CHARACTER LARGE OBJECT")
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

/*
 TODO [Reverse Engineering] create field to map the 'DESCRIPTION' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "DESCRIPTION", columnDefinition = "CHARACTER LARGE OBJECT")
    private Object description;
*/
}