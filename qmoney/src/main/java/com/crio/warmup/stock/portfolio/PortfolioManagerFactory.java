
package com.crio.warmup.stock.portfolio;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    // return null;
    return new PortfolioManagerImpl(restTemplate);
  }

}
