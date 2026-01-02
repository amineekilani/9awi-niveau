package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.LevelRequest;
import com.kawi_niveau.backend.dto.LevelResponse;
import com.kawi_niveau.backend.entity.Level;
import com.kawi_niveau.backend.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LevelService {

    @Autowired
    private LevelRepository levelRepository;

    public Page<LevelResponse> getAllLevels(Pageable pageable) {
        Page<Level> levels = levelRepository.findAll(pageable);
        return levels.map(this::convertToResponse);
    }

    public List<LevelResponse> getAllLevelsOrdered() {
        List<Level> levels = levelRepository.findAllOrderByLevel();
        return levels.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public LevelResponse getLevelById(Long id) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));
        return convertToResponse(level);
    }

    public LevelResponse createLevel(LevelRequest request) {
        // Vérifier que le niveau n'existe pas déjà
        if (levelRepository.findByLevel(request.getLevel()).isPresent()) {
            throw new RuntimeException("Un niveau " + request.getLevel() + " existe déjà");
        }

        Level level = new Level();
        level.setLevel(request.getLevel());
        level.setXpRequired(request.getXpRequired());
        level.setName(request.getName());
        level.setDescription(request.getDescription());

        level = levelRepository.save(level);
        return convertToResponse(level);
    }

    public LevelResponse updateLevel(Long id, LevelRequest request) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        // Vérifier que le nouveau numéro de niveau n'est pas déjà pris
        if (!level.getLevel().equals(request.getLevel())) {
            if (levelRepository.findByLevel(request.getLevel()).isPresent()) {
                throw new RuntimeException("Un niveau " + request.getLevel() + " existe déjà");
            }
        }

        level.setLevel(request.getLevel());
        level.setXpRequired(request.getXpRequired());
        level.setName(request.getName());
        level.setDescription(request.getDescription());

        level = levelRepository.save(level);
        return convertToResponse(level);
    }

    public void deleteLevel(Long id) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Niveau non trouvé"));

        // Vérifier qu'aucun utilisateur n'a ce niveau
        // (Cette vérification pourrait être ajoutée si nécessaire)

        levelRepository.delete(level);
    }

    private LevelResponse convertToResponse(Level level) {
        LevelResponse response = new LevelResponse();
        response.setId(level.getId());
        response.setLevel(level.getLevel());
        response.setXpRequired(level.getXpRequired());
        response.setName(level.getName());
        response.setDescription(level.getDescription());
        response.setCreatedAt(level.getCreatedAt());
        return response;
    }
}