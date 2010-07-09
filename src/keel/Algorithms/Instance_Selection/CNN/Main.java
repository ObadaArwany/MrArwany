/**
 * 
 * File: Main.java
 * 
 * This is the main class of the algorithm.
 * It gets the configuration script, builds the algorithm and executes it.
 * 
 * @author Written by Salvador Garc�a (University of Granada) 20/07/2004 
 * @version 0.1 
 * @since JDK1.5
 * 
 */
package keel.Algorithms.Instance_Selection.CNN;

public class Main {

	/** 
	 * <p>
	 * The main method of the class
	 * </p> 
	 * 
	 * @param args Arguments of the program (a configuration script, generally)  
	 * 
	 */
  public static void main (String args[]) {

    CNN cnn;

    if (args.length != 1)
      System.err.println("Error. A parameter is only needed.");
    else {
      cnn = new CNN (args[0]);
      cnn.ejecutar();
    }
	
  } //end-method 
  
}//end-class