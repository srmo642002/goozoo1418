package com.example.demo

import com.example.demo.data.AlgConfig
import com.example.demo.data.SymbolPriceHistoryDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Component
class TestGanjeGharoon {
    def config = [
            new AlgConfig(name: 'khey-manfi', fromIndex: -5, toIndex: -2, rsiThreshold: 70, weekChangePercentage: 1.15, qSobThreshold: 2000000, qYesterdayThreshold: 1000000, qSobToAvgMonth: 5, qYesterdayToAvgMonth: 5, qSobToBaseVol: 0, qYesterdayToBaseVol: 0, buy: 1.05, sell: 1.05, buyOnMinAllow: true, sellOnMaxAllow: false, stopSellIfBuyQueue: false),
            new AlgConfig(name: 'manfi', fromIndex: -2, toIndex: -1, rsiThreshold: 70, weekChangePercentage: 1.09, qSobThreshold: 1000000, qYesterdayThreshold: 1000000, qSobToAvgMonth: 3, qYesterdayToAvgMonth: 3, qSobToBaseVol: 0, qYesterdayToBaseVol: 0, buy: 1.05, sell: 1.05, buyOnMinAllow: true, sellOnMaxAllow: false, stopSellIfBuyQueue: false),
            new AlgConfig(name: 'normal', fromIndex: -1, toIndex: 1, rsiThreshold: 70, weekChangePercentage: 1.07, qSobThreshold: 1000000, qYesterdayThreshold: 1000000, qSobToAvgMonth: 0, qYesterdayToAvgMonth: 1, qSobToBaseVol: 0, qYesterdayToBaseVol: 0, buy: 1.05, sell: 1.05, buyOnMinAllow: false, sellOnMaxAllow: false, stopSellIfBuyQueue: false),
            new AlgConfig(name: 'mosbat', fromIndex: 1, toIndex: 2, rsiThreshold: 0, weekChangePercentage: 1, qSobThreshold: 0, qYesterdayThreshold: 0, qSobToAvgMonth: 0, qYesterdayToAvgMonth: 0, qSobToBaseVol: 0, qYesterdayToBaseVol: 0, buy: 1.05, sell: 1.05, buyOnMinAllow: false, sellOnMaxAllow: false, stopSellIfBuyQueue: true),
            new AlgConfig(name: 'khey-mosbat', fromIndex: 2, toIndex: 5, rsiThreshold: 0, weekChangePercentage: 0, qSobThreshold: 0, qYesterdayThreshold: 0, qSobToAvgMonth: 0, qYesterdayToAvgMonth: 0, qSobToBaseVol: 0, qYesterdayToBaseVol: 0, buy: 1.05, sell: 1.05, buyOnMinAllow: false, sellOnMaxAllow: false, stopSellIfBuyQueue: true),
    ]

    @Autowired
    SymbolPriceHistoryDao symbolPriceHistoryDao
    def df = new SimpleDateFormat('yyyyMMdd')

