package com.kawi_niveau.backend.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercice_element")
@Data
public class ExerciceElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_element", nullable = false)
    private TypeElement typeElement;

    @Column(name = "position_ordre", nullable = false)
    private Integer positionOrdre;

    @Column(name = "reponse_correcte")
    private String reponseCorrecte;

    @Column(name = "options", columnDefinition = "TEXT")
    private String optionsJson;

    @Transient
    private List<String> options = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        serializeOptions();
    }

    @PreUpdate
    protected void onUpdate() {
        serializeOptions();
    }

    @PostLoad
    protected void onLoad() {
        deserializeOptions();
    }

    private void serializeOptions() {
        if (options != null && !options.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.optionsJson = mapper.writeValueAsString(options);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erreur lors de la sérialisation des options", e);
            }
        }
    }

    private void deserializeOptions() {
        if (optionsJson != null && !optionsJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.options = mapper.readValue(optionsJson, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                this.options = new ArrayList<>();
            }
        } else {
            this.options = new ArrayList<>();
        }
    }

    public void setOptions(List<String> options) {
        this.options = options;
        serializeOptions();
    }

    public List<String> getOptions() {
        if (options == null || options.isEmpty()) {
            deserializeOptions();
        }
        return options;
    }

    public enum TypeElement {
        TEXT,          // Texte normal
        BLANK,         // Espace à remplir
        DRAGGABLE,     // Élément déplaçable
        DROP_ZONE      // Zone de dépôt
    }
}