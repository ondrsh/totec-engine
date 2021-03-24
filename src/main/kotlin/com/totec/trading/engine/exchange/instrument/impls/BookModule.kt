/**
 * Created by ndrsh on 9/7/20
 */

package com.totec.trading.engine.exchange.instrument.impls

import com.totec.trading.core.instrument.book.Book
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.bookside.Asks
import com.totec.trading.core.instrument.book.bookside.Bids
import com.totec.trading.engine.exchange.instrument.book.*
import dagger.Module
import dagger.Provides
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet
import net.openhft.smoothie.SmoothieMap

@Module
open class BookModule {
	
	/**
	 * Don't forget to set the lastUpdated timestamp on both [BookImpl.asks] and [BookImpl.bids].
	 * Otherwise it will be [Long.MIN_VALUE].
	 */
	@Provides
	fun book(bookImpl: BookImpl): Book = bookImpl
	
	@Provides
	fun asks(asksImpl: AsksImpl): Asks = asksImpl
	
	@Provides
	fun bids(bidsImpl: BidsImpl): Bids = bidsImpl
	
	@Provides
	fun provideBookSideMap(): MutableMap<Double, BookEntry> = SmoothieMap()
	
	@Provides
	@AsksSet
	open fun provideAsksSortedSet(): MutableSet<BookEntry> = ObjectAVLTreeSet { entry1, entry2 -> entry1.price.compareTo(entry2.price) }
	
	@Provides
	@BidsSet
	open fun provideBidsSortedSet(): MutableSet<BookEntry> = ObjectAVLTreeSet { entry1, entry2 -> entry2.price.compareTo(entry1.price) }
}