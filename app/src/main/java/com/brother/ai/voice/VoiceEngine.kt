package com.brother.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Human-like voice output engine.
 * Voice categories change pitch/speed profiles. TTS engine of the device
 * provides the neural voices (Google TTS recommended).
 */
class VoiceEngine(context: Context) {

    enum class VoiceCategory(val label: String, val pitch: Float, val rate: Float) {
        MALE("Male", 0.85f, 1.0f),
        FEMALE("Female", 1.25f, 1.0f),
        FRIENDLY("Friendly", 1.1f, 1.05f),
        PROFESSIONAL("Professional", 1.0f, 0.95f),
        CALM("Calm", 0.95f, 0.85f),
        ENERGETIC("Energetic", 1.3f, 1.2f),
        DEEP("Deep", 0.7f, 0.9f),
        YOUNG("Young", 1.4f, 1.1f),
        MATURE("Mature", 0.9f, 0.88f),
        ROBOTIC("Robotic", 0.5f, 1.3f),
        FUTURISTIC("Futuristic", 1.15f, 1.15f)
    }

    private var tts: TextToSpeech? = null
    private var ready = false
    private var onDone: (() -> Unit)? = null
    @set:JvmName("setCurrentCategory")
    var category: VoiceCategory = VoiceCategory.MALE
    var langTag: String = "bn-BD"   // bn-BD / en-US / hi-IN

    private val pending = StringBuilder()

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) { onDone?.invoke() }
                    override fun onError(utteranceId: String?) { onDone?.invoke() }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?, errorCode: Int) { onDone?.invoke() }
                    override fun onStart(utteranceId: String?) {}
                })
                applyVoice()
                if (pending.isNotEmpty()) {
                    speakRaw(pending.toString())
                    pending.clear()
                }
            }
        }
    }

    /** Called when the current utterance finishes (or fails) — used for auto-listening. */
    fun setOnDoneListener(l: (() -> Unit)?) { onDone = l }

    private fun locale(): Locale = try {
        Locale.forLanguageTag(langTag)
    } catch (e: Exception) {
        Locale.US
    }

    private fun applyVoice() {
        tts?.let {
            it.language = locale()
            it.setPitch(category.pitch)
            it.setSpeechRate(category.rate)
        }
    }

    fun speak(text: String) {
        if (ready) speakRaw(text) else pending.append(text)
    }

    private fun speakRaw(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "brother_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }

    fun setCategory(c: VoiceCategory) {
        category = c
        if (ready) applyVoice()
    }
}