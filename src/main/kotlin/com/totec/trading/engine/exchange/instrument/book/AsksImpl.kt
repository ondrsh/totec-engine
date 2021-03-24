/**
 * Created by ndrsh on 6/19/20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.InstrumentProcessEngines
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.Side
import com.totec.trading.core.instrument.book.bookside.Asks
import com.totec.trading.core.instrument.book.ops.BookOp
import javax.inject.Inject

class AsksImpl @Inject constructor(map: MutableMap<Double, BookEntry>,
                                   @AsksSet set: MutableSet<BookEntry>) : BookSideImpl(map, set), Asks {
	
	override val side: Side = Side.Sell
	
	// TODO: Implement specifics
	override fun InstrumentProcessEngines.addOp(op: BookOp) = this@AsksImpl.engines.addAskOp(op)
}
