# Menu asset provenance
Built-in image_gen used; approved concept 2 is the reference.
Lit background: exec-f3c0e1a1-8dcb-44c0-b3d7-2498dcd140bf.png
Prompt: Preserve exact approved composition, aged laboratory, clock, lamp and rain window. Remove all left UI/title/button text and lines, reconstruct dark plaster wall. Remove only clock hands and central hub while retaining numbers/ticks. No other redesign. Full production background plate.
Unlit background: exec-0768a41c-ef55-459a-9306-44293f7f7b3e.png
Prompt: Lighting-only edit, turn off upper-right lamp and its warm illumination, retain faint blue window light and readable dial. Preserve all objects, camera, framing and empty dial; no text/hands/new objects. Align to lit background for crossfade.
Runtime assets: src/assets/images/menu/menu_background.png and menu_unlit.png.
Menu text, hands, rain, camera movement, lighting blend and glass fractures are Java2D code layers.
Audio: original procedural thunder and glass synthesis, existing game rain ambience.


v29: ClockGlassEffect renders the approved irregular glass fractures in Java2D. Two cached transparent layers; localized central microcracks and subdued edge reflections. No external fracture photo, GIF playback or detached shards. Legacy clock_fracture.png removed from this version.
