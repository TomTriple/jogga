package de.tomhoefer.jogga.extensions 

import android.app.{Activity, AlertDialog, ProgressDialog}
import android.util.Log 
import android.content.DialogInterface 
import android.view.{View, MotionEvent, ViewGroup}
import android.view.animation.{Animation, AnimationUtils, LayoutAnimationController} 
import android.view.{Window, WindowManager}
import android.os.Bundle
import android.media.{SoundPool, AudioManager}
import android.content.{Context}
import android.util.AttributeSet
import android.widget.{LinearLayout, TextView}
import android.view.{View, LayoutInflater}
import de.tomhoefer.jogga.R 
import android.util.Log 
import android.media.MediaPlayer 


/**
 * Trait der Extensions fuer ViewGroup-Komponenten bereitstellt.
 * 
 * @author tom
 *
 */
trait ViewGroupExt { 
	this: ViewGroup =>
	/**
	 * Methode die die View mit der id `id` laedt, an den Block `block` uebergibt und diese ebenfalls zurueckgibt  
	 * 
	 * @param id Id der zu ladenden View
	 * @param block Callback an den die View mit der id `id` uebergeben wird
	 * @return View mit der id `id` 
	 */
	def withView[A <: View](id:Int)(block:A => Unit) = { 
	  val view = findViewById(id).asInstanceOf[A]
      block(view) 
	  view 
	}
	/**
	 * Methode die die View mit der id `id` laedt, intern castet und zurueck gibt 
	 * 
	 * @param id Id der zu ladenden View
	 * @return Gefundene View 
	 */
	def findView[A <: View](id:Int) = findViewById(id).asInstanceOf[A]
	/**
	 * Laedt eine Animatons-Ressource
	 * 
	 * @param id Id der zu ladenden Animation
	 * @return Gefundene Animation
	 */
	def findAnim(id:Int) = AnimationUtils.loadAnimation(getContext, id)
	/**
	 * Laedt die Animation mit der id `id`, gibt diesen an den block `block` weiter.  
	 * 
	 * @param id Zu ladende Animation
	 * @param block Auszufuehrender Block
	 * @return Gefundene Animation
	 */
	def withAnim(id:Int)(block: Animation => Unit) = {
		val anim = findAnim(id)
		block(anim)
		anim 
	}
	/**
	 * Laedt Layout-Animation mit der id `id`
	 * 
	 * @param id Zu ladende Layoutanimations-Ressource
	 * @return Gefundene LayoutAnimation
	 */
	def findLayoutAnim(id:Int) = AnimationUtils.loadLayoutAnimation(getContext, id) 
}


trait ActivityExt { 
	this: Activity =>  
	def withView[A <: View](id:Int)(block:A => Unit) = { 
	  val view = findViewById(id).asInstanceOf[A]
      block(view)
	  view 
	}
	def findView[A <: View](id:Int) = findViewById(id).asInstanceOf[A]
	def findAnim(id:Int) = AnimationUtils.loadAnimation(this, id)
	def withAnim(id:Int)(block: Animation => Unit) = {
		val anim = findAnim(id)
		block(anim)
		anim 
	}
	def findLayoutAnim(id:Int) = AnimationUtils.loadLayoutAnimation(this, id)
	/**
	 * Methode fuer Alert-Anzeige
	 * 
	 * @param title Titel des Alerts
	 * @param message Nachricht im Alert-Fenster
	 * @return AlertDialog.Builder-Instanz
	 */
	def simpleAlert(title:String, message:CharSequence) = {  
			val alert = new AlertDialog.Builder(this)
			alert setTitle title
			alert setMessage message
			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				def onClick(dialog:DialogInterface, arg:Int) = "" 
			})
			alert.show
			alert 
	}
	/**
	 * Methode fuer die Anzeige einer Fortschrittsanzeige
	 * 
	 * @param message Nachricht zur Fortschrittsanzeige
	 * @return ProgressDialog-Instanz
	 */
	def simpleProgress(message:CharSequence) = {  
			val progress = new ProgressDialog(this) 
			progress setMessage message 
			progress setIndeterminate true 
			progress setProgressStyle ProgressDialog.STYLE_SPINNER 
			progress.show 
			progress 
	}
	/**
	 * Helfer-Methode um die EventQueue zu befuellen
	 * @param Block der im Kontext des EventQueue-Thread ausgefuert werden soll  
	 */
	def runOnGuiThread(block: => Unit) = this runOnUiThread new Runnable {
		override def run() = block  
	}
}

/**
 * Extension die die View-Klasse um Methoden erweitert. 
 * 
 * @author tom
 *
 */
