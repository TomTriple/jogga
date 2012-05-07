package de.tomhoefer.jogga.components


/*
 * Implementierung des Observer-Pattern
 */
trait Observable {
  /*
   * Queue mit Observer-Objekten 
   */
  private val observers = new collection.mutable.Queue[EventHandler]
  /*
   * Struktur eines Event-Handler-Callbacks
   */
  protected type EventHandler
  /*
   * Registriert einen Observer in der internen Queue
   */
  def onUpdate(observer:EventHandler):Unit = observers += observer
  /*
   * Benachrichtigt alle Observer 
   */
  def notifyListeners(block:EventHandler => Unit) = observers foreach {block(_)} 
} 