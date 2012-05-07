package de.tomhoefer.jogga.models 

import java.text.SimpleDateFormat
import java.util.Date 
import collection.mutable.{ListBuffer, ArrayBuffer} 
import android.location.Location 
import android.util.Log 
import de.tomhoefer.jogga.components.Observable

/**
 * Model fuer die Daten, die waehrend des Joggens relevant sind und ggf im UI dargestellt werden sollen. 
 * 
 * @author tom
 *
 */
object joggingModel {	
	/*
	 * Observer-Struktur fuer Callback definieren
	 */
	private type EventHandler = () => Unit
	/*
	 * Observable verwenden
	 */
	private val locationRetrievedObservable = new Observable {
		type EventHandler = joggingModel.EventHandler
	}
	/*
	 * Formatierer fuer Stunden, Minuten und Sekunden
	 */
	private val sdf = new SimpleDateFormat("HH:mm:ss")
	/*
	 * Alle Locations - das Model ist ein Listener des Tracking-Moduls
	 * ListBuffer konstante Laufzeit bei prepend und head
	 */
	private var locations = ListBuffer.empty[Location]
	/*
	 * Ist true wenn mind 2 Locations in locations enthalten sind.
	 * Hilfsflag um staendige size-Abfrage von locations zu vermeiden da diese bei Listen nur lineare Laufzeit hat   
	 */
	private var enoughLocations = false 
	/*
	 * Alle gemessenen khm-Werte werden hier gespeichert. Lediglich prepend wird verwendet, welche konstante Laufzeit hat 
	 */
	var kmhList = ListBuffer.empty[Float] 
	var kmhListMax:Float = _
	var kmhListMin:Float = _ 
	/*
	 * Die zurueckgelegte Distanz - Cachevariable: Das Delta wird immer hinzu addiert 
	 */
	private var _distanceCache:Float = _
	
	/*
	 * Aktuelle Geschwindigkeit 
	 */
	private var _speed:Float = _
	/*
	 * Distanz 
	 */
	private var _distance:Float = _
	/*
	 * Verbrannte Kalorien
	 */
	private var _calories:Int = _
	/*
	 * Schritte
	 */
	private var _steps:Int = _ 
	/*
	 * Dauer des Laufs
	 */
	private var _duration:Long = _
	/*
	 * Hoehe 
	 */
	private var _altitude:Option[Float] = _
	/**
	 * Gibt die aktuelle Location zurueck
	 */
	def locationCurrent = locations head
	/**
	 * Gibt die vorhergehende Location zurueck - also die vor locationCurrent 
	 */
	def locationLast = if(enoughLocations) Some(locations take 2 last) else None
	
	/**
	 * Formatierer fuer Geschwindigkeit
	 */
	def speedStr = _speed.toInt+" km/h"
	def speed = _speed
	def speed_=(_speed:Float) = this._speed = _speed
	
	/**
	 * Formatierer fuer Distanz
	 */
	def distanceStr = _distance.toInt+" m"
	def distance = _distance 
	def distance_=(_distance:Float) = this._distance = _distance  

	/**
	 * Formatierer Kalorien 
	 */
	def caloriesStr = _calories+" kcal"
	def calories = _calories 
	def calories_=(_calories:Int) = this._calories = _calories
	
	/**
	 * Formatierer Schritte 
	 */
	def stepsStr = _steps+" steps"
	def steps = _steps
	def steps_=(_steps:Int) = this._steps = _steps 
	
	/**
	 * Formatierer Laufdauer 
	 */
	def durationStr = sdf.format(new Date(_duration))
	def duration = _duration 
	def duration_=(_duration:Long) = this._duration = _duration  
	
	/**
	 * Das Distanz in Metern zwischen den letzten beiden Locations
	 * 
	 * @return Distanz in Metern 
	 */
	protected def locationDelta = locationLast match { 
		case Some(last) => locationCurrent distanceTo last  
		case None => 0
	}

	/**
	 * Berechnet und gibt die momentane Geschwindigkeit zurueck
	 * 
	 * @return Geschwindigkeit
	 */
	protected def calcSpeed = {
		val cur = locationCurrent 
		locationLast match { 
			case Some(locationLast) => {
				val deltaDist = locationDelta
				val deltaTime = cur.getTime - locationLast.getTime  
				deltaDist / deltaTime * 1000 * 3.6f
			} 
			case None => 0
		}
	}

	/**
	 * Gibt eine Option zur aktuellen Hoehe zurueck 
	 */
	protected def calcAltitude = locationCurrent.hasAltitude match {
		case true => Some(locationCurrent.getAltitude.asInstanceOf[Float]) 
		case false => None 
	}

	/**
	 * Berechnet und gibt die bisher gelaufene Distanz zurueck 
	 */
	protected def calcDistance = {
		_distanceCache += locationDelta  
		_distanceCache 
	}
	 
	/**
	 * Berechnet und gibt die Kalorien zurueck (http://jogmap.de/civic4/?q=node/10036 )
	 */
	protected def calcCalories = (85 * _distanceCache  / 1000).toInt
	
	/**
	 * BerechnetWerte neue bei einer neuen Location 
	 */
	protected def update() = { 
		_altitude = calcAltitude
		_speed = calcSpeed
		_distance = calcDistance
		_calories = calcCalories   
	}
	
	/**
	 * Fuegt eine neue Location hinzu, fuert darauf aufbauende Berechnungen aus und
	 * benachrichtigt Observer die an den resultaten Interessiert sind 
	 */
	def +(location:Location) = {
		
		assert(location != null, "location must not be null")
		
		if(! locations.isEmpty) {
			// es werden zumindest 2 locations benoetigt 
			locationLast match {
				case Some(locationLast) => { 
					var diffTime = location.getTime - locationLast.getTime
					var diffDist = location distanceTo locationLast
					// in meter umrechnen 
					var mpm = (diffDist / diffTime * 1000 * 3.6f).toInt  
					kmhList prepend mpm 
					if(mpm > kmhListMax)
						kmhListMax = mpm
					else if(mpm < kmhListMin)
						kmhListMin = mpm
				}
				case _ => 
			}
		}
		// in gesamtliste einfuegen 
		locations prepend location
		// short-circuit-evaluation aus java ueber 2x if nachbauen
		if(!enoughLocations) if(locations.size > 1) enoughLocations = true
		update() 
		locationRetrievedObservable notifyListeners {_()}  
	}
	
	/**
	 * Fuehrt `block` aus, sobald Berechnungen im Zuge einer neuen Location abgeschlossen wurden  
	 */
	def onLocationRetrieved(block: => Unit) = locationRetrievedObservable.onUpdate(block _)
	
	/**
	 * Text fuer Sprachsynthetisierung 
	 */
	def speechInfo = {
""" 
Deine Geschwindigkeit ist momentan """+speed.toInt+""" kmh. Du bist bisher """+distance.toInt+""" Meter gelaufen und hast dabei """+calories.toInt+""" Kalorien verbrannt. Das ist super!
"""
	}
}