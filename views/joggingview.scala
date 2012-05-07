package de.tomhoefer.jogga.views 

import de.tomhoefer.jogga.R 
import android.app.Activity 
import android.os.Bundle
import android.util.AttributeSet
import android.view.{View, LayoutInflater} 
import android.widget.{RelativeLayout, LinearLayout, Button, TextView, ImageView}   
import android.content.Context 
import android.util.Log  
import android.view.View.{VISIBLE, INVISIBLE, GONE}
import concurrent.ops._  
import de.tomhoefer.jogga.extensions._ 
import de.tomhoefer.jogga.extensions.implicits._   
import de.tomhoefer.jogga.components.musicplayer._  
import de.tomhoefer.jogga.models.joggingModel


class JoggingView(context:Context, as:AttributeSet) extends RelativeLayout(context, as) with ViewGroupExt {
	
	def this(context:Context) = this(context, null)
	
	private val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
	li.inflate(R.layout.jogging_view, this, true)
	val timeView:TextView = findView(R.id.joggingTime) 
	val stepsView:TextView = findView(R.id.joggingSteps)
	val joggingGraph:ImageView = findView(R.id.joggingGraph) 
	val speedView:TextView = findView(R.id.joggingSpeed)
	val distanceView:TextView = findView(R.id.joggingDistance)	
	val caloriesView:TextView = findView(R.id.joggingCalories)
	var options:ImageView = findView(R.id.joggingOptions) 
	var playerNext:ImageView = findView(R.id.playerNext)
	var playerPause:ImageView = findView(R.id.playerPause) 
	var playerPrev:ImageView = findView(R.id.playerPrev)
	var songinfo:TextView = findView(R.id.songinfo)
	val player = new MusicPlayer with FolderBasedPlaylist

	
	playerNext touch {
		player.next 
		songinfo setText player.songinfo
	}
	playerPause touch {
		player pause
	}
	playerPrev touch {
		player.prev 
		songinfo setText player.songinfo 
	}
	options touch {
		
	} 
	
}
