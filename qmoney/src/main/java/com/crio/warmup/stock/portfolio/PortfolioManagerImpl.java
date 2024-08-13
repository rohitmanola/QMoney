
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    // return null;
    String url = buildUri(symbol, from, to);
    return fetchCandles(url);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate + "&endDate=" + endDate + "&token=b5c13879157d88d03d9ce1ce1fb7e4139b97dc47";
    return uriTemplate;
  }

  private List<Candle> fetchCandles(String url) throws JsonProcessingException {
    TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(result);
  }

  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    Double totalYears = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;
    Double annualizedReturn = Math.pow(1 + totalReturns, 1 / totalYears) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // return null;
    return portfolioTrades.stream().map(trade -> {
      try {
        List<Candle> candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        Double buyPrice = getOpeningPriceOnStartDate(candles);
        Double sellPrice = getClosingPriceOnEndDate(candles);
        return calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      } catch (Exception e) {
        throw new RuntimeException(
            "Error calculating annualized return for symbol: " + trade.getSymbol(), e);
      }
    }).sorted(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed())
        .collect(Collectors.toList());
  }
}
