
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  RestTemplate restTemplate;


  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }



  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
          throws JsonProcessingException, StockQuoteServiceException {
  
      List<Candle> stocksStartToEndDate = new ArrayList<>();
  
      if (from.compareTo(to) >= 0) {
          throw new RuntimeException(); // Needs a message!
      }
  
      String url = buildUri(symbol, from, to);
  
      try {
          String stocks = restTemplate.getForObject(url, String.class);
  
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.registerModule(new JavaTimeModule());
  
          TiingoCandle[] stocksStartToEndDateArray = objectMapper.readValue(stocks, TiingoCandle[].class); // Corrected class
          stocksStartToEndDate = Arrays.asList(stocksStartToEndDateArray);
  
      } catch (NullPointerException e) {
          throw new StockQuoteServiceException(
                  "Error occurred when requesting response from Tiingo API",
                  e.getCause()); // Consider the cause or the entire exception
      }
  
      return stocksStartToEndDate;
  }


  private static ObjectMapper getObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    return om;
  }


  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String TOKEN = "2f8596fb079a6a31332d218111e473546fa385c0";

    String uriTemplate = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?"
                                       + "startDate=%s&endDate=%s&token=%s", symbol, startDate, endDate, TOKEN);
    return uriTemplate;
}



  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.




  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //  1. Update the method signature to match the signature change in the interface.
  //     Start throwing new StockQuoteServiceException when you get some invalid response from
  //     Tiingo, or if Tiingo returns empty results for whatever reason, or you encounter
  //     a runtime exception during Json parsing.
  //  2. Make sure that the exception propagates all the way from
  //     PortfolioManager#calculateAnnualisedReturns so that the external user's of our API
  //     are able to explicitly handle this exception upfront.

  //CHECKSTYLE:OFF


}
