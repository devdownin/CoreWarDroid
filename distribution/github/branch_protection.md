# Guide de Protection de la Branche `main` sur GitHub

Pour garantir la stabilité du projet **CoreWar KMP**, il est recommandé de configurer des règles de protection pour la branche `main`. Voici les étapes à suivre :

## 1. Accéder aux Paramètres
1. Allez sur la page principale de votre dépôt sur GitHub.
2. Cliquez sur l'onglet **Settings** (Paramètres).
3. Dans la barre latérale gauche, cliquez sur **Code and automation** > **Branches**.

## 2. Ajouter une Règle de Protection
1. Sous **Branch protection rules**, cliquez sur **Add branch protection rule**.
2. Dans le champ **Branch name pattern**, saisissez `main`.

## 3. Paramètres Recommandés
Cochez les options suivantes pour une sécurité optimale :

- **Require a pull request before merging** : Empêche les commits directs sur `main`.
  - *Require approvals* : (Optionnel) Cochez cette case et réglez-la sur `1` si vous avez des collaborateurs.
- **Require status checks to pass before merging** : Indispensable pour la CI/CD.
  - Cochez *Require branches to be up to date before merging*.
  - Recherchez et ajoutez vos jobs GitHub Actions (ex: `build-android`, `run-tests`).
- **Require conversation resolution before merging** : Garantit que tous les commentaires de la Pull Request ont été traités.
- **Do not allow bypassing the above settings** : Applique ces règles même aux administrateurs du dépôt.

## 4. Sauvegarder
Cliquez sur le bouton **Create** (ou **Save changes**) en bas de la page.

---

### Pourquoi est-ce important ?
- **Qualité du Code** : Garantit que chaque changement passe par une revue et des tests automatisés.
- **Stabilité de la Release** : Empêche les régressions accidentelles avant la publication sur le Play Store.
- **Historique Propre** : Encourage l'utilisation de Pull Requests descriptives plutôt que de micro-commits directs.
