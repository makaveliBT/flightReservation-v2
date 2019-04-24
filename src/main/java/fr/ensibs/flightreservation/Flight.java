package fr.ensibs.flightreservation;

import net.jini.core.entry.Entry;

/**
* A flight entry in the shared space
*
* @author Pascale Launay
*/
public class Flight implements Entry
{
  public String code;      // flight code
  public String departure; // departure airport code
  public String arrival;   // arrival airport code
  public String date;      // departure date
  public Integer price;    // flight price

  /**
  * No arg constructor
  */
  public Flight() { }

  /**
  * Constructor
  *
  * @param code flight code
  * @param departure departure airport code
  * @param arrival arrival airport code
  * @param date departure date
  * @param price flight price
  */
  public Flight(String code, String departure, String arrival, String date, Integer price) {
    this.code = code;
    this.departure = departure;
    this.arrival = arrival;
    this.date = date;
    this.price = price;
  }

  @Override
  public String toString() {
    return "Flight " + code + " " + departure + " " + arrival + " " + date + " " + price;
  }
}
