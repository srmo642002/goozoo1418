package com.example.demo.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.sql.Timestamp

interface SymbolPriceHistoryDao extends JpaRepository<SymbolPriceHistory, String> {

    @Query('from SymbolPriceHistory s where s.time > :time order by s.time')
    List<SymbolPriceHistory> findAllByTime(@Param('time') Timestamp time)
}
