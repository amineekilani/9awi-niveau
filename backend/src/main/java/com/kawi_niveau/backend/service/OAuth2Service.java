package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;

    public User processGoogleUser(String googleIdToken) {
        // Vérifier le token avec Google
        Map<String, Object> googleUserInfo = verifyGoogleToken(googleIdToken);
        
        if (googleUserInfo == null) {
            throw new RuntimeException("Invalid Google token");
        }

        String email = (String) googleUserInfo.get("email");
        String googleId = (String) googleUserInfo.get("sub");
        String name = (String) googleUserInfo.get("name");

        // Chercher ou créer l'utilisateur (exclure les comptes archivés)
        Optional<User> existingUser = userRepository.findByEmailAndArchivedFalse(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Mettre à jour les infos Google si nécessaire
            if (user.getProvider() == null || !user.getProvider().equals("google")) {
                user.setProvider("google");
                user.setProviderId(googleId);
                user.setEmailVerified(true); // Les utilisateurs Google ont leur email déjà vérifié
                userRepository.save(user);
            }
            return user;
        } else {
            // Créer un nouvel utilisateur
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setProvider("google");
            newUser.setProviderId(googleId);
            newUser.setRole(com.kawi_niveau.backend.entity.Role.ETUDIANT);
            newUser.setEmailVerified(true); // Les utilisateurs Google ont leur email déjà vérifié
            return userRepository.save(newUser);
        }
    }

    private Map<String, Object> verifyGoogleToken(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
