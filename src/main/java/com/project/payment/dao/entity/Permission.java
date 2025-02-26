package com.project.payment.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
@EntityListeners(SoftDeleteListener.class)
public class Permission extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(length=100, unique=true, nullable=false)
    private String name;

    @Column(length=8)
    @Builder.Default
    private String type = "user";

    private String description;
}
