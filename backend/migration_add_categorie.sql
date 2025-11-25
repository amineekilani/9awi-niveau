-- Migration pour ajouter la catégorie aux cours
ALTER TABLE cours ADD COLUMN categorie VARCHAR(100);
