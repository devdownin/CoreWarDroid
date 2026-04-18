# Recommandations pour la publication sur Google Play Store

Pour réussir la publication de **CoreWar KMP**, voici les étapes clés et les meilleures pratiques techniques et marketing à suivre :

## 1. Préparation Technique (Build & Sécurité)

### Build d'Release (AAB)
Google Play exige désormais le format **Android App Bundle (.aab)** plutôt que l'APK.
- Générez le build final via `./gradlew :composeApp:bundleRelease`.
- Assurez-vous que la signature de l'application est configurée dans `composeApp/build.gradle.kts` ou via les secrets de votre CI/CD.

### Optimisation R8/ProGuard
Le projet est configuré pour SDK 35. Activez la minification (`isMinifyEnabled = true`) dans votre build de production pour réduire la taille de l'application et protéger votre code source.

### Compatibilité Predictive Back
Nous avons déjà activé `android:enableOnBackInvokedCallback="true"`. Assurez-vous de tester la navigation entre l'éditeur et l'arène de combat pour garantir une expérience utilisateur fluide sur Android 14+.

## 2. Ressources Visuelles & Marketing

### Captures d'Écran (Screenshots)
Le Play Store nécessite au moins deux captures par format (téléphone, tablette 7", tablette 10").
- **Battle Arena** : Montrez la grille de mémoire néon en pleine action.
- **Redcode Editor** : Mettez en avant l'auto-complétion et la coloration syntaxique.
- **Academy** : Montrez l'arbre de compétences pour souligner l'aspect progression/RPG.

### Vidéo de Présentation
Une vidéo courte (30s) montrant un cycle de combat est fortement recommandée pour les jeux de simulation. Elle permet aux joueurs de comprendre instantanément le concept du "Code & Battle".

### Icônes Adaptatives
Nous avons implémenté les icônes adaptatives (Background + Foreground). Vérifiez le rendu dans l'aperçu du Play Store pour vous assurer que le logo n'est pas tronqué sur les différents lanceurs (rond, carré, squirclé).

## 3. Conformité & Questionnaire

### Questionnaire sur le Contenu
Lors de la soumission, vous devrez remplir le questionnaire sur le contenu. CoreWar KMP étant un simulateur de programmation abstrait, il devrait normalement obtenir une classification **PEGI 3** (ou equivalent).

### Sécurité des Données (Data Safety)
Bien que l'application ne collecte aucune donnée personnelle (comme indiqué dans notre Privacy Policy), vous devez déclarer officiellement sur la console Google Play :
- Aucune donnée collectée.
- Aucune donnée partagée avec des tiers.
- Chiffrement en transit non applicable (car pas de transfert).

## 4. Stratégie de Lancement

### Test Interne & Fermé
Ne passez pas directement en production.
1. **Test Interne** : Invitez jusqu'à 100 testeurs de confiance pour valider le build sur des appareils réels.
2. **Test Fermé** : Google Play exige désormais pour les nouveaux comptes personnels 20 testeurs actifs pendant 14 jours avant de pouvoir demander l'accès à la production.

## 5. Maintenance
Surveillez l'onglet **Android Vitals** après le lancement. Avec le passage au SDK 35 et Kotlin 2.1, il est crucial de surveiller les éventuels "ANR" (Application Not Responding) lors du traitement intensif des cycles de combat dans le MarsEngine.