    @PostConstruct
    void init() {
        def dirName = 'ganjeGharon'

        Thread.start {
            new File(dirName).mkdirs()
            new File(dirName).listFiles().each { it.delete() }
            def g = new HashSet()
            def safKharid = [:]
            def kharid = [:]
            def safForoosh = [:]
            def lastData = [:]
            def tamoom = []
            def naakaamHaa = []
            def begaaaHaa = []
            def mandeHaa = [:]
            def dateAmmar = [:]
            def dateCandidate = [:]
            def date
            def dateIndex
            def calcAmar = { ->
                dateCandidate[date] = g.collect { lastData[it].namad }
                safForoosh.keySet().each {
                    if (lastData[it].bestBuyVolume > lastData[it].bestSellVolume)
                        begaaaHaa.add([date: date, tick: lastData[it], kharid: safKharid[it], foroosh: lastData[it].bestBuy, ktick: kharid[it], canForoosh: false])
                    else
                        mandeHaa[it] = [date: date, tick: lastData[it], kharid: safKharid[it], foroosh: lastData[it].bestBuy, ktick: kharid[it], canForoosh: false]
                    safKharid.remove(it)
                }
                safKharid.keySet().each {
                    naakaamHaa.add([date: date, tick: lastData[it], kharid: safKharid[it]])
                }
                safKharid.clear()
                safForoosh.clear()

                dateAmmar[date] = [
                        bega  : begaaaHaa.findAll { it.date == date },
                        nakaam: naakaamHaa.findAll { it.date == date },
                        tamoom: tamoom.findAll { it.date == date },
                        index : dateIndex
                ]
                def amaar = [
                        bega  : dateAmmar[date].bega.size(),
                        nakaam: dateAmmar[date].nakaam.size(),
                        tamoom: dateAmmar[date].tamoom.size(),
                        index : dateIndex
                ]
                dateAmmar[date].profit = (dateAmmar[date].bega.collect { it.foroosh / it.kharid - 1.014 }.sum() ?: 0) +
                        (dateAmmar[date].tamoom.collect { it.foroosh / it.kharid - 1.014 }.sum() ?: 0)
                println "date summeries: date:${date}, namads:${g.size()}, amar: ${amaar} "
                dateIndex = 0
            }
            def page = 0
            def maxPage
            AlgConfig algConfig
            def lastIndex
            while (!maxPage || page < maxPage) {
                def items = symbolPriceHistoryDao.findAll(PageRequest.of(page, 500000/*, Sort.Direction.ASC, 'time'*/))
                maxPage = items.totalPages
                println "${page}/${maxPage}: tamom:${tamoom.size()}, montazere-foroosh:${safForoosh.size() + mandeHaa.size()}, montazere-kharid:${safKharid.size()}"
                page++
                items.content.each { tick ->
                    lastData[tick.id] = tick
                    if (date != df.format(tick.time)) {

                        if (date) {
                            calcAmar()
                        }
                        g.clear()
                        date = df.format(tick.time)
                        dateIndex = tick.indexChange

                    }
                    if (isTradeTime(tick.time)) {
                        if (!algConfig || lastIndex != tick.indexChange) {
                            algConfig = config.find { it.fromIndex <= tick.indexChange && it.toIndex > tick.indexChange }
                            lastIndex = tick.indexChange
                        }
                        if (algConfig &&
                                (tick.rsi ?: 0) >= algConfig.rsiThreshold &&
                                (tick.yesterday ?: 0) / (tick.closing_5 ?: 1) >= algConfig.weekChangePercentage &&
                                (tick.queueSob ?: 0) >= algConfig.qSobThreshold &&
                                (tick.queueYesterday ?: 0) >= algConfig.qYesterdayThreshold &&
                                (tick.queueSob ?: 0) / (tick.avgVol21Day ?: 1) >= algConfig.qSobToAvgMonth &&
                                (tick.queueYesterday ?: 0) / (tick.avgVol21Day ?: 1) >= algConfig.qYesterdayToAvgMonth &&
                                (tick.queueSob ?: 0) / (tick.baseVol ?: 1) >= algConfig.qSobToBaseVol &&
                                (tick.queueYesterday ?: 0) / (tick.avgVol21Day ?: 1) >= algConfig.qYesterdayToBaseVol)
                            g.add(tick.id)
                        else g.remove(tick.id)
                        if (mandeHaa[tick.id] && tick.bestBuyVolume > tick.bestSellVolume) {
                            begaaaHaa.add([date: date, tick: tick, kharid: mandeHaa[tick.id].kharid, foroosh: tick.bestBuy, ktick: mandeHaa[tick.id].ktick, canForoosh: true])
                            mandeHaa.remove(tick.id)
                        }

                        if (tick.last) {
                            if (!safKharid.containsKey(tick.id) && !safForoosh.containsKey(tick.id) && g.contains(tick.id)) {
                                if (tick.closing / tick.last >= 1) {
                                    safKharid[tick.id] = algConfig.buyOnMinAllow ? tick.minAllow : (tick.closing / algConfig.buy)
                                }

                            }
                            if (safKharid.containsKey(tick.id) && Math.min(tick.last, tick.min10) <= safKharid[tick.id] && !safForoosh.containsKey(tick.id)) {
                                kharid[tick.id] = tick
                                safForoosh[tick.id] = algConfig.sellOnMaxAllow ? tick.maxAllow : (safKharid[tick.id] * algConfig.sell)
                            } else if (safForoosh.containsKey(tick.id) && safKharid.containsKey(tick.id) && Math.max(tick.last, tick.max10) >= safForoosh[tick.id]) {
                                def item = [tick: tick, date: date, kharid: safKharid[tick.id], foroosh: safForoosh[tick.id], ktick: kharid[tick.id]]
                                tamoom.add(item)
                                safForoosh.remove(tick.id)
                                safKharid.remove(tick.id)
                            }
                        }

                    }
                    if (isSettleTime(tick.time)) {

                        if (safForoosh.containsKey(tick.id)) {
                            begaaaHaa.add([date: date, tick: tick, kharid: safKharid[tick.id], foroosh: tick.last, ktick: kharid[tick.id], canForoosh: tick.bestBuyVolume ? true : false])
                            safForoosh.remove(tick.id)
                            safKharid.remove(tick.id)
                        } else if (safKharid.containsKey(tick.id)) {
                            naakaamHaa.add([date: date, tick: tick, kharid: safKharid[tick.id]])
                            safKharid.remove(tick.id)
                        }
                    }
                }

            }
            calcAmar()
            def ns = new File("${dirName}/namads.csv")
            def t = new File("${dirName}/totals.csv")
            def m = new File("${dirName}/details.csv")
            ns << "date,namad\n"

            dateCandidate.each { dt, value ->
                value.each {
                    ns << "${dt},${it}\n"
                }
            }
            t << "date,index,naakaam,tamaam,begaaa,profit\n"
            m << "kharid,foroosh,buy,sell,profit,namad\n"
            def p = 0
            dateAmmar.each {
                p += it.value.profit * 100;
                t << "${it.key},${Math.round((it.value.index ?: 0) * 10) / 10},${it.value.nakaam.size()},${it.value.tamoom.size()},${it.value.bega.size()},${(it.value.profit * 100).toDouble().round(1)}\n"
                it.value.tamoom.each {
                    m << "${it.ktick.time},${it.tick.time},${it.kharid as int},${it.foroosh as int},${(((it.foroosh ?: it.kharid) / it.kharid - 1.014) * 100).toDouble().round(1)},${it.tick.namad}\n"
                }
                it.value.bega.each {
                    m << "${it.ktick.time},${it.canForoosh ? it.tick.time : '-'},${it.kharid as int},${it.foroosh as int},${(((it.foroosh ?: it.kharid) / it.kharid - 1.014) * 100).toDouble().round(1)},${it.tick.namad}\n"
                }
            }
            def tt = "TOTAL,${naakaamHaa.size()},${tamoom.size()},${begaaaHaa.size()},${p.toDouble().round(1)}\n"
            println tt
            t << tt
            println "tamoom"
        }


    }

    boolean isTradeTime(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        def h = c.get(Calendar.HOUR_OF_DAY)
        def m = c.get(Calendar.MINUTE)
        (h == 9 && m >= 10) || [10, 11].contains(h) || (h == 12 && m < 10)
    }

    boolean isSettleTime(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        def h = c.get(Calendar.HOUR_OF_DAY)
        def m = c.get(Calendar.MINUTE)
        (h == 12 && m >= 20 && m <= 30)
    }
}
