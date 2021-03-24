/**
 * Created by ndrsh on 14.06.20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.InstrumentProcessEngines
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.Side
import com.totec.trading.core.instrument.book.ops.BookOp
import net.openhft.smoothie.SmoothieMap
import java.util.*
import javax.inject.Inject

class AnotherBookSideImpl @Inject constructor() : BookSideImpl(SmoothieMap(),
                                                               TreeSet<BookEntry>()) {
	
	override val side: Side = Side.Sell
	
	override fun processChange(change: BookOp): Boolean {
		return false
	}
	
	override fun InstrumentProcessEngines.addOp(op: BookOp) {
		TODO("Not yet implemented")
	}
}
