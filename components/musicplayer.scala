package de.tomhoefer.jogga.components.musicplayer 

import java.io.{File, FilenameFilter}
import android.media.MediaPlayer 
import android.util.Log


/*
 * Spezifikation einer Playlist 
 */
trait Playlist {
	/*
	 * Gibt den Namen des aktuellen Songs zurueck
	 */
	def songName():String
	/*
	 * Gibt den Pfad zum aktuellen Song zurueck
	 */
	def songPath():String
	/*
	 * Spielt den naechsten Song ab
	 */
	def nextSong():Unit
	/*
	 * Spring zum vorherigen Song 
	 */
	def prevSong():Unit
}


/**
 * Darueber ist eine gewoehnliche Playliste realisiert die mp3-Dateien aus einem Verzeichnis liest und 
 * in einer internen Liste verwaltet
 * 
 * @author tom
 *
 */
trait FolderBasedPlaylist extends Playlist {
	/*
	 * Verzeichnis durchsuchen und nach mp3-Dateien filtern
	 */
	private val fileList = new File("/mnt/sdcard/MP3/jogga/").listFiles.filter {_.getName.endsWith(".mp3")}

	/*
	 * Invariante - es muss mind eine mp3-Datei verfuegbar sein
	 */
	assert(fileList.size > 0, "music-folder must contain mp3-files")
	
	/*
	 * Aktueller Index 
	 */
	private var index = 0 
	
	def songName() = fileList(index).getName 
	def songPath() = fileList(index).getAbsolutePath
	def nextSong() = {
		if(index == fileList.length - 1)
			index = 0
		else
			index += 1
	} 
	def prevSong() = {
		if(index == 0)
			index = fileList.length - 1
		else
			index -= 1
		index 
	} 
}


/*
 * Abstrakter Zustand 
 */
protected trait PlayerState { 
	def next():Unit 
	def prev():Unit 
}

/**
 * Diese Klasse arbeitet nach dem State-Pattern. Beim Instanziieren muss eine Instanz einer Playlist
 * uebergeben werden um die selftype-annotation zu erfuellen. 
 * 
 * @author tom
 *
 */
class MusicPlayer { self:Playlist =>  

	/* 
	 * Konkreter Zustand - Musicplayer gestoppt
	 */
	private val playerStateStopped = new PlayerState { 
		def next():Unit = {  
			self.play()  
			state = playerStatePlaying
		}
		def prev():Unit = { 
			prevSong()
			state = playerStatePlaying
		}
	}

	/*
	 * Konkreter Zustand - Musicplayer in pausiertem Zustand 
	 */
	private val playerStatePaused = new PlayerState {
		def next():Unit = {
			mp.start 
			state = playerStatePlaying 
		}
		def prev():Unit = {
			prevSong()
			play() 
			state = playerStatePlaying
		}
	}
	/*
	 * Konkreter Zustand - Musikplayer spielt einen Song 
	 */
	private val playerStatePlaying = new PlayerState {
		def next():Unit = {
			nextSong() 
			play() 
		} 
		def prev():Unit = {
			prevSong()
			play() 
		}
	} 
	
	private val mp = new MediaPlayer
	private var state:PlayerState = playerStateStopped  
	
	mp setOnPreparedListener new MediaPlayer.OnPreparedListener { 
		def onPrepared(mp:MediaPlayer) = mp start 
	}
	
	mp setOnCompletionListener new MediaPlayer.OnCompletionListener() {
		def onCompletion(mp:MediaPlayer) = next   
	}
		
	
	def play() = { 
		mp.reset 
		mp.setDataSource(songPath)  
		mp.prepareAsync 
	}
	
	def next() = state next
	def pause() = {
		mp.pause
		state = playerStatePaused  
	}
	def stop() = {
		mp.stop 
		state = playerStateStopped 
	}
	def prev() = state prev 
	def release() = {
		mp.stop
		mp.release 
	}
	def songinfo() = songName.replace(".mp3", "")   
}
