package com.example.data.repository

import com.example.R
import com.example.data.model.LyricLine
import com.example.data.model.Track

object TrackRepository {

    private val allTracks = listOf(
        Track(
            id = "t_mi_vicio",
            title = "Mi Vicio (Remix)",
            artist = "Brytiago, Luar La L",
            album = "Mi Vicio (Remix)",
            durationMs = 284285L,
            contentUriString = "asset:///mi_vicio_remix.flac",
            artworkResId = R.drawable.album_mi_vicio,
            audioResId = 0,
            filePath = "public/assets/aistudio/Mi_Vicio__Remix__-_Brytiago__Luar_La_L_69ecb4d7.flac",
            mimeType = "audio/flac",
            codec = "FLAC",
            sampleRateHz = 44100,
            bitDepth = 16,
            bitrateBps = 1149000L,
            isLossless = true,
            qualityFormat = "Lossless • FLAC • 16-bit • 44.1 kHz",
            category = "Urbano Latino",
            playCount = "8.4M",
            primaryAccent = 0xFFF43F5E,
            secondaryAccent = 0xFF00E5FF,
            lyrics = listOf(
                LyricLine(10490L, "Por el humo y el alcohol"),
                LyricLine(13120L, "Siempre me confundes cuando discutes, pero cuando te meto nos va mejor"),
                LyricLine(18780L, "Cuando nunca te lo saco y me gritas que te dé má'"),
                LyricLine(21450L, "Y hace' que perdamo' el control, oh-oh"),
                LyricLine(26320L, "De mis decisiones fuiste la peor"),
                LyricLine(29210L, "Pero al estar dentro de ti"),
                LyricLine(30520L, "Y sentir el calor y el aroma de tu olor"),
                LyricLine(33190L, "De ese cuerpo quiero más, ah"),
                LyricLine(37580L, "Sabes que nunca en mi vida te he vuelto a fallar"),
                LyricLine(40350L, "Que te jalo por el pelo y tú por el collar"),
                LyricLine(43140L, "Aprovecha esta oportunidad, que me va' a extrañar, ay"),
                LyricLine(48810L, "Cuando no te moje' porque no te sepan tocar"),
                LyricLine(53250L, "Te va' a quedar sin alguien cuando quiera' buscarme"),
                LyricLine(58600L, "Si tú sabe' que estoy pa' ti, ¿por qué quiere' usarme?, ey"),
                LyricLine(64480L, "Ese es mi vicio por ti, que tu cuerpo es un arte"),
                LyricLine(68660L, "Y se te va a hacer tarde"),
                LyricLine(70880L, "Por hijo e' puta' como tú en el amor soy un cobarde"),
                LyricLine(76039L, "Hello, papacito"),
                LyricLine(78080L, "Indica, baby, yo le llego (indica)"),
                LyricLine(79920L, "Pero solo pa' chichar, no veo amore', yo ando ciego (jaja)"),
                LyricLine(82650L, "Acabo 'e prender un Phillie"),
                LyricLine(83760L, "En Carola por la D'Diego (la L)"),
                LyricLine(85410L, "Y tu jevo es trili"),
                LyricLine(86150L, "Está capeando die' week en el punto (¿qué?)"),
                LyricLine(88550L, "Yo no creo en amore' (no)"),
                LyricLine(89530L, "Yo las quiero a toa' en conjunto (de una)"),
                LyricLine(91280L, "Y si tienen el booty grande"),
                LyricLine(92440L, "En el rating les subo punto' (diablo)"),
                LyricLine(94390L, "Hay muchos que te tiran, pero no hacen uno junto"),
                LyricLine(96620L, "Y tú te haces, pero conozco tu punto G"),
                LyricLine(99840L, "Tú sabe', no te haga', que soy el que te penetra, jaja (¿qué?)"),
                LyricLine(102979L, "Aprovéchame, que se está haciendo tarde y ya me voy (y ya me voy)"),
                LyricLine(107950L, "Ven, dale trépate, te la vo'a echar adentro"),
                LyricLine(110430L, "Pa' eso es que te puse el call"),
                LyricLine(111630L, "Caché una Perco así que, baby, por ahí voy"),
                LyricLine(113880L, "En una dosi', ya partí la oxy"),
                LyricLine(116650L, "De sexo ya hay sobredosis, sí"),
                LyricLine(119850L, "Ya vo'a aplicártela, ya tengo par de posicione'"),
                LyricLine(122670L, "No hagas pregunta', mami, a mí no me cuestione'"),
                LyricLine(125720L, "(Yo no me canso y siempre)"),
                LyricLine(127310L, "De ese cuerpo quiero má', ah"),
                LyricLine(131610L, "Sabe' que nunca en la vida te he vuelto a fallar"),
                LyricLine(134540L, "Que te jalo por el pelo y tú por el collar"),
                LyricLine(137060L, "Aprovecha esta oportunidad, que me va' a extrañar, ah"),
                LyricLine(143020L, "Cuando no te moje' porque no te sepan tocar"),
                LyricLine(147329L, "Te va' a quedar sin alguien cuando quiera' buscarme"),
                LyricLine(152930L, "Si tú sabe' que estoy pa' ti, ¿por qué quiere' usarme?, ey"),
                LyricLine(158710L, "Ese es mi vicio por ti, que tu cuerpo es un arte"),
                LyricLine(163170L, "Y se te va a hacer tarde"),
                LyricLine(165220L, "Por hijo e' puta' como tú en el amor soy un cobarde, ah"),
                LyricLine(169130L, "Mi corazón dejaste seco"),
                LyricLine(171530L, "Como Iron Man, me dejaste un hueco"),
                LyricLine(174480L, "Ahora quiero a otra montada en el Jetsko"),
                LyricLine(176850L, "Quédate con tu orgullo y con las Retro"),
                LyricLine(178990L, "Porque to' lo que me hace' e' maligno"),
                LyricLine(182640L, "Y me fijé tanto en complacerte"),
                LyricLine(184690L, "Que ya no me conozco ni a mí mismo"),
                LyricLine(188370L, "Tú tienes problema' de actitudes"),
                LyricLine(190780L, "Y yo tengo problemas de alcoholismo"),
                LyricLine(193280L, "Tú borras los mensajes, por eso me enchismo, ey"),
                LyricLine(196900L, "Así que quédate en el viaje"),
                LyricLine(199730L, "Y con respeto, cágate en tu madre"),
                LyricLine(202170L, "Que yo me voy pa' la disco con mis anormale'"),
                LyricLine(204760L, "Ando detrás de ese culo"),
                LyricLine(205860L, "Y tú detrás de la wallet, ey"),
                LyricLine(208910L, "Por el humo y el alcohol"),
                LyricLine(211640L, "Siempre me confunde' cuando discutes"),
                LyricLine(213920L, "Pero cuando le meto nos va mejor"),
                LyricLine(217370L, "Cuando nunca te lo saco y me gritas que te dé más"),
                LyricLine(220010L, "Y hace' que perdamo' el control, oh"),
                LyricLine(225150L, "De mis decisiones fuiste la peor, pero"),
                LyricLine(227780L, "Al estar dentro de ti y sentir el calor y el aroma de tu olor"),
                LyricLine(231730L, "De ese cuerpo quiero má', ah"),
                LyricLine(236090L, "Sabe' que nunca en mi vida te he vuelto a fallar"),
                LyricLine(238850L, "Que te jalo por el pelo y tú por el collar"),
                LyricLine(241460L, "Aprovecha esta oportunidad, que me va' a extrañar, ay"),
                LyricLine(247210L, "Cuando no te moje' porque no te sepan tocar"),
                LyricLine(251680L, "Te va' a quedar sin alguien cuando quiera' buscarme"),
                LyricLine(257070L, "Si tú sabe' que estoy pa' ti, ¿por qué quiere' usarme?, ey"),
                LyricLine(263090L, "Ese es mi vicio por ti, que tu cuerpo es un arte"),
                LyricLine(267010L, "Y se te va a hacer tarde"),
                LyricLine(269600L, "Por hijo e' puta' como tú en el amor soy un cobarde")
            )
        ),
        Track(
            id = "t1",
            title = "Neon Pulse",
            artist = "Cyberdyne",
            album = "Neon City",
            durationMs = 31000L,
            artworkResId = R.drawable.album_neon_city,
            audioResId = R.raw.track_neon_pulse,
            qualityFormat = "Lossless 24-Bit / 96kHz",
            category = "Cyberpunk",
            playCount = "242.1K",
            primaryAccent = 0xFFB056FE,
            secondaryAccent = 0xFF00E5FF,
            lyrics = listOf(
                LyricLine(0L, "⚡ [Intro - Síntesis analógica iniciando]"),
                LyricLine(3500L, "Luces de neón reflejadas en el asfalto"),
                LyricLine(7500L, "El pulso eléctrico despierta toda la ciudad"),
                LyricLine(11500L, "Sombras veloces cruzando la avenida nocturna"),
                LyricLine(15500L, "Frecuencias altas que rompen el silencio"),
                LyricLine(19500L, "Energía pura en cada respiración sincronizada"),
                LyricLine(23500L, "El ritmo digital nunca se detiene"),
                LyricLine(27500L, "✦ [Outro - Resonancia en C menor]")
            )
        ),
        Track(
            id = "t2",
            title = "Night Runners",
            artist = "Synthwave Dreams",
            album = "Retro Horizon",
            durationMs = 34000L,
            artworkResId = R.drawable.album_midnight_drive,
            audioResId = R.raw.track_synthwave_dreams,
            qualityFormat = "Lossless 24-Bit / 192kHz",
            category = "Synthwave",
            playCount = "189.5K",
            primaryAccent = 0xFFFF2A85,
            secondaryAccent = 0xFFB056FE,
            lyrics = listOf(
                LyricLine(0L, "🌅 [Swell de sintetizador de ensueño]"),
                LyricLine(4200L, "Carretera infinita bajo un cielo púrpura y magenta"),
                LyricLine(8500L, "El gran sol retro iluminando el horizonte cálido"),
                LyricLine(13000L, "Acelerando hacia recuerdos de los ochenta"),
                LyricLine(17500L, "La brisa marina trae viejas melodías guardadas"),
                LyricLine(22000L, "Luces de freno dibujando estelas incandescentes"),
                LyricLine(26500L, "Viajando sin destino, solo tú y el camino"),
                LyricLine(31000L, "✨ [Arpegio analógico final]")
            )
        ),
        Track(
            id = "t3",
            title = "Ethereal Echoes",
            artist = "Lumina Void",
            album = "Cosmic Waves",
            durationMs = 39000L,
            artworkResId = R.drawable.album_cosmic_waves,
            audioResId = R.raw.track_ethereal_echoes,
            qualityFormat = "Hi-Res Studio Master",
            category = "Ambient Space",
            playCount = "315.8K",
            primaryAccent = 0xFF00E5FF,
            secondaryAccent = 0xFFB056FE,
            lyrics = listOf(
                LyricLine(0L, "🌌 [Ondas cósmicas en el vacío estelar]"),
                LyricLine(4500L, "Resonancia estelar entre nebulosas lejanas"),
                LyricLine(9500L, "Ondas de luz atravesando la curvatura espacial"),
                LyricLine(14500L, "Gravedad cero, quietud y silencio absoluto"),
                LyricLine(19500L, "Polvo de estrellas brillando en tonos violetas"),
                LyricLine(24500L, "Conectando constelaciones dentro de la mente"),
                LyricLine(29500L, "El eco eterno del universo infinito"),
                LyricLine(34500L, "✦ [Disolución en el horizonte de sucesos]")
            )
        ),
        Track(
            id = "t4",
            title = "Cyber Drift",
            artist = "Vector Seven",
            album = "Neon City",
            durationMs = 31000L,
            artworkResId = R.drawable.album_neon_city,
            audioResId = R.raw.track_neon_pulse,
            qualityFormat = "Lossless 24-Bit / 96kHz",
            category = "Cyberpunk",
            playCount = "94.2K",
            primaryAccent = 0xFF00E5FF,
            secondaryAccent = 0xFFFFB800,
            lyrics = listOf(
                LyricLine(0L, "🏎️ [Arranque de motor electro-magnético]"),
                LyricLine(4000L, "Curvas cerradas entre rascacielos gigantes"),
                LyricLine(8500L, "Hologramas parpadean en la niebla ácida"),
                LyricLine(13500L, "Neumáticos quemando el asfalto mojado"),
                LyricLine(18500L, "Sobrecarga de turbo en el sector siete"),
                LyricLine(23500L, "Adrenalina pura a trescientos por hora"),
                LyricLine(28000L, "🏁 [Corte de inyección synth]")
            )
        ),
        Track(
            id = "t5",
            title = "Midnight Horizon",
            artist = "Kavinsky Wave",
            album = "Retro Horizon",
            durationMs = 34000L,
            artworkResId = R.drawable.album_midnight_drive,
            audioResId = R.raw.track_synthwave_dreams,
            qualityFormat = "Lossless 24-Bit / 192kHz",
            category = "Retrowave",
            playCount = "156.0K",
            primaryAccent = 0xFFFF2A85,
            secondaryAccent = 0xFF00E5FF,
            lyrics = listOf(
                LyricLine(0L, "🌃 [Medianoche en la costa oeste]"),
                LyricLine(5000L, "Las palmeras mecidas por el viento cálido"),
                LyricLine(10000L, "Cintas de casete girando en el estéreo"),
                LyricLine(15000L, "Bajo analógico marcando el tempo perfecto"),
                LyricLine(20000L, "El reflejo de la luna en los cristales tintados"),
                LyricLine(25000L, "Viviendo en una postal de 1986"),
                LyricLine(30000L, "🌴 [Fade out nostálgico]")
            )
        ),
        Track(
            id = "t6",
            title = "Astral Journey",
            artist = "Solar Flare",
            album = "Cosmic Waves",
            durationMs = 39000L,
            artworkResId = R.drawable.album_cosmic_waves,
            audioResId = R.raw.track_ethereal_echoes,
            qualityFormat = "Hi-Res Studio Master",
            category = "Ambient",
            playCount = "88.7K",
            primaryAccent = 0xFFB056FE,
            secondaryAccent = 0xFFFFB800,
            lyrics = listOf(
                LyricLine(0L, "🪐 [Navegación orbital activada]"),
                LyricLine(5000L, "Dejando atrás la atmósfera terrestre"),
                LyricLine(10500L, "Anillos de hielo girando en cámara lenta"),
                LyricLine(16000L, "Pulsos de cuásares a millones de años luz"),
                LyricLine(21500L, "La paz suprema del cosmos infinito"),
                LyricLine(27000L, "Flotando en la inmensidad del espacio tiempo"),
                LyricLine(33000L, "✨ [Transición a hiperespacio]")
            )
        )
    )

    fun getTracks(): List<Track> = allTracks

    fun getQuickPicks(): List<Track> = allTracks.take(4)

    fun getTrackById(id: String): Track? = allTracks.find { it.id == id }

    fun searchTracks(query: String): List<Track> {
        if (query.isBlank()) return allTracks
        val lower = query.trim().lowercase()
        return allTracks.filter {
            it.title.lowercase().contains(lower) ||
            it.artist.lowercase().contains(lower) ||
            it.album.lowercase().contains(lower) ||
            it.category.lowercase().contains(lower)
        }
    }
}
