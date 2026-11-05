package com.example.model

enum class GenreCategory(val displayName: String) {
    ALL("All Genres"),
    EDM_DUBSTEP("EDM & Dubstep"),
    HOUSE_TECHNO("House & Techno"),
    BASS_DNB("Bass & Drum & Bass"),
    HIPHOP_TRAP("Hip-Hop & Trap"),
    POP_INDIE("Pop & Indie"),
    ROCK_METAL("Rock & Metal"),
    AMBIENT_LOFI("Ambient & Lo-Fi"),
    LATIN_AFRO("Latin & Afrobeat"),
    CINEMATIC("Cinematic & Acoustic")
}

data class MusicGenre(
    val id: String,
    val name: String,
    val category: GenreCategory,
    val typicalBpmRange: String,
    val description: String
)

object MusicGenres {
    val ALL_GENRES = listOf(
        // EDM & Dubstep
        MusicGenre("dubstep", "Dubstep", GenreCategory.EDM_DUBSTEP, "140 - 150 BPM", "Heavy sub bass, syncopated riddim, aggressive growls and halftime drops"),
        MusicGenre("melodic_dubstep", "Melodic Dubstep", GenreCategory.EDM_DUBSTEP, "140 - 145 BPM", "Emotional piano, supersaws, euphoric chords, heavy melodic drop (Seven Lions/Illenium style)"),
        MusicGenre("riddim", "Riddim", GenreCategory.EDM_DUBSTEP, "140 - 142 BPM", "Minimal repetitive sub-bass bounce, metallic square waves, heavy skank rhythm"),
        MusicGenre("tearout", "Tearout Dubstep", GenreCategory.EDM_DUBSTEP, "145 - 150 BPM", "Ultra-distorted gun basses, double drops, aggressive machine gun rhythms"),
        MusicGenre("brostep", "Brostep", GenreCategory.EDM_DUBSTEP, "140 - 145 BPM", "Metallic growls, modern electro synths, mid-bass aggression"),
        MusicGenre("future_bass", "Future Bass", GenreCategory.EDM_DUBSTEP, "130 - 160 BPM", "Detuned supersaw chords, pitch-bent vocal chops, 808 subs"),
        MusicGenre("edm", "EDM / Festival Big Room", GenreCategory.EDM_DUBSTEP, "128 - 132 BPM", "Festival mainstage anthems, energetic builds, punchy kick drops"),
        MusicGenre("trap_edm", "Festival Trap / Hybrid Trap", GenreCategory.EDM_DUBSTEP, "140 - 160 BPM", "Brass horns, hard 808s, screeching metallic drops"),

        // House & Techno
        MusicGenre("melodic_techno", "Melodic Techno", GenreCategory.HOUSE_TECHNO, "122 - 128 BPM", "Hypnotic rolling basslines, atmospheric synth arpeggios, analog warmth"),
        MusicGenre("tech_house", "Tech House", GenreCategory.HOUSE_TECHNO, "124 - 128 BPM", "Groovy 4/4 drums, infectious rolling bass, techy vocal hooks"),
        MusicGenre("deep_house", "Deep House", GenreCategory.HOUSE_TECHNO, "120 - 124 BPM", "Soulful minor chords, FM bass plucks, lush pads, warm groove"),
        MusicGenre("progressive_house", "Progressive House", GenreCategory.HOUSE_TECHNO, "126 - 130 BPM", "Emotional evolving melodies, layered saw leads, club energy"),
        MusicGenre("hard_techno", "Hard Techno / Industrial", GenreCategory.HOUSE_TECHNO, "145 - 160 BPM", "Rumbling distorted kicks, dark industrial stabs, relentless warehouse rhythm"),
        MusicGenre("slap_house", "Slap House / Brazilian Bass", GenreCategory.HOUSE_TECHNO, "124 - 126 BPM", "Plucky Reese bass, punchy commercial top-40 club energy"),

        // Bass & Drum & Bass
        MusicGenre("drum_and_bass", "Drum & Bass (Liquid)", GenreCategory.BASS_DNB, "170 - 175 BPM", "Fast breakbeats, melodic Rhodes chords, lush warm vocal stems"),
        MusicGenre("neurofunk", "Neurofunk DnB", GenreCategory.BASS_DNB, "172 - 178 BPM", "Complex modulated reese basses, technical sound design, gritty groove"),
        MusicGenre("jump_up", "Jump Up DnB", GenreCategory.BASS_DNB, "174 - 176 BPM", "Bouncing pitch-bent squeaks, punchy snares, dancefloor energy"),
        MusicGenre("midtempo", "Midtempo Bass", GenreCategory.BASS_DNB, "100 - 110 BPM", "Dark cyberpunk beats, distorted industrial growls, stomping 4/4 half-step"),

        // Hip-Hop, R&B & Trap
        MusicGenre("trap", "Hip-Hop / Trap", GenreCategory.HIPHOP_TRAP, "130 - 160 BPM", "Rolling hi-hat triplets, boomy 808 bass, dark melodic piano hooks"),
        MusicGenre("drill", "UK / NY Drill", GenreCategory.HIPHOP_TRAP, "140 - 144 BPM", "Sliding 808 glides, syncopated snare skips, haunting bells"),
        MusicGenre("boom_bap", "Boom Bap / 90s Hip Hop", GenreCategory.HIPHOP_TRAP, "85 - 95 BPM", "Vinyl crackle, sampled jazz chops, punchy acoustic breakbeats"),
        MusicGenre("rnb", "Modern R&B / Soul", GenreCategory.HIPHOP_TRAP, "80 - 110 BPM", "Smooth vocal runs, seductive chords, crisp modern percussion"),

        // Pop, Electronic & Indie
        MusicGenre("synthwave", "Synthwave / Retrowave", GenreCategory.POP_INDIE, "100 - 120 BPM", "80s gated reverbs, analog synthesizer arpeggios, nostalgic neon aesthetic"),
        MusicGenre("hyperpop", "Hyperpop / Glitchcore", GenreCategory.POP_INDIE, "140 - 180 BPM", "Pitch-shifted vocals, distorted auto-tune, chaotic maximalist beats"),
        MusicGenre("indie_pop", "Indie Pop / Bedroom Pop", GenreCategory.POP_INDIE, "95 - 120 BPM", "Jangle guitars, breezy vocals, catchy earworm hooks"),
        MusicGenre("indie_rock", "Indie Rock / Alternative", GenreCategory.ROCK_METAL, "115 - 140 BPM", "Overdriven live guitars, acoustic drum kits, heartfelt indie vocals"),
        MusicGenre("metalcore", "Metalcore / Modern Metal", GenreCategory.ROCK_METAL, "130 - 160 BPM", "Down-tuned 7-string riffs, heavy breakdowns, screaming & clean vocals"),

        // Ambient, Lo-Fi & Global
        MusicGenre("lofi_hiphop", "Lo-Fi Hip Hop", GenreCategory.AMBIENT_LOFI, "75 - 85 BPM", "Detuned cassette tape flutter, jazzy piano, study and relaxation beats"),
        MusicGenre("ambient", "Ambient / Chillout", GenreCategory.AMBIENT_LOFI, "60 - 90 BPM", "Drone soundscapes, evolving textures, meditation and relaxation"),
        MusicGenre("afrobeats", "Afrobeats", GenreCategory.LATIN_AFRO, "100 - 108 BPM", "Syncopated log drums, joyous guitar riffs, infectious West African rhythm"),
        MusicGenre("reggaeton", "Reggaeton / Latin Urban", GenreCategory.LATIN_AFRO, "90 - 100 BPM", "Dembow drum groove, dancefloor rhythm, tropical synthesis"),
        MusicGenre("cinematic", "Cinematic / Film Score", GenreCategory.CINEMATIC, "Var. BPM", "Orchestral brass, strings, hybrid trailer sound design")
    )

    val POPULAR_GENRES = listOf(
        "All",
        "Dubstep",
        "Melodic Dubstep",
        "EDM",
        "Riddim",
        "Tech House",
        "Melodic Techno",
        "Drum & Bass",
        "Synthwave",
        "Trap",
        "Future Bass",
        "Lo-Fi Hip Hop",
        "Indie Pop",
        "Afrobeats"
    )
}
