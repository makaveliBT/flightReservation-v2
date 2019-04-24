package fr.ensibs.flightreservation;

import fr.ensibs.river.RiverLookup;
import java.util.Scanner;
import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionManager;

/**
* Distributed flight reservation application using a shared space
*
* @author Pascale Launay
*/
public class Main
{
  /**
  * The options message
  */
  private static final String OPTIONS = "Enter command:"
  + "\n  ADD airport <airport_code> <city>"
  + "\n  ADD flight <flight_code> <departure_airport_code> <arrival_airport_code> <date> <price>"
  + "\n  ADD seat <seat_number> <flight_code>"
  + "\n  ADD account <account_code> <balance>"
  + "\n  FLIGHT <departure_city> <arrival_city> <date>"
  + "\n  SEAT <flight_code> <account_code>"
  + "\n  QUIT";

  /**
  * Shared JavaSpace
  */
  private JavaSpace space;

  /**
  * Transaction manager for the seat reservation
  */
  private TransactionManager transactions;

  /**
  * Print a usage message and exit
  */
  private static void usage()
  {
    System.out.println("Usage: java Main <host> <port>");
    System.exit(-1);
  }

  /**
  * Entry point
  *
  * @param args host port
  */
  public static void main(String[] args) throws Exception
  {
    if (args.length != 2) {
      usage();
    }

    try {
      String host = args[0];
      int port = Integer.parseInt(args[1]);
      Main instance = new Main(host, port);
      instance.run();
    } catch (NumberFormatException e) {
      usage();
    }
  }

  /**
  * Constructor
  *
  * @param host the server host name
  * @param port the server port number
  */
  public Main(String host, int port) throws Exception
  {
    RiverLookup river = new RiverLookup();
    this.space = river.lookup(host, port, JavaSpace.class);
    this.transactions = river.lookup(host, port, TransactionManager.class);
  }

  /**
  * Process the user commands
  */
  public void run() throws Exception
  {
    System.out.println(OPTIONS);
    Scanner scanner = new Scanner(System.in);
    String option = scanner.nextLine().trim();
    while (!"quit".equals(option) && !"QUIT".equals(option)) {
      String[] tokens = option.split(" +");
      if (tokens.length > 1) {
        switch (tokens[0]) {
          case "ADD":
          case "add":
          this.add(tokens);
          break;
          case "FLIGHT":
          case "flight":
          this.flight(tokens);
          break;
          case "SEAT":
          case "seat":
          this.seat(tokens);
          break;
          default: System.out.println("Unknown option: " + tokens[0]);
        }
      }
      option = scanner.nextLine().trim();
    }
  }

  /**
  * Add a new entry in the shared space
  *
  * @param tokens ADD command arguments (see OPTIONS)
  */
  private void add(String[] tokens) throws Exception
  {
    // create the entry to be added from the given arguments
    Entry entry = null;
    if (tokens.length < 4) {
      System.out.println("Missing arguments");
      return;
    }
    try {
      switch (tokens[1]) {
        case "AIRPORT":
        case "airport":
        entry = new Airport(tokens[2], tokens[3]);
        break;
        case "FLIGHT":
        case "flight":
        if (tokens.length < 7) {
          System.out.println("Missing arguments");
          return;
        }
        entry = new Flight(tokens[2], tokens[3], tokens[4], tokens[5], Integer.parseInt(tokens[6]));
        break;
        case "SEAT":
        case "seat":
        entry = new Seat(Integer.parseInt(tokens[2]), tokens[3], true);
        break;
        case "ACCOUNT":
        case "account":
        entry = new Account(tokens[2], Integer.parseInt(tokens[3]));
        break;
        default: System.out.println("Unknown option: " + tokens[0] + " " + tokens[1]);
      }
    } catch (NumberFormatException e) {
      System.out.println("Wrong arguments");
    }
    // add the entry in the shared space
    if (entry != null) {
      this.space.write(entry, null, 3600000);
      System.out.println(tokens[1] + " added");
    }
  }

