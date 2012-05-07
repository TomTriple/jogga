package de.tomhoefer.jogga 

import android.app.Activity
import android.os.Bundle
import android.view.animation.{Animation, AnimationUtils, LayoutAnimationController}
import android.view.View 
import android.widget.{ImageView, RelativeLayout, LinearLayout, TextView}
import android.content.{Intent, Context}
import android.content.res.Resources
import extensions._ 
import views.SplashScreen
import extensions.implicits._ 
import models.userModel 

/**
 * Eintritts-Activity der Anwendung
 * 	
 */
class MainActivity extends AppDefaultActivity {   
  /*
   * Splashscreen-Instanz 
   */
  private var splash:SplashScreen = _
  /*
   * Layout mit Buttons auf den Startscreen
   */
  private var mainMenuButtons:LinearLayout = _
  /*
   * Username-Control
   */
  private var username:TextView = _ 
  
  
  /**
   * Ueberschriebener onCreate-Hook
   * 
   * @param b Bundle mit Werten der letzten Sitzung
   */
  override def onCreate(b:Bundle) = {

	super.onCreate(b)
	/*
	 * Context an Model uebergeben
	 */
	userModel.setContext(this) 
    setContentView(R.layout.main)  
    
    /*
     * Views holen 
     */
    splash = findView(R.id.splash)
    username = findView(R.id.username) 
    mainMenuButtons = findView(R.id.mainMenuButtons) 
    splash.status = "Loading sounds..."  
    
    /* 
     * soundPool mit Key-Value-Paaren initialisieren und Block uebergeben der nach dem Laden der Sounds
     * ausgefuert werden soll. Darin Anfangs-Intro definieren. 
     */
    soundPool.onFinishedLoading(this, ('ding -> R.raw.ding), ('gps -> R.raw.gps), ('sung -> R.raw.sung)) {
	    splash startAnimation withAnim(R.anim.splash_fadeout) {anim => 
	    	anim(end={
				splash setVisibility View.GONE 
				mainMenuButtons setLayoutAnimation findLayoutAnim(R.anim.layout_mainmenubuttons)
				mainMenuButtons.startLayoutAnimation
				withView[ImageView](R.id.mainMenuLogo) { view =>
					view startAnimation withAnim(R.anim.logo_scaleup) {anim => 
				    	anim(end=soundEffect('ding))
				    }
				}
	    	})
		}
		splash.status = "Sounds ready!"
	}

	/*
	 * onTouch-Callback fuer StartButton
	 */
    withView(R.id.startButton) { view:ImageView =>  
		view touch { 
			val intent = new Intent(this, classOf[JoggingActivity])   
			startActivity(intent)  
		}
	}
    
    /*
     * onTouch-Callback fuer SettingsButton
     */
    withView[ImageView](R.id.settingsButton) { view => 
    	view touch {
    		val intent = new Intent(this, classOf[SettingsActivity])
    		startActivity(intent)  
    	}
    }
  }
  
  /*
   * Sobald der Startscreen erscheit ggf den Benutzernamen des Users anzeigen 
   */
  override def onStart() = {
	  super.onStart()
	  username setText userModel.accountInfo
  }

}

