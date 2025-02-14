
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

    static RestTemplate restTemplate = new RestTemplate();


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

      ObjectMapper om = getObjectMapper();
      File file = resolveFileFromResources(args[0]);
      PortfolioTrade[] trade = om.readValue(file, PortfolioTrade[].class);
      List<String> symbols = new ArrayList<>();
      for (PortfolioTrade t : trade) {
          symbols.add(t.getSymbol());

      }
      return symbols;
  }


    public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
        ObjectMapper om = getObjectMapper();
        File file = resolveFileFromResources(args[0]);
        String endDate = args[1];
        PortfolioTrade[] trade = om.readValue(file, PortfolioTrade[].class);

        List<TotalReturnsDto> totalReturnsDto = new ArrayList<>();

        for (PortfolioTrade t : trade) {
            String URI = prepareUrl(t, LocalDate.parse(endDate), "2f8596fb079a6a31332d218111e473546fa385c0");
            TiingoCandle[] tingocandles = restTemplate.getForObject(URI, TiingoCandle[].class);
            TiingoCandle closeStock = tingocandles[tingocandles.length - 1];
            totalReturnsDto.add(new TotalReturnsDto(t.getSymbol(), closeStock.getClose()));
        }

        totalReturnsDto.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));
        return totalReturnsDto.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());



//        return Collections.emptyList();
    }




//    public static final Comparator<TotalReturnsDto> closingPriceComparator = new Comparator<TotalReturnsDto>() {
//        @Override
//        public int compare(TotalReturnsDto o1, TotalReturnsDto o2) {
//            return o1.getClosingPrice().compareTo(o2.getClosingPrice());
//        }
//    };

    // TODO:
    // After refactor, make sure that the tests pass by using these two commands
    // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
    // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
    public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
        return Collections.emptyList();
    }

    // TODO:
    // Build the Url using given parameters and use this function in your code to
    // cann the API.
    public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
        String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
                + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
        return url;
    }



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }



  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "";
    String toStringOfObjectMapper = "";
    String functionNameFromTestFileInStackTrace = "";
    String lineNumberFromTestFileInStackTrace = "";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));



  }
}

