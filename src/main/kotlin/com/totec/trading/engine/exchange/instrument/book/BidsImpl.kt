/**
 * Created by ndrsh on 6/19/20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.InstrumentProcessEngines
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.Side
import com.totec.trading.core.instrument.book.bookside.Bids
import com.totec.trading.core.instrument.book.ops.BookOp
import javax.inject.Inject

class BidsImpl @Inject constructor(map: MutableMap<Double, BookEntry>,
                                   @BidsSet set: MutableSet<BookEntry>) : BookSideImpl(map, set), Bids {
    
    override val side: Side = Side.Buy
	
    override fun InstrumentProcessEngines.addOp(op: BookOp) = this@BidsImpl.engines.addBidOp(op)
}
