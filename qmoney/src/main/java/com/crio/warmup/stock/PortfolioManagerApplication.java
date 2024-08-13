
package com.crio.warmup.stock;

import java.util.ArrayList;
import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.crio.warmup.stock.dto.Candle;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

import com.crio.warmup.stock.dto.PortfolioTrade;

import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PortfolioManagerApplication {
  private static final String API_TOKEN = "b5c13879157d88d03d9ce1ce1fb7e4139b97dc47";

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    // List<String> user = new ArrayList<>();
    // if (args == null || args.length == 0) {
    // System.out.println("No filename is provided.");
    // return user;
    // }
    // List<PortfolioTrade> trades =
    // getObjectMapper().readValue(resolveFileFromResources(args[0]),
    // new TypeReference<List<PortfolioTrade>>() {});
    // for (PortfolioTrade portfolioTrade : trades) {
    // user.add(portfolioTrade.getSymbol());
    // }
    // return user;

    // return Collections.emptyList();

    if (args == null || args.length == 0) {
      System.out.println("No filename is provided.");
      return Collections.emptyList();
    }
    return readTradesFromJson(args[0]).stream().map(PortfolioTrade::getSymbol)
        .collect(Collectors.toList());
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/rutikkulkarni2001-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "22";

    return Arrays.asList(
        new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    // if (args == null || args.length < 2) {
    // throw new IllegalArgumentException("Filename and end date must be
    // provided.");
    // }
    // List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    // LocalDate endDate = LocalDate.parse(args[1]);
    // List<TotalReturnsDto> returnsList = new ArrayList<>();
    // for (PortfolioTrade trade : trades) {
    // // if(trade.getPurchaseDate().getYear()>endDate.getYear())
    // // throw new RuntimeException("End date cant be less than purchase date");
    // if (trade.getPurchaseDate().getYear() > endDate.getYear()) {
    // throw new RuntimeException("Purchase yaar of trade is greater that the end
    // date");
    // }
    // String url = prepareUrl(trade, endDate, API_TOKEN);
    // RestTemplate restTemplate = new RestTemplate();
    // TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
    // if (result != null && result.length > 0) {
    // double closingPrice = result[result.length - 1].getClose();
    // returnsList.add(new TotalReturnsDto(trade.getSymbol(), closingPrice));
    // } else {
    // throw new RuntimeException(
    // "Error fetching data from Tiingo for symbol: " + trade.getSymbol());
    // }
    // }
    // returnsList.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));
    // return
    // returnsList.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());

    if (args == null || args.length < 2) {
      throw new IllegalArgumentException("Filename and end date must be provided.");
    }

    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);

    return trades.stream().map(trade -> {
      try {
        String url = prepareUrl(trade, endDate, API_TOKEN);
        List<Candle> candles = fetchCandles(url);
        return new TotalReturnsDto(trade.getSymbol(), getClosingPriceOnEndDate(candles));
      } catch (Exception e) {
        throw new RuntimeException(
            "Error fetching data from Tiingo for symbol: " + trade.getSymbol(), e);
      }
    }).sorted(Comparator.comparing(TotalReturnsDto::getClosingPrice))
        .map(TotalReturnsDto::getSymbol).collect(Collectors.toList());
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    // File file = resolveFileFromResources(filename);
    // ObjectMapper objectMapper = getObjectMapper();
    // List<PortfolioTrade> trades =
    // objectMapper.readValue(file, new TypeReference<List<PortfolioTrade>>() {});
    // return trades;
    // // return Collections.emptyList();

    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    return objectMapper.readValue(file, new TypeReference<List<PortfolioTrade>>() {
    });
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return String.format(
        "https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",
        trade.getSymbol(), trade.getPurchaseDate(), endDate, token);
    // return "";
    // return Collections.emptyList();
  }

  public static List<Candle> fetchCandles(String url) throws JsonProcessingException {
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(result);
  }

  // public static void main(String[] args) throws Exception {
  // Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  // ThreadContext.put("runId", UUID.randomUUID().toString());
  // printJsonObject(mainReadFile(args));
  // printJsonObject(mainReadQuotes(args));
  // }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    // return 0.0;
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    // return 0.0;
    return candles.get(candles.size() - 1).getClose();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token)
      throws JsonProcessingException {
    // return Collections.emptyList();

    String url = prepareUrl(trade, endDate, token);
    return fetchCandles(url);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    // return Collections.emptyList();

    if (args == null || args.length < 2) {
      throw new IllegalArgumentException("Filename and end date must be provided.");
    }

    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);

    return trades.stream().map(trade -> {
      try {
        List<Candle> candles = fetchCandles(trade, endDate, API_TOKEN);
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

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    // return new AnnualizedReturn("", 0.0, 0.0);
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    Double totalYears = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24;
    Double annualizedReturn = Math.pow(1 + totalReturns, 1 / totalYears) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }

  // public static void main(String[] args) throws Exception {
  // Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  // ThreadContext.put("runId", UUID.randomUUID().toString());

  // // printJsonObject(mainReadFile(args));
  // // printJsonObject(mainReadQuotes(args));

  // printJsonObject(mainCalculateSingleReturn(args));
  // }

  public static String getToken() {
    return API_TOKEN;
  }

  // public static void main(String[] args) throws Exception {
  // Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  // ThreadContext.put("runId", UUID.randomUUID().toString());
  // printJsonObject(mainCalculateSingleReturn(args));
  // }
  // }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) throws Exception {
    // String file = args[0];
    // LocalDate endDate = LocalDate.parse(args[1]);
    // String contents = readFileAsString(file);
    // ObjectMapper objectMapper = getObjectMapper();
    // return
    // portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades),
    // endDate);

    if (args == null || args.length < 2) {
      throw new IllegalArgumentException("Filename and end date must be provided.");
    }
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
    PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(null);
    return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  private static String readFileAsString(String file) {
    return null;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}
