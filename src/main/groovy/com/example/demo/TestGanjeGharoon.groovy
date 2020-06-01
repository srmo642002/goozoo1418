package com.example.demo

import com.example.demo.data.SymbolPriceHistory
import com.example.demo.data.SymbolPriceHistoryDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Component
class TestGanjeGharoon {
    def rsiThreshold = 70
    def closingWeek = 1.07 //7%
    def queueVolumeThreshold = 1000000
    def indexMosbat = 1
    def indexManfi = -1

    @Autowired
    SymbolPriceHistoryDao symbolPriceHistoryDao
    def df = new SimpleDateFormat('yyyyMMdd')

    @PostConstruct
    void init() {
        def dirName = 'ganjeGharon'

        Thread.start {
            new File(dirName).mkdirs()
            new File(dirName).listFiles().each { it.delete() }
            def g1 = new HashSet()
            def g = new HashSet()
            def gb = new HashSet()
            println "STAETED, FETCHING DATA..."
            def items = symbolPriceHistoryDao.findAllByTime(new Timestamp(df.parse('20200401').time))
            println "DATA READY"

            def safKharid = [:]
            def kharid = [:]
            def safForoosh = [:]
            def lastData = [:]
            def yesterdayData = [:]
            def firstData = [:]
            def tamoom = []
            def naakaamHaa = []
            def begaaaHaa = []
            def dateAmmar = [:]
            def dateCandidate = [:]
            def date
            def calcAmar = { ->
                def filter = gb.findAll { g1.contains(it) }
                println " test: date:${date}, g1:${g1.size()}, gb:${gb.size()}, filter:${filter} "
                dateCandidate[date] = filter.collect { lastData[it].namad }
                safForoosh.keySet().each {
                    begaaaHaa.add([date: date, tick: lastData[it], kharid: safKharid[it], foroosh: lastData[it].last, ktick: kharid[it], canForoosh: false])
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
                        tamoom: tamoom.findAll { it.date == date }
                ]
                dateAmmar[date].profit = (dateAmmar[date].bega.collect { it.foroosh / it.kharid - 1.014 }.sum() ?: 0) +
                        (dateAmmar[date].tamoom.collect { it.foroosh / it.kharid - 1.014 }.sum() ?: 0)
            }

            items.each { tick ->
                lastData[tick.id] = tick
                if (date != df.format(tick.time)) {

                    if (date) {
                        calcAmar()
                    }
                    g1.clear()
                    g1.addAll(g)
                    g.clear()
                    gb.clear()
                    firstData.clear()

                    date = df.format(tick.time)

                }
                if (isSelectionTimeMinusOne(tick.time)) {
                    yesterdayData[tick.id] = tick
                    if (tick.rsi >= rsiThreshold &&
                            tick.closing / tick.closing_5 >= closingWeek &&
                            (tick.bestBuyVolume ?: 0) - (tick.bestSellVolume ?: 0) >= queueVolumeThreshold)
                        g.add(tick.id)
                    else
                        g.remove(tick.id)
                }
                if (isSelectionTime(tick.time)) { // 8:30 ta 9
                    // todo: index dar tool e bazar alan sabete ,, vase hamin avval be bazar rasad mikonim
                    //rasad baraye navasan giri
                    SymbolPriceHistory tickY = lastData[tick.id]
                    if (tickY) {
                        def q_1 = (tickY.bestBuyVolume ?: 0) - (tickY.bestSellVolume ?: 0)
                        def q = (tick.bestBuyVolume ?: 0) - (tick.bestSellVolume ?: 0)
                        if (tick.indexChange > indexMosbat) { // bazar e mosbat
                            if (q_1 >= tickY.baseVol && q_1 <= (tickY.baseVol ?: 0) * 10)
                                gb.add(tick.id)
                            else
                                gb.remove(tick.id)
                        } else if (tick.indexChange <= indexMosbat && tick.indexChange >= indexManfi) {
                            // bazar moteadel
                            if (q_1 >= (tickY.avgVol21Day ?: tickY.avgVol5Day)
                                    && q >= queueVolumeThreshold)
                                gb.add(tick.id)
                            else
                                gb.remove(tick.id)
                        } else { // bazar manfi
                            if (q_1 >= (tickY.avgVol21Day ?: tickY.avgVol5Day ?: 0) * 5
                                    && q >= queueVolumeThreshold && tick.bestBuyVolume / (tick.bestSellVolume ?: 1) >= 5)
                                gb.add(tick.id)
                            else
                                gb.remove(tick.id)
                        }
                    }

                }
                if (isSelectionTime2(tick.time)) { // 9,10,11


                }
                if (isTradeTime(tick.time)) {
                    if (g1.contains(tick.id) && gb.contains(tick.id)) {
                        if (tick.last) {
                            if (!safKharid.containsKey(tick.id) && !safForoosh.containsKey(tick.id)) {
                                if (tick.indexChange > indexMosbat) { // bazar e mosbat
                                    if (tick.closing / tick.last >= 1)
                                        safKharid[tick.id] = tick.closing / 1.05
                                } else if (tick.indexChange <= indexMosbat && tick.indexChange >= indexManfi) {
                                    // bazar moteadel
                                    if (tick.closing / tick.last >= 1)
                                        safKharid[tick.id] = tick.closing / 1.05
                                } else { // bazar manfi
                                    if (tick.closing / tick.last >= 1.05)
                                        safKharid[tick.id] = tick.minAllow
                                }
                            }
                            if (safKharid.containsKey(tick.id) && Math.min(tick.last, tick.min10) <= safKharid[tick.id] && !safForoosh.containsKey(tick.id)) {
                                kharid[tick.id] = tick
                                if (tick.indexChange > indexMosbat) { // bazar e mosbat
                                    safForoosh[tick.id] = tick.last * 1.05
                                } else if (tick.indexChange <= indexMosbat && tick.indexChange >= indexManfi) {
// bazar moteadel
                                    safForoosh[tick.id] = tick.last * 1.05
                                } else { // bazar manfi
                                    safForoosh[tick.id] = tick.last * 1.05
                                }

                            } else if (safForoosh.containsKey(tick.id) && safKharid.containsKey(tick.id) && Math.max(tick.last, tick.max10) >= safForoosh[tick.id]) {
                                def item = [tick: tick, date: date, kharid: safKharid[tick.id], foroosh: safForoosh[tick.id], ktick: kharid[tick.id]]
                                tamoom.add(item)
                                safForoosh.remove(tick.id)
                                safKharid.remove(tick.id)
                            }
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
            t << "date,naakaam,tamaam,begaaa,profit\n"
            m << "kharid,foroosh,buy,sell,profit,namad\n"
            def p = 0
            dateAmmar.each {
                p += it.value.profit * 100;
                t << "${it.key},${it.value.nakaam.size()},${it.value.tamoom.size()},${it.value.bega.size()},${(it.value.profit * 100).toDouble().round(1)}\n"
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

    boolean isSelectionTimeMinusOne(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        c.get(Calendar.HOUR_OF_DAY) == 12 && c.get(Calendar.MINUTE) >= 29
    }

    boolean isSelectionTime(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        c.get(Calendar.HOUR_OF_DAY) == 8
    }

    boolean isSelectionTime2(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        c.get(Calendar.HOUR_OF_DAY) in [9, 10, 11]
    }

    boolean isTradeTime(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        def h = c.get(Calendar.HOUR_OF_DAY)
        def m = c.get(Calendar.MINUTE)
        (h == 9 && m >= 15) || [10, 11].contains(h) || (h == 12 && m < 15)
    }

    boolean isSettleTime(Timestamp time) {
        def c = Calendar.instance
        c.timeInMillis = time.getTime()
        def h = c.get(Calendar.HOUR_OF_DAY)
        def m = c.get(Calendar.MINUTE)
        (h == 12 && m >= 28 && m <= 30)
    }
}
