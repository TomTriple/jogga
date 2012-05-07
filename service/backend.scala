package de.tomhoefer.jogga.service.backend

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost 
import org.apache.http.entity.StringEntity 
import xml.XML 
import android.text.Html 


private class RestfulBackend extends Backend {
	
	/**
	 * Gibt eine HttpPost-Instanz zur URL `url` zurueck
	 * 
	 * @param url Die Url
	 */
	protected def getDefaultPost(url:String) = {
		val post = new HttpPost(url) 
		post.setHeader("Content-Type", "text/xml")
		post.setHeader("Accept", "text/xml")
		post 
	}
	
	/**
	 * Die Registrierungsmethode fuer neue Jogga-User 
	 * 
	 * @param options Map mit Benutzerwerten
	 * @param success Block, der im Erfolgsfall aufgerufen wird und Username und UserId uebergeben bekommt
	 * @param error   Block, der bei einem Fehlversuch aufgerufen wird und Fehlerdetails uebergeben bekommt 
	 */
	override def register(options:Map[Symbol, String], success:(Int) => Unit, error: String => Unit):Unit = {   
		
		/*
		 * xml-struktur aufbauen 
		 */
		var xml = <user>
				<birthdate type="date">{options('birthdate)}</birthdate> 
				<country>{options('country)}</country>
				<email>{options('email)}</email>
				<password>{options('password)}</password>
				<username>{options('username)}</username> 
			</user>

		/*
		 * Http-Funktionalitaet instanziieren
		 */
		val client = new DefaultHttpClient
		// 192.168.178.32 
		val post = getDefaultPost("http://192.168.1.34/users")
		val entity = new StringEntity(xml.toString)
		post setEntity entity 
		/*
		 * execute blockiert 
		 */
		val response = client execute post
		val responseXml = XML load(response.getEntity.getContent) 
		
		if(response.getStatusLine.getStatusCode == 201) { 
			val username = (responseXml \ "username").text
			val userId = (responseXml \ "id").text.toInt
			success(userId)
		} else {
			val errorStr = (responseXml \\ "error").foldLeft(new StringBuilder("")) {(acc, error) => acc append "<p>"+error.text+"</p>"}
			val errors = Html.fromHtml(errorStr.toString).toString 
			error(errors) 
		}
	}	
} 


trait Backend {
	def register(options:Map[Symbol, String], success:(Int) => Unit, error: String => Unit):Unit 
}


object Backend {
	def getDefault:Backend = new RestfulBackend 
}