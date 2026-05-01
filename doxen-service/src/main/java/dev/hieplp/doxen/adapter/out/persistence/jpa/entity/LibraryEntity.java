package dev.hieplp.doxen.adapter.out.persistence.jpa.entity;

import dev.hieplp.doxen.domain.enums.LibraryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "libraries")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryEntity extends AuditEntity {

    @Id
    @Column(name = "id", length = 36)
    private String libraryId;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "homepage_url")
    private String homepageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LibraryStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
