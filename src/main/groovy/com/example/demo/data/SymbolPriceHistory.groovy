package com.example.demo.data

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import java.sql.Timestamp

@Entity
class SymbolPriceHistory {
    @Id
    @GeneratedValue
    long hid
    String id
    String namad
    String market
    String type
    String marketType
    Integer last
    Integer closing
    Integer yesterday
    Double closing10
    Double max10
    Double min10
    Double max
    Double min
    Long totalVolume
    Long avgVol5Day
    Long avgVol21Day
    Long baseVol
    Integer bestBuy
    Integer bestSell
    Integer bestBuyVolume
    Integer bestSellVolume
    Integer bestBuyCount
    Integer bestSellCount
    Integer avgBestBuy
    Integer avgBestSell
    Integer avgBestBuyVolume
    Integer avgBestSellVolume
    Integer avgBestBuyCount
    Integer avgBestSellCount
    String qStatus
    String status
    Double minAllow
    Double maxAllow
    Double indexChange

    Timestamp time //used only for test

    @Override
    public SymbolPriceHistory clone() throws CloneNotSupportedException {
        return new SymbolPriceHistory(
                id: this.id,
                namad: this.namad,
                market: this.market,
                type: this.type,
                marketType: this.marketType,
                last: this.last,
                closing: this.closing,
                yesterday: this.yesterday,
                closing10: this.closing10,
                max10: this.max10,
                min10: this.min10,
                max: this.max,
                min: this.min,
                bestBuy: this.bestBuy,
                bestSell: this.bestSell,
                bestBuyVolume: this.bestBuyVolume,
                bestSellVolume: this.bestSellVolume,
                bestBuyCount: this.bestBuyCount,
                bestSellCount: this.bestSellCount,
                avgBestBuy: this.avgBestBuy,
                avgBestSell: this.avgBestSell,
                avgBestBuyVolume: this.avgBestBuyVolume,
                avgBestSellVolume: this.avgBestSellVolume,
                avgBestBuyCount: this.avgBestBuyCount,
                avgBestSellCount: this.avgBestSellCount,
                qStatus: this.qStatus,
                status: this.status,
                minAllow: this.minAllow,
                maxAllow: this.maxAllow,
                totalVolume: this.totalVolume,
                avgVol5Day: this.avgVol5Day,
                avgVol21Day: this.avgVol21Day,
                baseVol: this.baseVol,
                indexChange: this.indexChange,
                time: this.time
        )
    }

    @Override
    public String toString() {
        return "SymbolPriceHistory{" +
                "namad='" + namad + '\'' +
                ", last=" + last +
                ", closing=" + closing +
                ", yesterday=" + yesterday +
                ", bestBuy=" + bestBuy +
                ", bestSell=" + bestSell +
                ", bestBuyVolume=" + bestBuyVolume +
                ", bestSellVolume=" + bestSellVolume +
                ", qStatus='" + qStatus + '\'' +
                ", status='" + status + '\'' +
                ", minAllow=" + minAllow +
                ", maxAllow=" + maxAllow +
                ", closing10=" + closing10 +
                ", min10=" + min10 +
                ", max10=" + max10 +
                '}';
    }
}
