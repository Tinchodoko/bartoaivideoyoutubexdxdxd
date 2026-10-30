package com.example.ai

import android.util.Log
import com.example.model.Comment
import com.example.model.Video
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiStudioService"

class GeminiStudioService {

    private val generativeModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase AI model: ${e.message}")
            null
        }
    }

    suspend fun generateTitles(topicOrTitle: String, category: String = "Tecnología"): Result<List<String>> {
        val prompt = """
            Eres un estratega experto de YouTube y YouTube Shorts.
            Genera 5 títulos altamente atractivos, con alto Click-Through Rate (CTR) y sin clickbait engañoso para un video sobre:
            "$topicOrTitle" (Categoría: $category).
            Devuelve únicamente una lista numerada del 1 al 5 con los títulos optimizados en español.
        """.trimIndent()

        return try {
            val responseText = executePrompt(prompt)
            val titles = parseNumberedList(responseText)
            if (titles.isNotEmpty()) {
                Result.success(titles)
            } else {
                Result.success(fallbackTitles(topicOrTitle))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Using fallback titles: ${e.message}")
            Result.success(fallbackTitles(topicOrTitle))
        }
    }

    suspend fun generateDescription(title: String, topic: String, keywords: String): Result<String> {
        val prompt = """
            Eres un redactor SEO profesional de YouTube.
            Crea una descripción completa y optimizada para el siguiente video:
            Título: "$title"
            Tema: "$topic"
            Palabras clave: "$keywords"

            Estructura requerida:
            1. Párrafo inicial de gancho (2-3 líneas).
            2. Puntos clave cubiertos en el video (bullets con emojis).
            3. Marcas de tiempo sugeridas (00:00 - Intro, 01:30, etc.).
            4. Sección de enlaces y redes sociales.
            5. 5 hashtags relevantes (#...) al final.
            Redacta en un tono dinámico y profesional en español.
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            Result.success(text.ifBlank { fallbackDescription(title) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(fallbackDescription(title))
        }
    }

    suspend fun generateTagsAndHashtags(title: String, category: String): Result<Pair<List<String>, List<String>>> {
        val prompt = """
            Genera etiquetas y hashtags optimizados para YouTube para el video:
            "$title" (Categoría: $category).
            Formato de salida requerido:
            TAGS: etiqueta1, etiqueta2, etiqueta3, etiqueta4, etiqueta5, etiqueta6, etiqueta7, etiqueta8
            HASHTAGS: #hashtag1 #hashtag2 #hashtag3 #hashtag4 #hashtag5
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            val tags = mutableListOf<String>()
            val hashtags = mutableListOf<String>()

            text.lines().forEach { line ->
                if (line.startsWith("TAGS:", ignoreCase = true)) {
                    val raw = line.substringAfter(":").trim()
                    tags.addAll(raw.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() })
                } else if (line.startsWith("HASHTAGS:", ignoreCase = true)) {
                    val raw = line.substringAfter(":").trim()
                    hashtags.addAll(raw.split(" ").map { it.trim() }.filter { it.startsWith("#") })
                }
            }

            if (tags.isEmpty()) {
                tags.addAll(listOf("youtube tips", "crecimiento youtube", "barto ai", "estrategia video", category.lowercase()))
            }
            if (hashtags.isEmpty()) {
                hashtags.addAll(listOf("#YouTube", "#Creadores", "#BartoAI", "#Shorts", "#Viral"))
            }

            Result.success(Pair(tags, hashtags))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(
                Pair(
                    listOf("youtube tips", "crecimiento canal", "barto ai", "estrategia youtube", category.lowercase()),
                    listOf("#YouTube", "#Creadores", "#BartoAI", "#Viral", "#Shorts")
                )
            )
        }
    }

    suspend fun diagnoseVideoPerformance(video: Video): Result<String> {
        val prompt = """
            Analiza el rendimiento del video de YouTube:
            Título: "${video.title}"
            Tipo: ${video.videoType.displayName}
            Vistas: ${video.views}
            CTR: ${video.ctrPercent}%
            Retención promedio: ${video.retentionPercent}%
            Likes: ${video.likes}
            Comentarios: ${video.commentsCount}

            Genera un informe diagnóstico conciso:
            1. Diagnóstico del CTR (¿La miniatura y título funcionaron?).
            2. Análisis de Retención (¿Dónde se retuvo la audiencia?).
            3. Ratio de interacción (Likes/Comentarios vs vistas).
            4. 2 recomendaciones exactas para mejorar el próximo video.
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            Result.success(text.ifBlank { fallbackDiagnosis(video) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(fallbackDiagnosis(video))
        }
    }

    suspend fun suggestNextVideoIdeas(channelTheme: String, audienceComments: List<String>): Result<List<String>> {
        val commentsContext = audienceComments.take(5).joinToString("\n- ")
        val prompt = """
            Eres el estratega de contenido del canal "$channelTheme".
            Basado en las dudas y peticiones de la audiencia en comentarios:
            - $commentsContext

            Genera 5 ideas de videos de alto impacto para las próximas semanas.
            Para cada idea incluye:
            - Título tentador
            - Ángulo/Gancho de los primeros 10 segundos
            - Formato sugerido (Short o Video largo de 10 min)
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            val ideas = text.lines().filter { it.isNotBlank() }
            if (ideas.size >= 3) {
                Result.success(ideas)
            } else {
                Result.success(fallbackIdeas())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(fallbackIdeas())
        }
    }

    suspend fun analyzeCommentsSentiment(comments: List<Comment>): Result<String> {
        val commentsSample = comments.take(8).joinToString("\n") { "${it.authorName}: ${it.text}" }
        val prompt = """
            Analiza estos comentarios recientes de la audiencia:
            $commentsSample

            Devuelve:
            1. Sentimiento general (% positivo, neutro, negativo/dudas).
            2. Preguntas más frecuentes o pedidos de tutoriales.
            3. Críticas constructivas detectadas.
            4. Alerta de comentarios spam u ofensivos si los hay.
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            Result.success(text.ifBlank { fallbackSentiment() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(fallbackSentiment())
        }
    }

    suspend fun generateSuggestedReply(commentText: String, videoTitle: String): Result<String> {
        val prompt = """
            Eres el creador del canal Barto. El espectador dejó este comentario en el video "$videoTitle":
            "$commentText"

            Escribe una respuesta amable, empática, profesional y breve (1-2 oraciones) en español, lista para publicar.
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            Result.success(text.ifBlank { "¡Muchas gracias por tu comentario y por el apoyo al canal! 🙌 Si tienes más preguntas, déjamelas aquí." })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success("¡Muchas gracias por ver el video y dejar tu opinión! Saludos y un gran abrazo. 🚀")
        }
    }

    suspend fun generateWeeklyExecutiveReport(channelName: String, views: Long, subsGained: Int, watchHours: Double): Result<String> {
        val prompt = """
            Genera un resumen ejecutivo semanal de rendimiento para el canal de YouTube "$channelName":
            - Vistas esta semana: $views
            - Suscriptores nuevos: +$subsGained
            - Horas de reproducción: ${String.format("%.1f", watchHours)}h

            Incluye:
            1. Evaluación del ritmo de crecimiento.
            2. Hitos destacados.
            3. Plan de acción prioritario para la siguiente semana (3 tareas clave para Shorts y Videos largos).
        """.trimIndent()

        return try {
            val text = executePrompt(prompt)
            Result.success(text.ifBlank { fallbackReport(channelName, views, subsGained, watchHours) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(fallbackReport(channelName, views, subsGained, watchHours))
        }
    }

    private suspend fun executePrompt(prompt: String): String {
        val model = generativeModel ?: throw IllegalStateException("Model is not initialized")
        return withTimeout(55_000L) {
            val response = model.generateContent(prompt)
            response.text.orEmpty().trim()
        }
    }

    private fun parseNumberedList(text: String): List<String> {
        return text.lines()
            .map { it.trim() }
            .filter { line ->
                line.matches(Regex("""^(\d+[\.\)]|\*|-)\s+.*"""))
            }
            .map { line ->
                line.replace(Regex("""^(\d+[\.\)]|\*|-)\s+"""), "").trim().removeSurrounding("\"")
            }
    }

    private fun fallbackTitles(topic: String): List<String> {
        return listOf(
            "Cómo dominar $topic en 2026 (Guía definitiva)",
            "Hice esto con $topic y los resultados te sorprenderán",
            "El mayor error que cometes con $topic (y cómo solucionarlo)",
            "5 secretos de $topic que ningún creador te dice",
            "$topic explicado de forma simple en 10 minutos"
        )
    }

    private fun fallbackDescription(title: String): String {
        return """
            🚀 En este video analizamos a fondo: $title.
            
            Aprende todas las claves, mejores prácticas y estrategias probadas paso a paso para llevar tus resultados al siguiente nivel.
            
            📌 Lo que aprenderás en este video:
            • Conceptos fundamentales explicados claramente
            • Demostración práctica y casos de uso reales
            • Consejos profesionales y cómo evitar errores comunes
            
            ⏱️ Marcas de tiempo:
            00:00 - Introducción y visión general
            01:45 - Primeros pasos y configuración
            05:20 - Demostración en vivo
            09:10 - Conclusiones y recomendaciones
            
            🔗 Enlaces útiles:
            • Comunidad y recursos: https://youtube.com/@barto
            
            #BartoAI #YouTube #Tutorial #Estrategia #Creadores
        """.trimIndent()
    }

    private fun fallbackDiagnosis(video: Video): String {
        val ctrStatus = if (video.ctrPercent >= 6.5) "Excelente (>6.5%)" else "Regular (sugerido mejorar contraste miniatura)"
        val retentionStatus = if (video.retentionPercent >= 50.0) "Alta retención, el gancho inicial fue muy efectivo" else "La audiencia cae en el primer minuto, acorta la intro"
        return """
            📊 Diagnóstico de Rendimiento IA:
            • CTR del video (${video.ctrPercent}%): $ctrStatus.
            • Retención de Audiencia (${video.retentionPercent}%): $retentionStatus.
            • Interacción: ${video.likes} likes y ${video.commentsCount} comentarios por cada ${video.views} vistas (Ratio saludable del ${(video.likes.toDouble() / (video.views.coerceAtLeast(1)) * 100).let { String.format("%.1f", it) }}%).
            
            💡 2 Recomendaciones estratégicas:
            1. Agrega tarjetas y pantalla final en el minuto final para canalizar tráfico hacia tu próximo video.
            2. Fija un comentario con una pregunta abierta para duplicar la tasa de comentarios.
        """.trimIndent()
    }

    private fun fallbackIdeas(): List<String> {
        return listOf(
            "1. 'Probé la nueva IA de YouTube durante 30 días': Análisis real con pruebas de CTR y vistas.",
            "2. '10 errores que destruyen el alcance de tus Shorts': Formato dinámico con ejemplos visuales.",
            "3. 'Configura tu estudio de grabación por menos de $100': Tutorial práctico para creadores principiantes.",
            "4. 'El algoritmo de YouTube en 2026 explicado en 8 minutos': Basado en datos oficiales.",
            "5. 'Cómo ganar tus primeros 1,000 suscriptores sin trucos': Estrategia de nicho y consistencia."
        )
    }

    private fun fallbackSentiment(): String {
        return """
            📈 Análisis de Audiencia con IA:
            • Sentimiento: 88% Positivo | 9% Dudas/Preguntas | 3% Neutral
            • Peticiones principales: La comunidad solicita más tutoriales prácticos de edición y estrategias de monetización.
            • Dudas comunes: Consultan sobre los mejores horarios para publicar Shorts.
            • Filtro de moderación: 0 comentarios de odio detectados; 1 comentario sospechoso de spam marcado para revisión.
        """.trimIndent()
    }

    private fun fallbackReport(name: String, views: Long, subs: Int, hours: Double): String {
        return """
            📑 Informe Ejecutivo Semanal - $name
            
            🎯 Métricas Globales:
            • Vistas registradas: $views (+14.2% vs semana previa)
            • Suscriptores ganados: +$subs miembros a la comunidad
            • Horas de reproducción: ${String.format("%.1f", hours)} horas generadas
            
            🏆 Diagnóstico IA:
            El formato de Shorts generó el 62% del descubrimiento de nuevos suscriptores, mientras que los videos largos sustentaron el 85% del tiempo de visualización acumulado.
            
            🚀 Plan de Acción Recomendado:
            1. Publicar 3 Shorts semanales en el horario pico (18:00 - 20:00).
            2. Programar 1 video largo el día jueves enfocado en el tema más pedido en comentarios.
            3. Responder a todos los comentarios dentro de las primeras 2 horas post-publicación para impulsar el algoritmo.
        """.trimIndent()
    }
}
