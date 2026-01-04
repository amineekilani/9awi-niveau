// Test de compilation pour les composants parcours
const { execSync } = require('child_process');

try {
  console.log('🧪 Test de compilation des composants parcours...');
  
  // Vérifier que les fichiers TypeScript sont valides
  const result = execSync('npx tsc --noEmit --project tsconfig.json', { 
    cwd: __dirname,
    encoding: 'utf8',
    stdio: 'pipe'
  });
  
  console.log('✅ Compilation TypeScript réussie !');
  console.log('📦 Composants parcours prêts :');
  console.log('   - ParcoursCatalogueComponent');
  console.log('   - ParcoursDetailComponent');
  console.log('   - MesParcoursComponent');
  console.log('   - ParcoursService (méthodes apprenants)');
  console.log('   - Routes et navigation intégrées');
  
} catch (error) {
  console.error('❌ Erreurs de compilation TypeScript :');
  console.error(error.stdout || error.message);
  process.exit(1);
}