  /**
  * Look for a flight in the shared space
  *
  * @param tokens FLIGHT command arguments (see OPTIONS)
  */
  private void flight(String[] tokens) throws Exception
  {
    if (tokens.length < 4) {
      System.out.println("Missing arguments");
      return;
    }
    // look for the departure and arrival airports
    String departure = getAirportCode(tokens[1]);
    String arrival = getAirportCode(tokens[2]);

    // look for the flight
    if (departure != null && arrival != null) {
      Entry template = new Flight(null, departure, arrival, tokens[3], null);
      Entry entry = this.space.read(template, null, JavaSpace.NO_WAIT);
      if (entry instanceof Flight) {
        System.out.println("Flight: " + entry);
      } else {
        System.out.println("No flight found");
      }
    }
  }

  /**
  * Look for and reserve a seat in the shared space
  *
  * @param tokens SEAT command arguments (see OPTIONS)
  */
  private void seat(String[] tokens) throws Exception
  {
    if (tokens.length < 3) {
      System.out.println("Missing arguments");
      return;
    }
    // look for the price
    Integer price = getFlightPrice(tokens[1]);
    if (price == null) {
      return;
    }

    // create a transaction
    long transactionId = this.transactions.create(60000).id;
    Transaction transaction = new ServerTransaction(this.transactions, transactionId);

    // look for the seat (take) and reserve it
    Seat seat = reserveSeat(tokens[1], transaction);
    if (seat == null) {
      transaction.abort();
      return;
    }

    boolean payed = paySeat(tokens[2], price, transaction);
    if (!payed) {
      transaction.abort();
      return;
    }
    transaction.commit();
    System.out.println("Seat reserved: " + seat);
  }

  /**
  * Look for an airport entry from its name and give its code
  *
  * @param city the city name
  * @return the city code
  */
  private String getAirportCode(String city) throws Exception
  {
    Entry template = new Airport(null, city);
    Entry entry = this.space.read(template, null, JavaSpace.NO_WAIT);
    if (entry instanceof Airport) {
      return ((Airport)entry).code;
    }
    System.out.println(city + " not found");
    return null;
  }

  /**
  * Look for a flight entry from its code and give its price
  *
  * @param code the flight code
  * @return the flight price
  */
  private Integer getFlightPrice(String code) throws Exception
  {
    Entry template = new Flight(code, null, null, null, null);
    Entry entry = this.space.read(template, null, JavaSpace.NO_WAIT);
    if (entry instanceof Flight) {
      return ((Flight)entry).price;
    }
    System.out.println("No flight found");
    return null;
  }

  /**
  * Look for a seat (take) and reserve it
  *
  * @param code the flight code
  * @param transaction the transaction
  * @return the seat that has been reserved (may be null)
  */
  private Seat reserveSeat(String code, Transaction transaction) throws Exception
  {
    // look for the seat (take)
    Entry template = new Seat(null, code, true);
    Entry entry = this.space.take(template, transaction, JavaSpace.NO_WAIT);
    if (entry instanceof Seat) {
      // reserve and write the seat
      Seat seat = (Seat)entry;
      seat.free = false;
      this.space.write(entry, transaction, 3600000);
      return seat;
    }
    System.out.println("No seat found");
    return null;
  }

  /**
  * Look for an account (take) and withdraw the given amount if its balance is
  * sufficient
  *
  * @param code the account code
  * @param price the amount to be withdrawn
  * @param transaction the transaction
  * @return true if the amount has been withdrawn
  */
  private boolean paySeat(String code, int price, Transaction transaction) throws Exception
  {
    // look for the bank account
    Entry template = new Account(code, null);
    Entry entry = this.space.take(template, transaction, JavaSpace.NO_WAIT);
    int balance = 0;
    if (entry instanceof Account) {
      balance = ((Account)entry).balance;
    } else {
      System.out.println("No account found");
      return false;
    }

    // pay the seat
    if (balance >= price) {
      ((Account)entry).balance = balance - price;
      this.space.write(entry, transaction, 3600000);
      return true;
    }
    System.out.println("Insufficient balance");
    return false;
  }
}
