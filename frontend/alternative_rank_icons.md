# Alternative: Icônes différentes pour chaque rang

Si vous préférez avoir des icônes différentes pour chaque rang du podium, voici les modifications à apporter :

## Option 1: Icônes variées mais cohérentes

```typescript
// Dans classement.ts
getRankIcon(rank: number): string {
  switch (rank) {
    case 1: return 'crown';     // Couronne pour le 1er (si disponible)
    case 2: return 'award';     // Trophée pour le 2ème
    case 3: return 'star';      // Étoile pour le 3ème
    default: return 'user';
  }
}
```

## Option 2: Tous des trophées (recommandé - actuel)

```typescript
// Dans classement.ts - Configuration actuelle
getRankIcon(rank: number): string {
  switch (rank) {
    case 1: return 'award';     // Badge doré
    case 2: return 'award';     // Badge argenté
    case 3: return 'award';     // Badge bronze
    default: return 'user';
  }
}
```

## Option 3: Icônes spécialisées

```typescript
// Dans classement.ts
getRankIcon(rank: number): string {
  switch (rank) {
    case 1: return 'trophy';    // Si l'icône trophy existe
    case 2: return 'medal';     // Si l'icône medal existe
    case 3: return 'star';      // Étoile pour le 3ème
    default: return 'user';
  }
}
```

## Icônes Feather disponibles pour les rangs:

- `award` ✅ (trophée - recommandé)
- `star` ✅ (étoile)
- `target` ✅ (cible)
- `zap` ✅ (éclair)
- `shield` ✅ (bouclier)
- `hexagon` ✅ (hexagone)

## Note importante:

L'icône `crown` n'existe pas dans Feather Icons standard. Si vous voulez l'utiliser, il faudrait:

1. Ajouter une bibliothèque d'icônes supplémentaire
2. Ou utiliser une image SVG personnalisée
3. Ou utiliser une autre icône Feather disponible

## Configuration actuelle recommandée:

La configuration actuelle avec tous les badges utilisant l'icône `award` est recommandée car:

- ✅ Cohérence visuelle
- ✅ Icône disponible dans Feather
- ✅ Différenciation par couleur (or/argent/bronze)
- ✅ Animation spéciale pour le rang 1
