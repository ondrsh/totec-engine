/**
 * Created by ndrsh on 6/19/20
 */

package com.totec.trading.engine.exchange.impls.gdax

import com.totec.trading.core.exchanges.Exchange
import com.totec.trading.core.exchanges.SymbolConverter
import com.totec.trading.core.instrument.InfoDeserializer
import com.totec.trading.core.networking.InstrumentInfoDeserializer
import com.totec.trading.engine.exchange.instrument.impls.coinbase.CoinbaseInfoDeserializer
import com.totec.trading.engine.exchange.instrument.impls.coinbase.CoinbaseInstrumentInfoDeserializer
import dagger.Module
import dagger.Provides

@Module
class CoinbaseExchangeModule {
	@Provides fun provideExchange(coinbaseExchange: CoinbaseExchange): Exchange = coinbaseExchange
	@Provides fun provideInstrumentInfoDeserializer(cIID: CoinbaseInstrumentInfoDeserializer): InstrumentInfoDeserializer = cIID
	@Provides fun provideInfoDeserializer(cIID: CoinbaseInfoDeserializer): InfoDeserializer = cIID
	@Provides fun provideSymbolConverter(coinbaseSymbolConverter: CoinbaseSymbolConverter): SymbolConverter = coinbaseSymbolConverter
}
