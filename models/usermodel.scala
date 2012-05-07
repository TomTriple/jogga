package de.tomhoefer.jogga.models 


import android.content.Context
import android.util.Log  
import de.tomhoefer.jogga.service.backend._   
import android.preference.PreferenceManager

object userModel { 
  
	private var backend = Backend getDefault     
	private var _context:Context = _
	// kapselung ist dennoch erhalten durch uniform access principle 	
	var userId:Int = _
	var username:String = _ 

	//private def getPrefs = _context.getSharedPreferences("user", Context.MODE_PRIVATE)
	private def getPrefs = PreferenceManager.getDefaultSharedPreferences(_context)
	
	def setContext(context:Context) = {
		_context = context
		reload 
	}
	
	def reload() = {
		val prefs = getPrefs 
		userId = prefs.getInt("userId", -1)
		prefs.getString("username", "")
	}
	
	def persist = {
		val editor = getPrefs.edit 
		editor.putString("username", username) 
		editor.putInt("userId", userId)
		editor.commit()
	} 
	
	def accountInfo = username match { 
		case "" | null => "You are not using an account"
		case x => "Your account: "+x
	}

	
	def register(options:Map[Symbol, String], success:Int => Unit, error: String => Unit):Unit = backend.register(
		options = options,
		error = error,
		success = { userId:Int => 
			this.userId = userId 
			username = options('username)   
			/*
			 * Im Erolg userModel speichern
			 */
			persist
			/*
			 * original-success callback aufrufen
			 */
			success(userId) 
		}
	) 
}