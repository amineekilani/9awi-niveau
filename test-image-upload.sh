#!/bin/bash

# Script de test du système d'upload d'images
# Assurez-vous que le backend est lancé sur http://localhost:8080

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${YELLOW}=== Tests du système d'upload d'images ===${NC}\n"

# Test 1: Upload une image sans authentification
echo -e "${YELLOW}Test 1: Upload d'une image (endpoint public)${NC}"
if [ -f "./test-image.jpg" ]; then
    curl -X POST \
        -F "file=@./test-image.jpg" \
        "${BASE_URL}/images/users/upload" \
        -w "\nStatus: %{http_code}\n\n"
else
    echo -e "${RED}test-image.jpg not found. Veuillez créer une image de test.${NC}\n"
fi

# Test 2: Récupérer une image (remplacer {filename} par un vrai nom)
echo -e "${YELLOW}Test 2: Récupérer une image${NC}"
echo "Utilisez l'URL: ${BASE_URL}/images/users/{filename}"
echo "Exemple: ${BASE_URL}/images/users/12345-uuid.jpg\n"

# Test 3: Créer un utilisateur pour les tests
echo -e "${YELLOW}Test 3: Créer un compte de test${NC}"
curl -X POST \
    -H "Content-Type: application/json" \
    -d '{
        "email": "test@example.com",
        "password": "Password123!",
        "firstName": "Test",
        "lastName": "User",
        "dateOfBirth": "1990-01-01"
    }' \
    "${BASE_URL}/api/auth/register" \
    -w "\nStatus: %{http_code}\n\n"

# Test 4: Se connecter (après avoir vérifié l'email)
echo -e "${YELLOW}Test 4: Se connecter${NC}"
echo "Email: test@example.com"
echo "Password: Password123!"
echo "Utilisez le token JWT retourné pour les tests authentifiés\n"

# Test 5: Upload l'image du profil (authentifié)
echo -e "${YELLOW}Test 5: Upload l'image du profil (avec authentification)${NC}"
echo "Remplacez {TOKEN} par votre JWT token"
echo "Command:"
echo "curl -X POST \\"
echo "  -H 'Authorization: Bearer {TOKEN}' \\"
echo "  -F 'file=@./test-image.jpg' \\"
echo "  '${BASE_URL}/api/profile/upload-image'\n"

# Test 6: Récupérer le profil
echo -e "${YELLOW}Test 6: Récupérer le profil avec l'image${NC}"
echo "Command:"
echo "curl -X GET \\"
echo "  -H 'Authorization: Bearer {TOKEN}' \\"
echo "  '${BASE_URL}/api/profile'\n"

# Test 7: Créer une image de test
echo -e "${YELLOW}Création d'une image de test (si elle n'existe pas)${NC}"
if ! [ -f "./test-image.jpg" ]; then
    # Créer une simple image JPG rouge 100x100
    python3 -c "
from PIL import Image
img = Image.new('RGB', (100, 100), color='red')
img.save('test-image.jpg')
print('✓ test-image.jpg créée')
" 2>/dev/null || echo "PIL non disponible. Créez manuellement une image test.jpg"
fi

echo -e "\n${GREEN}=== Tests terminés ===${NC}"
echo -e "\n${YELLOW}Notes:${NC}"
echo "1. Les uploads réussis retournent: { \"filename\": \"...\", \"url\": \"...\" }"
echo "2. Les images sont stockées dans: uploads/users/"
echo "3. Les accès sans authentification sont possibles via /images/users/{filename}"
echo "4. L'upload du profil requiert un JWT token valide\n"
