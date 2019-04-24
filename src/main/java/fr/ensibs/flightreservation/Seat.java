package fr.ensibs.flightreservation;

import net.jini.core.entry.Entry;

/**
* A seat entry in the shared space
*
* @author Pascale Launay
*/
public class Seat implements Entry
{
  public Integer number;    // seat number in the flight
  public String flightCode; // flight code
  public Boolean free;      // true if the seat is not reserved

  /**
  * No arg constructor
  */
  public Seat() { }

  /**
  * Constructor
  *
  * @param number seat number in the flight
  * @param code flight code
  * @param free true if the seat is not reserved
  */
  public Seat(Integer number, String flightCode, Boolean free) {
    this.number = number;
    this.flightCode = flightCode;
    this.free = free;
  }

  @Override
  public String toString() {
    return "Seat " + number + " " + flightCode + " " + free;
  }
}
