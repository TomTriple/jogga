package de.tomhoefer.jogga.components

import android.content.Context 
import android.hardware.{SensorManager, Sensor, SensorEventListener, SensorEvent}

/**
 * Schrittzaehler, dem ein Observer uebergeben werden kann um bei Schritten 
 * benachrichtigt zu werden
 * 
 * @param context AnwendungsContext 
 *
 */
case class Pedometer(context:Context) extends Observable {
	
	/*
	 * Invariante - ein Context muss verfuegbar sein
	 */
	require(context != null, "context must not be null")
	
	/**
	 * Enumeration fuer Zustaende in einer Bewegung
	 * @author tom
	 *
	 */
	protected object Dir extends Enumeration { 
		val Top = Value
		val Bottom = Value 
	}

	
	/*
	 * EventHandler-Struktur festlegen 
	 */
	type EventHandler = Int => Unit
	/*
	 * Anzahl getaetiger Schritte 
	 */
	private var steps = 0
	/*
	 * Zustand der Bewegungsrichtung (Oszilliert um Null)  
	 */
	private var state:Dir.Value = _ 
	private val sensorManager:SensorManager = context.getSystemService(Context.SENSOR_SERVICE).asInstanceOf[SensorManager] 
	private val accelerometer:Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
	/*
	 * Listener fuer neue Sensor-Werte hinzufuegen
	 */
	private var sensorListener:SensorEventListener = new SensorEventListener {
		def onSensorChanged(e:SensorEvent) = {
			val (x,y,z) = (e.values(0), e.values(1), e.values(2))
			/*
			 * Laenge Bewegungsvektor berechnen
			 */
			val length = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.STANDARD_GRAVITY
			val result = (length * 100).asInstanceOf[Int]
			if(result >= 100) {
				state = Dir.Top
			} else {
				if(state == Dir.Top) { 
					steps += 1 
					notifyListeners {_(steps)} 
				}
				state = Dir.Bottom
			}
		}
		
		def onAccuracyChanged(sensor:Sensor, acc:Int) = {}
	}
	sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
}