trait ViewExt {
	val that:View
	private def touchResponse(value:AnyVal) = value match {
		case false => false 
		case _ => true 
	}
	/**
	 * Methoden die den uebergebenen Block bei einem TouchEvent ausfuehrt
	 * 
	 * @param callback Auszufuerender Block
	 */
	def touch(callback: => AnyVal) = that setOnTouchListener new View.OnTouchListener { 
		def onTouch(v:View, e:MotionEvent) = {
			var result = false 
			if(e.getAction == MotionEvent.ACTION_DOWN) {
				result = touchResponse(callback)   
			} 
			result
		} 
	}
	/**
	 * Ueberladene Methode, die die Original-Parameter View und MotionEvent zusaetzlich an den Block
	 * uebergeben
	 * @param callback Auszufuehrender Block 
	 */
	def touch(callback:(View, MotionEvent) => AnyVal) = that setOnTouchListener new View.OnTouchListener {
		def onTouch(v:View, e:MotionEvent) = touchResponse(callback(v,e))
	}
}


/**
 * Definiert Implizite Konvertierungen. 
 * 
 * @author tom
 *
 */
object implicits {
	
	/**
	 * Wrapper-Klasse fuer Animations-Objekte
	 * 
	 * @param animation Original-Animation
	 */
	protected class RichAnimation(val animation:Animation) {
		/**
		 * Intern wird ein gewoehnlicher AnimationListener verwendet, um die uebergebenen Bloecke
		 * bei deren zugehoerigen Ereignissen auszufuehren. 
		 * 
		 * @param start Block, der beim Start der Animation ausgefuehrt werden soll
		 * @param end Block, der beim Beenden der Animation ausgefuert werden soll
		 * @param repeat Block, der beim Wiederholen einer Animation ausgefuehrt werden soll 
		 */
		def apply(start: => Unit = null, end: => Unit = null, repeat: => Unit = null) = {
			animation.setAnimationListener(new Animation.AnimationListener {
				def onAnimationStart(a:Animation) = start
		    	def onAnimationEnd(a:Animation) = end
		    	def onAnimationRepeat(a:Animation) = repeat 
			}) 
		}
	}
	
	/**
	 * Wrapper-Klasse fuer View-Instanzen - Methoden aus `ViewExt` sind damit verfuegbar 
	 */
	protected class RichView(val v:View) extends ViewExt {  
		val that:View = v 
	}
	
	/*
	 * Konvertierung fuer Animationen 
	 */
	implicit def animation2RichAnimation(a:Animation):RichAnimation = new RichAnimation(a)
	/*
	 * Konvertierung fuer Views
	 */
	implicit def view2RichView(v:View):RichView = new RichView(v)
}



/**
 * Basisklasse aller Jogga-Activities, die um ActivityExtensions erweitert wurde und per default Fullscreen-Format besitzt. 
 * 
 * @author tom
 *
 */
class AppDefaultActivity extends Activity with ActivityExt {  

  /**
   * Ueberschriebener onCreate-Hook
   * 
   * @param b Bundle mit Werten der letzten Sitzung
   */
  override def onCreate(b:Bundle) = {  
	super.onCreate(b) 
	requestWindowFeature(Window.FEATURE_NO_TITLE); 
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);	
  }
  
  def soundEffect(name:Symbol):Unit = soundPool(name)
} 


/**
 * Ueber den Soundpool werden Sounds geladen und mit einem Symbol assoziiert 
 * 
 * @author tom
 *
 */
object soundPool {
	
	type PairType = Pair[Symbol, Int]
	protected var _context:Context = _
	protected val _pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100)  
	protected var _sounds:Map[Symbol, Int] = _ 
	protected var _count = 0 
	protected var _audioManager:AudioManager = _ 

	/**
	 * Initialisierungsmethode, ueber welche die Sounds geladen und der Audio-Service gestartet wird
	 * 
	 * @param context ActivityContext
	 * @param pairs   Argument variabler Laenge mit Symbol/Werte-Paaren
	 * @param block   Callback, der nach der Initialisierung des soundPool aufgerufen wird 
	 */
	def onFinishedLoading(context:Context, pairs:PairType*)(block: => Unit):Unit = {
		if(_sounds != null)
			return block 
		_context = context
		_audioManager = _context.getSystemService(Context.AUDIO_SERVICE).asInstanceOf[AudioManager] 
		val pairList = pairs.toList
	    _pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener {
	    	def onLoadComplete(p:SoundPool, sampled:Int, status:Int) = {
	    		_count += 1
	    		if(_count == pairList.size)
	    			block 
	    	} 
	    })
		_sounds = pairList.foldLeft(Map.empty[Symbol, Int]) { (acc, it) => acc + (it._1 -> _pool.load(_context, it._2, 1)) }
	}
	
	/**
	 * Spiel den Sound, welcher mit *name* assoziiert ist 
	 * 
	 * @param name Key des abzuspielenden Sounds
	 * @param loop Zeigt an wie oft der Sound abgespielt werden soll. Optionaler Parameter. 
	 */
	def apply(name:Symbol, loop:Int = 0) = {
		var streamVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) 
		streamVolume = streamVolume / _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) 
		_pool.play(_sounds(name), 1, 1, 1, loop, 1f)   
	}
}


