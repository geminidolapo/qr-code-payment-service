package com.project.payment.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Entity
@ToString
@SuperBuilder
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({SoftDeleteListener.class, AuditingEntityListener.class})
@SQLDelete(sql="UPDATE roles SET deleted=true WHERE id=?")
public class Role extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @Column(length = 8)
    @Builder.Default
    private String type = "user";

    private String description;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

}