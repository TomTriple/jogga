package de.tomhoefer.jogga.components 

import java.util.{Timer => JTimer} 
import java.util.{TimerTask, Date} 


/**
 * Zeitgeber - benachrichtigt Observer im Interval von `rate` 
 * @author tom
 *
 */
class Timer(rate:Int) extends Observable {   
	
	/*
	 * Invariante 
	 */
	require(rate > 0, "rate must be > 0")
	
	type EventHandler = Long => Unit
	private val timer = new JTimer
	private var time:Long = _ 
	
	override def onUpdate(block:EventHandler) = {
		/*
		 * Vorbedingung
		 */
		assert(block != null, "block must not be null")
		
		super.onUpdate(block)
		/*
		 * Timer-Thread verwenden 
		 */
		timer.scheduleAtFixedRate(new TimerTask() {  
			def run() = {
				time += rate
				/*
				 * Observer benachrichten und vergangene Zeit uebergeben  
				 */
				notifyListeners {_(time - 1000 * 60 * 60)}  
			}   
		}, 0, rate)
	}
}

