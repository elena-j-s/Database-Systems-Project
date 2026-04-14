Track photos for Race Selector cards
=====================================

Put one image per race, named by F1 round number (same as racesData / CSV Round):

  frontend/public/asset/tracks/1.jpg   → Australian Grand Prix
  frontend/public/asset/tracks/2.jpg   → Chinese Grand Prix
  …
  frontend/public/asset/tracks/24.jpg  → Abu Dhabi Grand Prix

URLs in dev: /asset/tracks/1.jpg etc. (Vite copies this folder into dist/ on build.)

Format: .jpg only (see trackPhotoUrl in src/racesData.ts). If a file is missing or fails to load, the card shows the colored gradient instead.

Optional: change trackPhotoUrl to use .png or a different folder name.
