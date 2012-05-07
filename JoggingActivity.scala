package de.tomhoefer.jogga 

import android.app.Activity 
import android.os.Bundle
import android.content.Intent     
import android.util.AttributeSet
import android.view.{View, LayoutInflater, WindowManager}  
import android.widget.{RelativeLayout, LinearLayout, Button, TextView}  
import android.content.Context 
import android.util.Log 
import extensions._ 
import extensions.implicits._
import extensions.ViewGroupExt
import components._
import components.linechart._ 
import android.view.View.{VISIBLE, INVISIBLE, GONE}
import concurrent.ops._ 
import views.JoggingView 
import models.joggingModel 
import actors.Actor 
import android.graphics.{BitmapFactory, Bitmap, Paint, Canvas}


import android.speech.tts.TextToSpeech._  

/*
 * Start-Message fuer den internen Actor - Android scheint self als Actor nicht zu unterstuetzen 
 */
case class Start

/**
 * BasisActivity des Screens, der waehrend des Laufens sichtbar ist
 * 
 * @author tom
 *
 */
class JoggingActivity extends AppDefaultActivity { self => 

	/*
	 * Layout des GPS-Wartescreen
	 */
	var gps:RelativeLayout = _
	/*
	 * Buttons die den Status der GPS-Ueberwachung anzeigen 
	 */
	var gpsWaitingView:TextView = _ 
	var gpsFoundView:TextView = _
	var gpsReadyView:TextView = _   

	/*
	 * Layout der JoggingScreens
	 */
	var joggingView:JoggingView = _ 

	/*
	 * Zeitgeber
	 */
	var timer:Timer = _
	/*
	 * Schrittzaehler
	 */
	var pedometer:Pedometer = _
	/*
	 * GPS-Tracker
	 */
	var tracker:Tracker = _
	/*
	 * Sprach-Synthetisierung 
	 */
	var speecher:Speecher = _ 
	
/*
	override def onActivityResult(requestCode:Int, resultCode:Int, data:Intent) = { 
		if(requestCode == 1) {
			if(resultCode == 1) {
				tts = new TextToSpeech(this, new TextToSpeech.OnInitListener {
					def onInit(status:Int) = {
						Log.d("test", "speec-init-success: "+status) 
						ttsInit = true 
						if(tts.isLanguageAvailable(Locale.GERMAN) >= 0) 
							tts setLanguage Locale.GERMAN 
					}
				})
				tts.setLanguage(Locale.US) 
			} else {
				startActivity(new Intent("android.speech.tts.engine.INSTALL_TTS_DATA")) 
			}
		}
	}
*/
	
	/**
	 * Ueberschriebener onCreate-Hook
	 * 
	 * @param b Bundle mit Werten der letzten Sitzung
	 */
	override def onCreate(b:Bundle) = { 
		
		super.onCreate(b)  
		setContentView(R.layout.jogging) 
		getWindow addFlags WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON 
		
		/*
		 * Views holen
		 */
		gps = findView(R.id.gps) 
		gpsWaitingView = findView(R.id.gpsWaiting) 
		gpsFoundView = findView(R.id.gpsFound)
		gpsReadyView = findView(R.id.gpsReady) 
		joggingView = findView(R.id.joggingView) 
		gpsWaitingView startAnimation findAnim(R.anim.gpsbuttons_show)

		/*
		 * Tracker instanziieren und initialen Block uebergeben. Im Block die GUI-Interaktion und Callbacks festlegen. 
		 * 
		 */
		tracker = Tracker(this) { (provider, location) =>  
			gpsFoundView setVisibility VISIBLE
			gpsFoundView startAnimation withAnim(R.anim.gpsbuttons_show) { anim => 
				anim(end={
					gpsReadyView setVisibility VISIBLE
					gpsReadyView startAnimation withAnim(R.anim.gpsbuttons_show) { anim => 
						anim(end={ 
							soundEffect('ding) 
							gpsReadyView touch {
								gps startAnimation withAnim(R.anim.gpsbuttons_hide) { anim =>
									anim(end={
										gps setVisibility GONE
										startViewUpdates() 
									})
								}
							}
							gpsReadyView startAnimation findAnim(R.anim.gpsbuttonready_repeat)
						})
					}
				})
			}
		}
		
	}
	
	/*
	 * Wird aufgerufen, sobald der GPS-Wartescreen fertig ist 
	 */
	private def startViewUpdates() = {
		/*
		 * Anzahl Segmente auf LineChart
		 */
		val segments = 60
		/*
		 * Hintergrund-Bitmap
		 */
		val src = BitmapFactory.decodeResource(getResources(), R.drawable.graph_background)
		/*
		 * Actor fuer das Zeichnen des Graphen - Bitmap nur kopiert uebergeben um den Forderungen des Actor-Modells gerechet zu werden  
		 */
		val lineChartActor = new LineChartActor(src.copy(src.getConfig, true)) with LineChartStyle
		lineChartActor.start 
		val paintActor = new Actor {
			def act() = loop {
				receive {
					/*
					 * Sobald die Zeichnung eines neuen LineCharts abgeschlossen ist diesen in die GUI uebernehmen
					 */
					case LineChartRepaintResponse(lineChart) => runOnGuiThread {
						joggingView.joggingGraph setImageBitmap lineChart   
						joggingView.joggingGraph.postInvalidate
					} 
					/*
					 * Schickt eine Repaint-Message an den LineChartActor
					 */ 
					case Start => lineChartActor ! LineChartRepaint(
						LineChartData(
							segments = segments,
							max = joggingModel.kmhListMax,
							maxStr = joggingModel.kmhListMax.toInt+" km/h",
							min = joggingModel.kmhListMin, 
							minStr = joggingModel.kmhListMin.toInt+" km/h",
							vals = joggingModel.kmhList.take(segments+1).toList.reverse,
							color = (255 << 24 | 255 << 16 | 255 << 8 | 255)  
						)
					)
					case _ => throw new RuntimeException("unknown message")
				}
			}
		}
		paintActor start 
		
		speecher = new Speecher(self)
		
		/*
		 * Timer instanziiern und Observer hinzufuegen - alle 15 sekunden status per sprache ausgeben. Jede Sekunde das joggingModel aktualisieren
		 */
		timer = new Timer(1000) 
		timer onUpdate { duration =>
			if(duration % 240000 == 0) 
				speecher speak joggingModel.speechInfo
			joggingModel.duration = duration
			runOnGuiThread { joggingView.timeView setText joggingModel.durationStr }
		}
		
		/*
		 * Schrittzaehler instanziieren und Observer hinzufuegen. Bei jedem Schritt das joggingModel aktualisieren
		 */
		pedometer = Pedometer(self) 
		pedometer onUpdate { steps => 
			joggingModel.steps = steps 
			joggingView.stepsView setText joggingModel.stepsStr 
		}
		
		/*
		 * Observer fuer das joggingModel hinzufuegen - bei jeder neuen Location, die an das Model uebertragen wurde GUI-Elemente aktualisieren sowie
		 * eine neue Grafik ueber den paintActor asynchron anfordern
		 */
		joggingModel onLocationRetrieved {
			joggingView.speedView setText joggingModel.speedStr
			joggingView.distanceView setText joggingModel.distanceStr  
			joggingView.caloriesView setText joggingModel.caloriesStr
			if(joggingModel.kmhList.size > 1) { 
				paintActor ! Start  
			} 
		}
		
		/*
		 * Bei jeder neu empfangenen Location das joggingModel aktualisieren
		 */
		tracker onUpdate { (provider, location) =>
			joggingModel + location 
		} 
		
		
	}
}

