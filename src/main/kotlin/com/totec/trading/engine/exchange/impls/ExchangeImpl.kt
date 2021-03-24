/**
 * Created by ndrsh on 13.06.20
 */

package com.totec.trading.engine.exchange.impls

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.instrument.Instrument
import com.totec.trading.core.networking.RestClient

abstract class ExchangeImpl(override val restClient: RestClient) : Exchange {
	override val instruments: HashMap<String, Instrument> = hashMapOf()
}
