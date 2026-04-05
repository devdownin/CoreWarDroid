# Suggestions : Qualité et Préférences Utilisateur

## 1. Qualité du Code et Maintenance
*   **Analyse Statique** : Intégrer **Detekt** pour maintenir une complexité cyclomatique basse et respecter les conventions de nommage Kotlin.
*   **Tests UI** : Ajouter des tests de composants utilisant `compose-test` pour vérifier le comportement des écrans sans lancer l'application complète.
*   **Modularisation** : Séparer le `core-engine` du module `composeApp` pour permettre sa réutilisation (ex: un serveur de tournois en pur Kotlin/JVM).
*   **Logging Structuré** : Remplacer les `println` par une bibliothèque comme **Napier** (compatible KMP) pour une meilleure gestion des logs en production.

## 2. Préférences Utilisateur Avancées
*   **Paramètres de l'Arène** :
    *   Choisir la taille de la mémoire (de 1024 à 65536 cellules).
    *   Définir le nombre maximal de cycles avant un match nul.
    *   Ajuster la distance minimale de spawn entre les guerriers.
*   **Préférences de l'Éditeur** :
    *   Ajuster la taille de la police de caractères.
    *   Activer/Désactiver l'auto-complétion.
    *   Support du mode clair/sombre automatique basé sur le système.
*   **Gestion Audio** : Sliders pour le volume des effets sonores (SFX) et de la musique d'ambiance.

## 3. Internationalisation (i18n)
*   Extraire toutes les chaînes de caractères pour supporter le **Français** et l'**Anglais** (via `moko-resources` ou le support natif Compose Multiplatform Resources).

## 4. Accessibilité (A11y)
*   **Descriptions de contenu** : Ajouter des `contentDescription` explicites sur tous les graphiques de la mémoire et les boutons iconographiques.
*   **Support Clavier complet** : Permettre la navigation dans la grille mémoire via les flèches du clavier.

## 5. Expérience Utilisateur (UX)
*   **État de Restauration** : Sauvegarder le code en cours dans l'éditeur même si l'utilisateur quitte l'application sans sauvegarder explicitement (Draft system).
*   **Favoris** : Marquer certains guerriers comme favoris pour les retrouver plus vite dans la liste.
