package com.example.demo

import com.example.demo.data.SymbolPriceHistoryDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Component
class Test1 {
    @Autowired
    SymbolPriceHistoryDao symbolPriceHistoryDao
    def df = new SimpleDateFormat('yyyyMMdd')

    @PostConstruct
    void init() {
        def dirName = 'nejat1'

        Thread.start {
            new File(dirName).mkdirs()
            new File(dirName).listFiles().each { it.delete() }
            def g1 = new HashSet()
            def g2 = new HashSet()
            def g = new HashSet()
            def gb = new HashSet()
            def n = new HashSet()
            println "STAETED, FETCHING DATA..."
            def items = symbolPriceHistoryDao.findAllByTime(new Timestamp(df.parse('20200419').time))
            println "DATA READY"

            def safKharid = [:]
            def kharid = [:]
            def safForoosh = [:]
            def lastData = [:]
            def firstData = [:]
            def tamoom = []
            def naakaamHaa = []
            def begaaaHaa = []
            def dateAmmar = [:]
            def dateCandidate = [:]
            def date
            def calcAmar = { ->
                def filter = gb.findAll { g1.contains(it) && g2.contains(it) }
                println " test: date:${date}, g1:${g1.size()}, g2:${g2.size()}, gb:${gb.size()}, filter:${filter} "
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
                        bega: begaaaHaa.findAll { it.date == date },
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
                    g2.clear()
                    g2.addAll(g1)
                    g1.clear()
                    g1.addAll(g)
                    g.clear()
                    gb.clear()
                    n.clear()
                    firstData.clear()

                    date = df.format(tick.time)

                }
                if (isSelectionTimeMinusOne(tick.time)) {
                    if ((tick.last ?: 0) - (firstData[tick.id]?.last ?: 0) < 0) //last < first
                        g.add(tick.id)
                    else
                        g.remove(tick.id)
                }
                if (isSelectionTime(tick.time)) {
                    if ((tick.bestSellVolume ?: 0) - (tick.bestBuyVolume ?: 0) > tick.avgVol5Day) { //saf > avg vol week
                        gb.add(tick.id)
                    } else
                        gb.remove(tick.id)
                }
                if (isSelectionTime2(tick.time)) {
                    if (!firstData.containsKey(tick.id) && tick.last)
                        firstData[tick.id] = tick
                    def q = (tick.bestSellVolume ?: 0) - (tick.bestBuyVolume ?: 0)
                    def volRate = (tick.bestSellVolume ?: 0) / (tick.avgVol5Day ?: tick.avgVol21Day ?: Long.MAX_VALUE)

                    if (q < 1000000 && q > 10 && volRate >= 2)
                        n.add(tick.id)
                    else
                        n.remove(tick.id)
                }
                if (isTradeTime(tick.time)) {
                    if (g2.contains(tick.id) && g1.contains(tick.id) && gb.contains(tick.id)) {
                        if (tick.last) {
                            if (!safKharid.containsKey(tick.id) && !safForoosh.containsKey(tick.id) && n.contains(tick.id)) {
                                safKharid[tick.id] = tick.last
                            }
                            if (safKharid.containsKey(tick.id) && Math.min(tick.last, tick.min10) <= safKharid[tick.id] && !safForoosh.containsKey(tick.id)) {
                                kharid[tick.id] = tick
                                safForoosh[tick.id] = tick.maxAllow
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
