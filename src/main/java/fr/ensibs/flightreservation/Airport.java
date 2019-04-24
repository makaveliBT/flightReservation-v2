package fr.ensibs.flightreservation;

import net.jini.core.entry.Entry;

/**
* An airport entry in the shared space
*
* @author Pascale Launay
*/
public class Airport implements Entry
{
  public String code; // airport code
  public String city; // airport city

  /**
  * No arg constructor
  */
  public Airport() { }

  /**
  * Constructor
  *
  * @param code airport code
  * @param city airport city
  */
  public Airport(String code, String city) {
    this.code = code;
    this.city = city;
  }

  @Override
  public String toString() {
    return "Airport " + code + " " + city;
  }
}
