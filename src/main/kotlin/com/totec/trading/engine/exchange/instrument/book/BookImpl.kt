/**
 * Created by ndrsh on 6/2/20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.book.Book
import com.totec.trading.core.instrument.book.bookside.Asks
import com.totec.trading.core.instrument.book.bookside.Bids
import javax.inject.Inject

open class BookImpl @Inject constructor(override var bids: Bids,
                                        override var asks: Asks) : Book
