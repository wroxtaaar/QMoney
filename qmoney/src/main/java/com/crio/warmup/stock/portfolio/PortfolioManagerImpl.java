
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
import java.util.Collections;
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



  RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
  this.restTemplate = restTemplate;
  }



  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

    if (from.compareTo(to) >= 0) {
      throw new RuntimeException();
    }

    String uri = buildUri(symbol, from, to);
    TiingoCandle[] candlesArray = restTemplate.getForObject(uri, TiingoCandle[].class);

    if (candlesArray == null) {
      return new ArrayList<Candle>();
    } else {
      List<Candle> stocksList = Arrays.asList(candlesArray);
      return stocksList;
    }

  }



  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "2f8596fb079a6a31332d218111e473546fa385c0";

    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
        .replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
    return url;
  }



  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // TODO Auto-generated method stub

    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();

    for (int i = 0; i < portfolioTrades.size(); i++) {

      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i), endDate);
      annualizedReturns.add(annualizedReturn);
    }

    Comparator<AnnualizedReturn> SortByAnnReturn =
        Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns, SortByAnnReturn);

    return annualizedReturns;

  }



  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {

    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    try {

      List<Candle> stocksStartToEndFull = getStockQuote(symbol, startDate, endDate);

      Candle stockStartDate = stocksStartToEndFull.get(0);
      Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);

      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stocksLatest.getClose();

      Double totalReturn = (sellPrice - buyPrice) / buyPrice;

      Double numYears = (double) ChronoUnit.DAYS.between(startDate, endDate) / 365.24;

      Double annualizedReturns = Math.pow((1 + totalReturn), (1 / numYears)) - 1;


      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturn);

    } catch (JsonProcessingException e) {
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }

    return annualizedReturn;

  }

}
