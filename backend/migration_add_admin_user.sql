-- Migration pour ajouter un utilisateur administrateur par défaut
-- Mot de passe: admin123 (hashé avec BCrypt)

INSERT INTO users (
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    email_verified, 
    provider, 
    created_at,
    archived
) VALUES (
    'admin@kawi.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- admin123
    'Admin',
    'System',
    'ADMIN',
    true,
    'local',
    EXTRACT(EPOCH FROM NOW()) * 1000,
    false
) ON CONFLICT (email) DO NOTHING;

-- Note: Changez le mot de passe après la première connexion!