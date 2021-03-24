/**
 * Created by ndrsh on 7/16/20
 */

package com.totec.trading.engine.exchange.impls

import com.totec.trading.core.instrument.InstrumentInfo
import com.totec.trading.core.instrument.Size
import com.totec.trading.core.instrument.currencies.CurrencyPair
import com.totec.trading.engine.exchange.instrument.impls.bitmex.InfoTimestamp
import net.openhft.chronicle.bytes.BytesIn
import net.openhft.chronicle.bytes.BytesOut
import javax.inject.Inject

open class InstrumentInfoImpl @Inject constructor(@InfoTimestamp override var timestamp: Long) : InstrumentInfo {
	
	override var symbol: String = ""
	override var pair: CurrencyPair = CurrencyPair.invalid()
	override val lotSize: Size = Size.Lot()
	override val tickSize: Size = Size.Tick()
	
	override fun update(otherInfo: InstrumentInfo): Boolean {
		TODO("no update can be performed on this class")
	}
	
	override fun cleanAfterUpdate() {
		TODO("Not yet implemented")
	}
	
	override fun writeInfoUpdate(bytes: BytesOut<*>) {
		TODO("no update can be performed on this class")
	}
	
	override fun writeInfoFull(bytes: BytesOut<*>) {
		// symbol
		bytes.write8bit("symbol")
		bytes.write8bit(symbol)
		
		// pair
		bytes.write8bit("pair")
		bytes.writeEnum(pair.base)
		bytes.writeEnum(pair.quote)
		bytes.writeEnum(pair.position)
		
		
		//tickSize
		if (tickSize.isInitialized) {
			bytes.write8bit("tickSize")
			bytes.writeDouble(tickSize.value)
		}
		
		bytes.write8bit("end")
	}
	
	override fun calculateLag(): Long {
		TODO("Not yet implemented")
	}
	
	override fun readInfo(bytes: BytesIn<*>, readTimestamp: Long) {
		timestamp = readTimestamp
		while (true) {
			when (bytes.read8bit()) {
				"symbol" -> bytes.setSymbol()
				"pair" -> bytes.setPair()
				"lotSize" -> lotSize.initializeWith(bytes.readDouble())
				"tickSize" -> tickSize.initializeWith(bytes.readDouble())
				"end" -> return
			}
		}
	}
	
	override fun appendTo(stringBuilder: StringBuilder) {
		if (lotSize.isInitialized) stringBuilder.append("lotSize:${lotSize.value}")
		if (tickSize.isInitialized) stringBuilder.append(",tickSize:${tickSize.value}")
	}
}
