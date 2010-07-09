//
//  BSE.java
//
//  Salvador Garc�a L�pez
//
//  Created by Salvador Garc�a L�pez 28-11-2005.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package keel.Algorithms.Instance_Selection.BSE;

import keel.Algorithms.Preprocess.Basic.*;

import org.core.*;
import java.util.StringTokenizer;

public class BSE extends Metodo {

 /*Own parameters of the algorithm*/
  private int k;

  public BSE (String ficheroScript) {
    super (ficheroScript);
  }

  public void ejecutar () {

    int i, j, l, m;
    int nClases;
    int claseObt;
    boolean marcas[];
    int nSel = 0;
    double conjS[][];
    double conjR[][];
    int conjN[][];
    boolean conjM[][];
    int clasesS[];
    int bestEval = 0;
    int eval;
    boolean parar = false;
    int peor;

    long tiempo = System.currentTimeMillis();

    /*Inicialization of the flagged instances vector for a posterior copy*/
    marcas = new boolean[datosTrain.length];
    for (i=0; i<datosTrain.length; i++)
      marcas[i] = true;
    nSel = datosTrain.length;

    /*Getting the number of differents classes*/
    nClases = 0;
    for (i=0; i<clasesTrain.length; i++)
      if (clasesTrain[i] > nClases)
        nClases = clasesTrain[i];
    nClases++;

    for (i=0; i<datosTrain.length; i++) {
      claseObt = KNN.evaluacionKNN2(k, datosTrain, realTrain, nominalTrain, nulosTrain, clasesTrain, datosTrain[i], realTrain[i], nominalTrain[i], nulosTrain[i], nClases, distanceEu);
      if (claseObt == clasesTrain[i]) {
        bestEval++;
      }
    }

    /*Body of the algorithm. BSE removes the object with the smallest contribution for the subset quality.*/
    while (!parar) {
      peor = -1;
      for (i=0; i<datosTrain.length; i++) {
        if (marcas[i]) {
          marcas[i] = false;
          nSel--;
          eval = 0;
          /*Building of the S set from the flags*/
          conjS = new double[nSel][datosTrain[0].length];
          conjR = new double[nSel][datosTrain[0].length];
          conjN = new int[nSel][datosTrain[0].length];
          conjM = new boolean[nSel][datosTrain[0].length];
          clasesS = new int[nSel];
          for (j=0, l=0; j<datosTrain.length; j++) {
            if (marcas[j]) { //the instance will be copied to the solution
              for (m=0; m<datosTrain[0].length; m++) {
                conjS[l][m] = datosTrain[j][m];
                conjR[l][m] = realTrain[j][m];
                conjN[l][m] = nominalTrain[j][m];
                conjM[l][m] = nulosTrain[j][m];
              }
              clasesS[l] = clasesTrain[j];
              l++;
            }
          }
          for (j=0; j<datosTrain.length; j++) {
              claseObt = KNN.evaluacionKNN2(k, conjS, conjR, conjN, conjM, clasesS, datosTrain[j], realTrain[j], nominalTrain[j], nulosTrain[j], nClases, distanceEu);
            if (claseObt == clasesTrain[j]) {
              eval++;
            }
          }
          if (eval >= bestEval) {
            peor = i;
            bestEval = eval;
          }
          marcas[i] = true;
          nSel++;
        }
      }
      if (peor >= 0) {
        marcas[peor] = false;
        nSel--;
      } else {
        parar = true;
      }
    }

    /*Building of the S set from the flags*/
    conjS = new double[nSel][datosTrain[0].length];
    conjR = new double[nSel][datosTrain[0].length];
    conjN = new int[nSel][datosTrain[0].length];
    conjM = new boolean[nSel][datosTrain[0].length];
    clasesS = new int[nSel];
    for (i=0, l=0; i<datosTrain.length; i++) {
      if (marcas[i]) { //the instance will be copied to the solution
        for (j=0; j<datosTrain[0].length; j++) {
          conjS[l][j] = datosTrain[i][j];
          conjR[l][j] = realTrain[i][j];
          conjN[l][j] = nominalTrain[i][j];
          conjM[l][j] = nulosTrain[i][j];
        }
        clasesS[l] = clasesTrain[i];
        l++;
      }
    }

    System.out.println("BSE "+ relation + " " + (double)(System.currentTimeMillis()-tiempo)/1000.0 + "s");

                // COn conjS me vale.
                 int trainRealClass[][];
                 int trainPrediction[][];
                
                 trainRealClass = new int[conjS.length][1];
		 trainPrediction = new int[conjS.length][1];	
                
                 //Working on training
                 for ( i=0; i<conjS.length; i++) {
                     trainRealClass[i][0] = clasesS[i];
                     trainPrediction[i][0] = KNN.evaluate(conjS[i],datosTrain, nClases, clasesTrain, this.k);
                 }
                 
                 KNN.writeOutput(ficheroSalida[0], trainRealClass, trainPrediction,  entradas, salida, relation);
                 
                 
                //Working on test
		int realClass[][] = new int[datosTest.length][1];
		int prediction[][] = new int[datosTest.length][1];	
		
		//Check  time		
				
		for (i=0; i<realClass.length; i++) {
			realClass[i][0] = clasesTest[i];
			prediction[i][0]= KNN.evaluate(datosTest[i],conjS, nClases, clasesS, this.k);
		}
                
                KNN.writeOutput(ficheroSalida[1], realClass, prediction,  entradas, salida, relation);
  }

  public void leerConfiguracion (String ficheroScript) {

    String fichero, linea, token;
    StringTokenizer lineasFichero, tokens;
    byte line[];
    int i, j;

    ficheroSalida = new String[2];

    fichero = Fichero.leeFichero (ficheroScript);
    lineasFichero = new StringTokenizer (fichero,"\n\r");

    lineasFichero.nextToken();
    linea = lineasFichero.nextToken();

    tokens = new StringTokenizer (linea, "=");
    tokens.nextToken();
    token = tokens.nextToken();

    /*Getting the names of the training and test files*/
    line = token.getBytes();
    for (i=0; line[i]!='\"'; i++);
    i++;
    for (j=i; line[j]!='\"'; j++);
    ficheroTraining = new String (line,i,j-i);
    for (i=j+1; line[i]!='\"'; i++);
    i++;
    for (j=i; line[j]!='\"'; j++);
    ficheroTest = new String (line,i,j-i);

    /*Getting the path and base name of the results files*/
    linea = lineasFichero.nextToken();
    tokens = new StringTokenizer (linea, "=");
    tokens.nextToken();
    token = tokens.nextToken();

    /*Getting the names of output files*/
    line = token.getBytes();
    for (i=0; line[i]!='\"'; i++);
    i++;
    for (j=i; line[j]!='\"'; j++);
    ficheroSalida[0] = new String (line,i,j-i);
    for (i=j+1; line[i]!='\"'; i++);
    i++;
    for (j=i; line[j]!='\"'; j++);
    ficheroSalida[1] = new String (line,i,j-i);

    /*Getting the number of neighbors*/
    linea = lineasFichero.nextToken();
    tokens = new StringTokenizer (linea, "=");
    tokens.nextToken();
    k = Integer.parseInt(tokens.nextToken().substring(1));

    /*Getting the type of distance function*/
    linea = lineasFichero.nextToken();
    tokens = new StringTokenizer (linea, "=");
    tokens.nextToken();
    distanceEu = tokens.nextToken().substring(1).equalsIgnoreCase("Euclidean")?true:false;    
}

}