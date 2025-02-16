
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static RestTemplate restTemplate = new RestTemplate();

  public static PortfolioManager portfolioManager =
      PortfolioManagerFactory.getPortfolioManager(restTemplate);


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);

    // Extract symbols from trades
    List<String> symbols = new ArrayList<>();
    for (PortfolioTrade trade : trades) {
      symbols.add(trade.getSymbol());
    }

    return symbols;
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
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/wroxtaar-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@3bf7ca37";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
    File file = resolveFileFromResources(args[0]);
    String endDate = args[1];
    PortfolioTrade[] trade = om.readValue(file, PortfolioTrade[].class);

    List<TotalReturnsDto> totalReturnsDto = new ArrayList<>();

    for (PortfolioTrade t : trade) {
      String URI =
          prepareUrl(t, LocalDate.parse(endDate), "2f8596fb079a6a31332d218111e473546fa385c0");
      TiingoCandle[] tingocandles = restTemplate.getForObject(URI, TiingoCandle[].class);
      TiingoCandle closeStock = tingocandles[tingocandles.length - 1];
      totalReturnsDto.add(new TotalReturnsDto(t.getSymbol(), closeStock.getClose()));
    }

    totalReturnsDto.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));
    return totalReturnsDto.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);

    return Arrays.asList(trades);
  }


  // TODO:
  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
    return url;
  }



  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  public static String getToken() {
    return "2f8596fb079a6a31332d218111e473546fa385c0";
  }

  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {

    String URI = prepareUrl(trade, endDate, token);
    TiingoCandle[] tingocandles = restTemplate.getForObject(URI, TiingoCandle[].class);

    return Arrays.asList(tingocandles);

  }



  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    ObjectMapper om = getObjectMapper();
    File file = resolveFileFromResources(args[0]);
    String endDate = args[1];
    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);

    List<AnnualizedReturn> result = new ArrayList<>();

    for (PortfolioTrade trade : trades) {
      String URI =
          prepareUrl(trade, LocalDate.parse(endDate), "2f8596fb079a6a31332d218111e473546fa385c0");
      Candle[] candlesArray = restTemplate.getForObject(URI, TiingoCandle[].class);
      List<Candle> candlesList = Arrays.asList(candlesArray);

      Double buyPrice = getOpeningPriceOnStartDate(candlesList);
      Double sellPrice = getClosingPriceOnEndDate(candlesList);

      AnnualizedReturn annualreturn =
          calculateAnnualizedReturns(LocalDate.parse(endDate), trade, buyPrice, sellPrice);

      result.add(annualreturn);
      // totalReturnsDto.add(new TotalReturnsDto(t.getSymbol(), closeStock.getClose()));
    }

    return result;
    // totalReturnsDto.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));
    // return totalReturnsDto.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());


    // return Collections.emptyList();
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {

    String symbol = trade.getSymbol();
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    LocalDate startDate = trade.getPurchaseDate();
    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
    Double total_num_years = daysBetween / 365.25; // Convert days to years
    Double annualizedReturns = Math.pow(1 + totalReturns, 1 / total_num_years) - 1;

    return new AnnualizedReturn(symbol, annualizedReturns, totalReturns);
  }



  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Once you are done with the implementation inside PortfolioManagerImpl and
  // PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  // Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  // call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  private static String readFileAsString(String filename) throws URISyntaxException, IOException {

    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()), "UTF-8");
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    String contents = readFileAsString(file);
    ObjectMapper objectMapper = getObjectMapper();

    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);

    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

