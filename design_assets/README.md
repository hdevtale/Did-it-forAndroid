# Design Assets

This folder contains high-resolution design assets for the Did-It app that are **NOT** compiled into the final APK.

## Contents

- **app_logo_1024x1024.png** - High-resolution source logo (1024x1024) for generating adaptive launcher icons

## Purpose

This folder serves as a workspace for:
- Source design files
- High-resolution assets
- Design mockups
- Icon generation source files
- Other design-related materials

## Important Notes

- Files in this folder are **NOT** included in the app compilation
- This folder is **NOT** copied to the final APK
- Use this folder to store source files before processing them for app use
- Generated app icons should be placed in `app/src/main/res/mipmap-*` folders

## Workflow

1. Place high-resolution source files here
2. Process/generate app-specific assets (icons, drawables, etc.)
3. Copy generated assets to appropriate `res` folders
4. Keep source files here for future modifications
