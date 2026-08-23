package com.andreitufeanu.backend.event.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name can't have more than {max} characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
