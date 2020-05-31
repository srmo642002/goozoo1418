package com.example.demo.data


import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

import java.sql.Timestamp

interface SymbolPriceHistoryDao extends CrudRepository<SymbolPriceHistory, String> {

    @Query('from SymbolPriceHistory s where s.time > :time order by s.time')
    List<SymbolPriceHistory> findAllByTime(@Param('time') Timestamp time)
}
