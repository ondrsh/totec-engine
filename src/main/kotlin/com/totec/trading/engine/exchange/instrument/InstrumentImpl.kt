/**
 * Created by ndrsh on 6/3/20
 */

package com.totec.trading.engine.exchange.instrument

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.instrument.Instrument
import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.InstrumentProcessEngines
import com.totec.trading.core.instrument.book.Book
import com.totec.trading.core.instrument.book.Side
import com.totec.trading.core.instrument.book.ops.BookOp
import javax.inject.Inject

class InstrumentImpl @Inject constructor(override val exchange: Exchange,
                                         override val book: Book,
                                         override val info: InstrumentInfo) : Instrument {
    
    override lateinit var engines: InstrumentProcessEngines
    
    @Inject
    fun injectProcessEngines(engines: InstrumentProcessEngines) {
        // book engines
        book.bids.engines = engines
        book.asks.engines = engines
        
	    for (engine in engines.infoEngines) {
            engine.instrumentInfo = info
            engine.exchange = exchange
        }
        this.engines = engines
    }
    
    override fun processBook(newBook: Book) {
        // update only if last update was not too long ago
        // this is determined also by the size of the book, so that more popular instruments like XBTUSD
        // have lower thresholds than less popular instruments
        if (book.isInitialized() && book.lastUpdateTooLongAgo(newBook) == false) {
            processBookSide(Side.Buy, newBook)
            processBookSide(Side.Sell, newBook)
        } else {
            replaceBookSide(Side.Buy, newBook)
            replaceBookSide(Side.Sell, newBook)
            engines.addBook(newBook)
        }
    }
    
    private fun processBookSide(side: Side, newBook: Book) {
        // if old entry is not contained in new set, delete it
        val oldSide = book[side]
        val newSide = newBook[side]
        for (entry in oldSide.set) {
            if (newSide.set.contains(entry) == false) {
                val deleteOp = BookOp.getDelete(entry.price, newBook.timestamp)
                val deleteSuccess = oldSide.processDelete(deleteOp)
                if (deleteSuccess == false) {
                    entry.decCount()
                }
            }
        }
        
        // either insert all new entries
        // or change them if they are already there but have wrong amount,
        // or do nothing if they have the correct amount
        for (entry in newSide.set) {
            val op = BookOp.getInsert(entry.price, entry.amount, entry.timestamp)
            if (op.price == 265.7) {
                println()
            }
            val opSuccess = oldSide.processInsert(op)
            if (opSuccess == false) {
                entry.decCount()
            }
        }
    }
    
    private fun replaceBookSide(side: Side, newBook: Book) {
        val bookSide = book[side]
        for (entry in bookSide.set) {
            entry.decCount()
        }
        bookSide.map = newBook[side].map
        bookSide.set = newBook[side].set
        bookSide.lastUpdated = newBook.timestamp
    }
}
