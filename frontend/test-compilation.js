// Test simple pour vérifier la compilation TypeScript
const { execSync } = require('child_process');

try {
  console.log('Test de compilation TypeScript...');
  
  // Vérifier que les fichiers TypeScript sont valides
  const result = execSync('npx tsc --noEmit --project tsconfig.json', { 
    cwd: __dirname,
    encoding: 'utf8',
    stdio: 'pipe'
  });
  
  console.log('✅ Compilation TypeScript réussie !');
  console.log(result);
} catch (error) {
  console.error('❌ Erreurs de compilation TypeScript :');
  console.error(error.stdout || error.message);
  process.exit(1);
}