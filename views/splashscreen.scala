package de.tomhoefer.jogga.views

import android.app.Activity
import android.view.{Window, WindowManager}
import android.os.Bundle
import android.media.{SoundPool, AudioManager}
import android.content.{Context}
import android.util.AttributeSet
import android.widget.{LinearLayout, TextView}
import android.view.{View, LayoutInflater}
import de.tomhoefer.jogga.R 
import android.util.Log 

import de.tomhoefer.jogga.extensions.ActivityExt 


class SplashScreen(c:Context, as:AttributeSet) extends LinearLayout(c,as) {
	private val that:View = this 
	def this(c:Context) = this(c, null)
	private val inf = getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
	inf.inflate(R.layout.splash, this, true)
	private val splashText = findViewById(R.id.splashText).asInstanceOf[TextView]  
	def status = splashText.getText 
	def status_= (_status:String) = splashText.setText(_status) 
}