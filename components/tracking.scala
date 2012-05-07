package de.tomhoefer.jogga.components

import de.tomhoefer.jogga.R 
import concurrent.ops._ 
import android.content.Context 
import android.app.Dialog 
import collection.mutable.ListBuffer
import android.widget.{Button, TextView}
import android.view.{View, MotionEvent, WindowManager, Window, ViewGroup} 
import android.location.{LocationManager => LM, LocationListener, Location}
import android.os.Bundle
import android.util.Log 
import de.tomhoefer.jogga.extensions.implicits._ 
import de.tomhoefer.jogga.models.joggingModel 


/**
 * TrackingModul - Observer werden benachrichtigt sofern eine neue gueltige Location verfuegbar ist. 
 * Hier koennen Locations ggf gefilert und geglaettet werden. 
 * 
 * Initialer Callback wird ausgefuert sobald erstmalig Locations verfuegbar sind. Observer, die ueber onUpdate registriert wurden,
 * werden bei jeder neuen Location ausgefuert 
 * 
 * @author tom
 *
 */

class Tracker(context:Context, initialCallback:(String, Location) => Unit) extends Observable { 
	
	require(context != null, "context must not be null")
	
	type EventHandler = (String, Location) => Unit
	private var isFirstLocation = true 	
	private var provider:String = _ 
	private val lm:LM = context.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LM]
	private val initialObservable = new Observable {
		type EventHandler = (String, Location) => Unit  
	}
	initialObservable onUpdate initialCallback 
	/**
	 * GPS basierter LocationListener. Sobald GPS nicht verfuegbar ist, Fallsback auf NetworkListener
	 */
	protected val gpsLocationListener = new LocationListener{
		def onProviderEnabled(p:String) = {}
		def onStatusChanged(p:String, status:Int, b:Bundle) = {}
		/*
		 * Wenn GPS-Provider am Phone vom User deaktiviert wurde Fallback auf Network - hauptsaechlich fuer debugging interessant 
		 */
		def onProviderDisabled(p:String) = {
			lm.removeUpdates(this)
			lm.requestLocationUpdates(LM.NETWORK_PROVIDER, 500, 0, networkLocationListener) 
		}
		def onLocationChanged(location:Location) = location.getAccuracy match {
			case acc => {
				provider  = "gps" 
				onLocationReceived(location)
			} 
		} 
	}

	/**
	 * Network basierter LocationListener
	 */ 
	protected val networkLocationListener = new LocationListener { 
		def onProviderEnabled(p:String) = {}
		def onStatusChanged(p:String, status:Int, b:Bundle) = {}
		def onProviderDisabled(p:String) = {}  
		def onLocationChanged(location:Location) = location.getAccuracy match { 
			case acc => {
				provider  = "network"
				onLocationReceived(location) 
			}  
		}
	}
	/*
	 * gps Listener als erstes versuchen 
	 */
	lm.requestLocationUpdates("gps", 500, 0, gpsLocationListener)
	
	/*	
	 * 
	 */
	protected def onLocationReceived(location:Location) = {
		/*
		 * Wenn es die erste Location ist, den initialen Callback erst ausfuehren
		 */
		if(isFirstLocation) {
			isFirstLocation = false
			initialObservable.notifyListeners(_(provider, location))  
 		}
		/*
		 * Observer, die ueber onUpdate registriert wurden benachrichtigen sowie Provider und aktuelle Location an Block uebergeben 
		 */
		notifyListeners {_(provider, location)}     
	}
}


object Tracker {
	def apply(context:Context)(block:(String, Location) => Unit) = new Tracker(context, block)  
}





