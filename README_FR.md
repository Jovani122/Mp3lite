# MP3 Player Android — compilation automatique sur GitHub

Ce projet Kotlin contient un lecteur audio local Android. Le dépôt inclut un workflow GitHub Actions qui compile un APK debug et le publie comme artefact téléchargeable.

## Compiler sans Android Studio (depuis un téléphone)

1. Décompresse `MP3Player_GitHub_AutoBuild.zip` sur ton téléphone.
2. Ouvre https://github.com et connecte-toi.
3. Crée un nouveau dépôt, par exemple `MP3Player-Android`.
4. Dans le dépôt, choisis **Add file → Upload files**.
5. Ouvre le dossier `MP3Player_Android_Project` décompressé et envoie **tout son contenu** à la racine du dépôt. Il faut notamment envoyer le dossier caché `.github` avec `workflows/android-apk.yml`.
6. Valide avec **Commit changes**.
7. Va dans l'onglet **Actions**, ouvre le workflow **Build Android APK**, puis attends la fin du travail.
8. Quand le workflow affiche une réussite, ouvre son exécution et télécharge l'artefact **MP3Player-debug-APK**.
9. Décompresse l'artefact téléchargé : tu y trouveras `app-debug.apk`. Ouvre-le sur ton Android pour l'installer. Si Android le demande, autorise l'installation depuis cette source.

## Important

- Ne téléverse pas uniquement le fichier ZIP dans GitHub : il faut téléverser les fichiers et dossiers décompressés.
- Le dossier `.github` est souvent caché dans les gestionnaires de fichiers Android. Active l'affichage des fichiers cachés ou utilise une application d'archive qui affiche les fichiers commençant par un point.
- Le workflow utilise Gradle 8.9, JDK 17 et Android SDK 35 dans un runner GitHub Actions. Aucun Android Studio n'est nécessaire sur ton téléphone.
- Cet artefact est un APK **debug**, adapté aux essais personnels. Pour publier l'application, il faudra préparer une version release signée avec une clé privée que tu conserves en sécurité.
- La compilation n'a pas été exécutée dans cet environnement. Le workflow est configuré, mais GitHub doit effectuer la première compilation; si une erreur apparaît dans Actions, partage le journal pour correction.
- Le projet reste un prototype fonctionnel à valider sur appareil : permissions audio, lecture et comportement en arrière-plan doivent être testés sur ta version Android.
