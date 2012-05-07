package de.tomhoefer.jogga 

import java.io.IOException 
import scala.xml.{Elem, XML} 
import android.app.{Activity, Dialog, AlertDialog, ProgressDialog} 
import android.os.Bundle
import android.view.animation.{Animation, AnimationUtils, LayoutAnimationController}
import android.view.{View, WindowManager}
import android.util.Log
import android.widget.{Spinner, LinearLayout, ArrayAdapter, DatePicker, EditText, Button, TextView}
import android.webkit.WebView 
import android.content.{Intent, Context, DialogInterface}
import android.content.res.Resources
import extensions._
import extensions.implicits._ 
import models.userModel 
import concurrent.ops._ 


/**
 * Basisklasse des SettingsScreen
 * 
 * @author tom
 *
 */
class SettingsActivity extends AppDefaultActivity {   
    
  /*
   * Laender-Auswahl
   */
  private var countrySpinner:Spinner = _
  /*
   * Geburtstags-Auswahl
   */
  private var birthday:DatePicker = _
  /*
   * Email-Input
   */
  private var email:EditText = _
  /*
   * Username-Input
   */
  private var username:EditText = _ 
  /*
   * Passwort-Input
   */
  private var password:EditText = _
  /*
   * Registrierungs-Button
   */
  private var registerNow:Button = _ 
  
  
  /**
   * Ueberschriebener onCreate-Hook
   * 
   * @param b Bundle der letzten Sitzung 
   */
  override def onCreate(b:Bundle) = {
	super.onCreate(b)
	setContentView(R.layout.settings) 
	
	/*
	 * Views holen
	 */
	countrySpinner = findView(R.id.country) 
    val adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item) 
    adapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item 
    countrySpinner setAdapter adapter 
    
    birthday = findView(R.id.birthday)
    birthday init(1980, 7, 22, null)  
    username = findView(R.id.username)
    registerNow = findView(R.id.registerNow)  
    password = findView(R.id.password)
    email = findView(R.id.email)
    
    
    /*
     * Sobald sich jemand registieren moechte...
     */
    registerNow touch { 
		
		/*
		 * Userdaten holen...
		 */
		val birthdate = this.birthday.getYear+"-"+this.birthday.getMonth+"-"+this.birthday.getDayOfMonth
		val username = this.username.getText.toString 
		val country = countrySpinner.getSelectedItem.toString
		val password = this.password.getText.toString
		val email = this.email.getText.toString 
		
		/*
		 * ... und Fortschrittsanzeige anzeigen 
		 */
		val progess = simpleProgress("Connecting to the web...") 
		/*
		 * Neuen Thread starten, damit GUI-Thread nicht blockiert 
		 */
		spawn {
			try {
				/*
				 * Registierung durchfuehren und Daten bzw Callback uebergeben
				 */
				userModel.register( 
					/*
					 * Userdaten in Map sammeln
					 */
					options = Map('birthdate -> birthdate, 'country -> country, 'email -> email, 'password -> password, 'username -> username),
					/*
					 * Block fuer den Erfolgsfall uebergeben
					 */
					success = { userId => 
						/*
						 * Block in EventQueue ablegen 
						 */
						runOnGuiThread {
							progess.dismiss	
							simpleAlert("Registration successful!", "Welcome to Jogga-City "+username+"!")
						}
					},	
					/*
					 * Block fuer den Fehlschlag uebergeben
					 */
					error = { errors =>
						/*
						 * Block in EventQueue ablegen  
						 */
						runOnGuiThread {
							progess.dismiss 
							simpleAlert("Please correct your input", errors) 
						}
					}
				)
			} catch {
				/*
				 * Falls keine Netzwerkverbindung existiert
				 */
				case ex:IOException => runOnGuiThread {
					progess.dismiss 
					simpleAlert("IO-Error", "Please make sure that you have a network connection available! Error-Details: "+ex.getMessage)  
				}
				/*
				 * Falls ein allgemeinerer Fehler aufgetreten ist  
				 */
				case ex:Exception => runOnGuiThread {
					progess.dismiss 
					simpleAlert("Error", "Sorry, an unexpected error occured, please try again later!")
				}
			  }
			}
		() 
	}	
  }
}
    