package fr.ensibs.flightreservation;

import net.jini.core.entry.Entry;

/**
* A bank account entry in the shared space
*
* @author Pascale Launay
*/
public class Account implements Entry
{
  public String code;     // bank account code
  public Integer balance; // bank account balance

  /**
  * No arg constructor
  */
  public Account() { }

  /**
  * Constructor
  *
  * @param code bank account code
  * @param balance bank account balance
  */
  public Account(String code, Integer balance) {
    this.code = code;
    this.balance = balance;
  }

  @Override
  public String toString() {
    return "Account " + code + " " + balance;
  }
}
