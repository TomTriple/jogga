package de.tomhoefer.jogga.components

import java.util.Locale 
import android.speech.tts.TextToSpeech 
import android.content.Context 


/**
 * Klasse fuer die Ausfuerhung von Sprachsynthese 
 * @author tom
 *
 */
class Speecher(context:Context) {
 
	require(context != null, "context must not be null")
	
	private var prepared = false
	private var ttsInit = false
	
	private val tts = new TextToSpeech(context, new TextToSpeech.OnInitListener {
		def onInit(status:Int) = {
			if(status == 0) {
				ttsInit = true 
			}
		}
	})
	
	protected def prepare() = { 
		tts.setSpeechRate(1.25f)  
		if(tts.isLanguageAvailable(Locale.GERMAN) >= 0) 
			tts setLanguage Locale.GERMAN
		else
			tts.setLanguage(Locale.US) 
	}
	
	/**
	 * Spricht den Satz `text`
	 * @param text String der gesprochen werden soll
	 * @return Unit 
	 */
	def speak(text:String) = if(ttsInit) { 
		if(!prepared) {
			prepare()
			prepared = true
		} 	  
		tts.speak(text, TextToSpeech.QUEUE_ADD, null) 
	}
	
}