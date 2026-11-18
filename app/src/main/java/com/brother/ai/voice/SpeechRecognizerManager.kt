package com.brother.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Always-ready speech recognition. Supports bn-BD, en-US, hi-IN.
 * Partial results stream live so the dashboard waveform reacts instantly.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var recognizer: SpeechRecognizer? = null
    var languageTag: String = "bn-BD"

    fun startListening() {
        stop()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
            startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
            )
        }
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()?.let(onPartial)
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()?.let(onFinal)
        }

        override fun onError(error: Int) {
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "কিছু শুনতে পেলাম না, আবার বলুন"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "সময় শেষ, আবার চেষ্টা করুন"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission দরকার"
                else -> "Error code: $error"
            }
            onError(msg)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}