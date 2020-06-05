package com.example.demo.data

class AlgConfig {

    String name
    double fromIndex
    double toIndex
    double rsiThreshold = 70
    double weekChangePercentage = 1.07
    double qSobThreshold = 1000000
    double qYesterdayThreshold = 1000000
    double qSobToAvgMonth = 0
    double qYesterdayToAvgMonth = 1.0
    double qSobToBaseVol = 0
    double qYesterdayToBaseVol = 0
    double buy = 1.05
    double sell = 1.05
    boolean buyOnMinAllow = false
    boolean sellOnMaxAllow = false
    boolean stopSellIfBuyQueue = false
}
