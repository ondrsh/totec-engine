/**
 * Created by ndrsh on 6/2/20
 */

package com.totec.trading.engine.exchange.instrument.book

import com.totec.trading.core.instrument.InstrumentProcessEngines
import com.totec.trading.core.instrument.book.BookEntry
import com.totec.trading.core.instrument.book.Side
import com.totec.trading.core.instrument.book.bookside.BookSide
import com.totec.trading.core.instrument.book.ops.BookOp
import com.totec.trading.core.instrument.book.ops.OpType
import com.totec.trading.core.utils.getInvalid
import com.totec.trading.core.utils.logger
import com.totec.trading.core.utils.roundAmountSafe
import com.totec.trading.core.utils.roundPriceSafe
import kotlin.math.max

abstract class BookSideImpl(override var map: MutableMap<Double, @JvmSuppressWildcards BookEntry>,
                            override var set: MutableSet<@JvmSuppressWildcards BookEntry>) : BookSide {
	
	override lateinit var engines: InstrumentProcessEngines
	override var lastUpdated: Long = Long.getInvalid()
	
	
	override fun processInsert(insert: BookOp): Boolean {
		val oldEntry: BookEntry? = map[insert.price]
		
		if (oldEntry != null) {
			if (oldEntry.price == 0.0) {
				println("trying to insert, but already order with price 0.0 inside")
				insert.decCount()
				return false
			} else {
				return if (oldEntry.changeAmountFromInsert(insert)) {
					if (oldEntry.timestamp >= lastUpdated) lastUpdated = oldEntry.timestamp
					else {
						logger.warn("Entry $oldEntry got changed from insert but book timestamp $lastUpdated is higher than entry timestamp")
					}
					engines.addOp(insert)
					true
				} else {
					insert.decCount()
					false
				}
			}
		}
		
		val entry = BookEntry.getNext(insert.price, insert.amount, insert.timestamp)
		set.add(entry)
		map[entry.price] = entry
		insert.setDistanceToTop()
		engines.addOp(insert)
		return true
	}
	
	/** This is called from [processInsert] only, when there already was an order inside. */
	private fun BookEntry.changeAmountFromInsert(insert: BookOp): Boolean {
		if (insert.amount.isZero(insert) || amount == insert.amount) {
			return false
		}
		insert.setDistanceToTop()
		val amountDiff = (insert.amount - amount).roundAmountSafe()
		amount = insert.amount
		insert.amount = amountDiff
		adjustTimestamps(insert, this)
		insert.type = OpType.CHANGE
		return true
	}
	
	override fun processChange(change: BookOp): Boolean {
		val originalEntry: BookEntry? = map[change.price]
		// if there is no such element, fail
		if (originalEntry == null) {
			println("applied change $change but there was no originalEntry with this price")
			change.decCount()
			return false
		}
		
		change.setDistanceToTop()
		val finalAmount = (originalEntry.amount + change.amount).roundAmountSafe()
		return if (finalAmount.isNotZero(op = change) == false) {
			change.decCount()
			false
		} else {
			originalEntry.amount = finalAmount
			adjustTimestamps(change, originalEntry)
			if (change.timestamp >= lastUpdated) lastUpdated = change.timestamp
			else logger.warn("Entry $change got changed but book timestamp $lastUpdated is higher than entry timestamp")
			engines.addOp(change)
			true
		}
	}
	
	override fun processDelete(delete: BookOp): Boolean {
		// we have to do this at the beginning to get 0.0 if we remove first level
		delete.setDistanceToTop()
		
		val entryToRemove = map[delete.price]
		val removedFromSet = if (entryToRemove != null) {
			map.remove(entryToRemove.price)
			set.remove(entryToRemove)
		} else false
		
		if (removedFromSet == false || entryToRemove == null) {
			println("couldn't remove entry with price ${delete.price}, removedFromSet is $removedFromSet and entryToRemove is $entryToRemove")
			delete.decCount()
			return false
		}
		delete.amount = entryToRemove.amount
		adjustTimestamps(delete, entryToRemove)
		if (delete.timestamp >= lastUpdated) lastUpdated = delete.timestamp
		else logger.warn("Entry $delete got deleted but book timestamp $lastUpdated is higher than entry timestamp")
		entryToRemove.decCount()
		engines.addOp(delete)
		return true
	}
	
	
	private fun Double.isZero(op: BookOp) = !isNotZero(op)
	private fun Double.isNotZero(op: BookOp): Boolean {
		if (this == 0.0) {
			println("tried to apply op $op but entry $this now has amount of 0.0")
			return false
		}
		return true
	}
	
	/**
	 * Set the [BookEntry.timestamp] of [entry] to the one of [bookOp] and the [BookOp.timestamp] of [bookOp] to
	 * the difference between the timestamp of [bookOp] and [entry]
	 */
	private fun adjustTimestamps(bookOp: BookOp, entry: BookEntry) {
		val oldTimestamp = entry.timestamp
		entry.timestamp = bookOp.timestamp
		bookOp.survived = bookOp.timestamp - oldTimestamp
	}
	
	private fun BookOp.setDistanceToTop() {
		distanceToTop = if (set.size == 0) {
			0.0
		} else {
			if (this@BookSideImpl.side == Side.Buy) {
				max((set.first().price - this.price).roundPriceSafe(), 0.0)
			} else max((this.price - set.first().price).roundPriceSafe(), 0.0)
		}
	}
}
