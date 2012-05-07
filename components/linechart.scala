package de.tomhoefer.jogga.components.linechart
	

import android.graphics.{Bitmap, Paint, Canvas, Path}
import actors.Actor

/**
 * Message-Klasse zum neu Zeichnen 
 * 
 * @param data Liste mit LineChartData-Instanzen
 */
class LineChartRepaint(val data:List[LineChartData])
/**
 * Companion der variable Parameteranzahl akzeptiert
 */
object LineChartRepaint {
	def apply(data:LineChartData*) = new LineChartRepaint(data.toList)
	def unapply(repaint:LineChartRepaint) = Some(repaint.data)    
}

/**
 * Response-Message fuer Repaint-Requests
 * @param bitmap Erzeugte Bitmap  
 * 
 */
case class LineChartRepaintResponse(bitmap:Bitmap)
/**
 * Typ eines Messageobjektes der Message Repaint. Alle Attribute sind Immutables. 
 */
case class LineChartData(min:Float, max:Float, vals:List[Float], segments:Int, maxStr:String, minStr:String, color:Int)


trait OnlyBitmap {
	protected[this] val originalBitmap:Bitmap  
}

/**
 * Actor ueber ein beliebiger LineChart gezeichnet werden kann 
 * 
 * @author tom
 *
 */
class LineChartActor(protected val originalBitmap:Bitmap) extends Actor with OnlyBitmap { this: LineChartStyle =>  
	
	/*
	 * Invarianten 
	 */
	require(originalBitmap != null, "bitmap must not be null")
	
	def act() = loop {
		receive {
			case LineChartRepaint(data:List[LineChartData]) => lineChartRepaint(data)
		}
	}


	private[this] def lineChartRepaint(data:List[LineChartData]) = {
		prepare
		data foreach { lcd => 
		
			require(lcd.segments > 0, "segments must be larger than 0")
			
			val width = chartWidth
			val height = chartHeight   
			val segmentWidth = width / lcd.segments  
			var index = 0 
			setMax(lcd.maxStr) 
			setMin(lcd.minStr)
			setColor(lcd.color)
			lcd.vals.sliding(2).foreach { mpmList =>
				drawSegment(segmentWidth * index, height / lcd.max * mpmList(0), segmentWidth * (index + 1), height / lcd.max * mpmList(1) ) 
				index += 1
			} 
		}
		reply(LineChartRepaintResponse(renderedBitmap)) 
	}
}



/**
 * Klasse, die eine Canvas kapselt und Zeichenroutinen fuer einen LineChart implementiert
 *
 */
trait LineChartStyle extends OnlyBitmap {
	
	/*
	 * Kopie der Bitmap 
	 */ 
	protected var bitmapCopy = originalBitmap.copy(originalBitmap.getConfig, true) 
	
	// invariante
	assert(bitmapCopy != null, "copying bitmap failed")
	
	protected val linePaint = new Paint(Paint.ANTI_ALIAS_FLAG) 
	linePaint setStrokeCap Paint.Cap.ROUND
	linePaint setStrokeWidth 5.0f 
	linePaint setColor (255 << 24 | 255 << 16 | 255 << 8 | 255)  
	
	protected val rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG) 
	rectPaint setColor (30 << 24 | 0 << 16 | 0 << 8 | 0)   
	rectPaint setStyle Paint.Style.FILL
	
	protected val textPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
	textPaint setTextSize 18.0f
	textPaint setColor (255 << 24 | 80 << 16 | 80 << 8 | 80)
	textPaint 
	
	protected val canvas = new Canvas() 
	canvas setBitmap bitmapCopy 
	protected val paddingLeft = 25
	protected val paddingBottom = 25
	
	protected var max:String = _ 
	protected var min:String = _
	
	/**
	 * Muss vor einem Aufruf von drawSegment aufgerufen werden  
	 */
	protected def prepare() = { 
		bitmapCopy = originalBitmap.copy(originalBitmap.getConfig, true)
		assert(bitmapCopy != null, "copying bitmap failed")
	}

	/**
	 * Zeichnet ein Segment im Chart 
	 */
	protected def drawSegment(startX:Float, startY:Float, stopX:Float, stopY:Float):Unit = {
		canvas setBitmap bitmapCopy
		val p = new Path
		p moveTo(startX + paddingLeft, canvas.getHeight - paddingBottom)
		p lineTo(startX + paddingLeft, canvas.getHeight - paddingBottom - startY)
		p lineTo(stopX + paddingLeft, canvas.getHeight - paddingBottom - stopY)
		p lineTo(stopX + paddingLeft, canvas.getHeight - paddingBottom) 
		p.close
		canvas.drawPath(p, rectPaint) 
		canvas.drawLine(startX + paddingLeft, canvas.getHeight - startY - paddingBottom, stopX + paddingLeft, canvas.getHeight - stopY - paddingBottom, linePaint)
		canvas.drawText(min, 10, canvas.getHeight - paddingBottom + 7, textPaint) 
		canvas.drawText(max, 10, paddingBottom + 7, textPaint)
	}
	protected def renderedBitmap = bitmapCopy 
	protected def chartWidth = originalBitmap.getWidth - 2 * paddingLeft
	protected def chartHeight = originalBitmap.getHeight - 2 * paddingBottom   
	protected def setMax(max:String) = this.max = max
	protected def setMin(min:String) = this.min = min 
	protected def setColor(color:Int) = 	linePaint setColor color  
} 




