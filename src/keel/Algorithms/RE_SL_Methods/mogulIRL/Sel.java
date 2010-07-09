package keel.Algorithms.RE_SL_Methods.mogulIRL;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.io.*;
import org.core.*;
import java.util.*;
import java.lang.Math;

class Sel {
        public double semilla;
        public long cont_soluciones;
        public long Gen, n_genes, n_reglas, n_generaciones;
        public int n_soluciones;

        public String fich_datos_chequeo, fich_datos_tst;
        public String fichero_conf, ruta_salida;
        public String fichero_br, fichero_reglas, fich_tra_obli, fich_tst_obli;
        public String informe = "";
        public String datos_inter = "";
        public String cadenaReglas = "";

        public MiDataset tabla, tabla_tst;
    public BaseR_Sel base_reglas;
    public BaseR_Sel base_total;
    public Adap_Sel fun_adap;
    public AG_Sel alg_gen;

        public Sel (String f_e, MiDataset train, MiDataset test) {
                fichero_conf = f_e;
                this.tabla = train;
		this.tabla_tst = test;
        }


        private String Quita_blancos(String cadena) {
                StringTokenizer sT = new StringTokenizer(cadena, "\t ", false);
                return (sT.nextToken());
        }


        /** Reads the data of the configuration file */
        public void leer_conf (){
                int i, j;
                String cadenaEntrada, valor;
                double cruce, mutacion, porc_num_reglas, alfa, tau;
                int tipo_fitness, long_poblacion;


                // we read the file in a String
                cadenaEntrada = Fichero.leeFichero(fichero_conf);
                StringTokenizer sT = new StringTokenizer(cadenaEntrada, "\n\r=", false);

                // we read the algorithm's name
                sT.nextToken();
                sT.nextToken();

                // we read the name of the training and test files
                sT.nextToken();
                valor = sT.nextToken();

                StringTokenizer ficheros = new StringTokenizer(valor, "\t ", false);
                ficheros.nextToken();
                fich_datos_chequeo = ((ficheros.nextToken()).replace('\"',' ')).trim();
                fich_datos_tst = ((ficheros.nextToken()).replace('\"',' ')).trim();
//                fichero_br = ((ficheros.nextToken()).replace('\"',' ')).trim();

                // we read the name of the output files
                sT.nextToken();
                valor = sT.nextToken();

                ficheros = new StringTokenizer(valor, "\t ", false);
                fich_tra_obli = ((ficheros.nextToken()).replace('\"',' ')).trim();
                fich_tst_obli = ((ficheros.nextToken()).replace('\"',' ')).trim();
//                fichero_reglas = ((ficheros.nextToken()).replace('\"',' ')).trim();
                fichero_br = ((ficheros.nextToken()).replace('\"',' ')).trim(); //Br inicial
                String aux = ((ficheros.nextToken()).replace('\"',' ')).trim(); //BD
                fichero_reglas = ((ficheros.nextToken()).replace('\"',' ')).trim();	//BR salida de Select
                aux =  ((ficheros.nextToken()).replace('\"',' ')).trim(); //BR salida de Tuning
                ruta_salida = fich_tst_obli.substring(0, fich_tst_obli.lastIndexOf('/') + 1);


                // we read the seed of the random generator
                sT.nextToken();
                valor = sT.nextToken();
                semilla = Double.parseDouble(valor.trim());
                Randomize.setSeed((long) semilla);;

                for (i = 0; i < 6; i++){
                        sT.nextToken(); //variable
                        sT.nextToken(); //valor
                }

                // we read the Number of Iterations
                sT.nextToken();
                valor = sT.nextToken();
                n_generaciones = Long.parseLong(valor.trim());

                // we read the Population Size
                sT.nextToken();
                valor = sT.nextToken();
                long_poblacion = Integer.parseInt(valor.trim());

                // we read the Tau parameter for the minimun maching degree required to the KB
                sT.nextToken();
                valor = sT.nextToken();
                tau = Double.parseDouble(valor.trim());

                // we read the Rate of rules to estimate the niche radio
                sT.nextToken();
                valor = sT.nextToken();
                porc_num_reglas = Double.parseDouble(valor.trim());

                // we read the Alfa parameter for the Power Law
                sT.nextToken();
                valor = sT.nextToken();
                alfa = Double.parseDouble(valor.trim());

                // we read the Type of Fitness Function
                sT.nextToken();
                valor = sT.nextToken();
                tipo_fitness = Integer.parseInt(valor.trim());

                n_soluciones = 1;

                // we read the Cross Probability
                sT.nextToken();
                valor = sT.nextToken();
                cruce = Double.parseDouble(valor.trim());

                // we read the Mutation Probability
                sT.nextToken();
                valor = sT.nextToken();
                mutacion = Double.parseDouble(valor.trim());


                // we create all the objects
//                tabla = new MiDataset(fich_datos_chequeo, true);
//                if (tabla.salir==false) {
                tabla.nuevaTabla();
		tabla_tst.nuevaTabla();
                base_total = new BaseR_Sel(fichero_br, tabla);
                base_reglas = new BaseR_Sel(base_total.n_reglas, tabla);
                fun_adap = new Adap_Sel(tabla, base_reglas, base_total, porc_num_reglas, n_soluciones, tau, alfa, tipo_fitness);
                alg_gen = new AG_Sel(long_poblacion, base_total.n_reglas, cruce, mutacion, fun_adap);
//            }
        }


        public void run () {
                int i, j;
                double ec, el, min_CR, ectst, eltst;

                /* We read the configutate file and we initialize the structures and variables */
                leer_conf();

                if (tabla.salir==false) {
                /* Inicialization of the solution counter */
                cont_soluciones = 0;

                do {

                        /* Generation of the initial population */
                        alg_gen.Initialize ();
                        Gen = 0;

                        /* Evaluation of the initial population */
                        alg_gen.Evaluate ();
                        Gen++;

                        /* Main of the genetic algorithm */
                        do {
                                /* Interchange of the new and old population */
                                alg_gen.Intercambio();

                                /* Selection by means of Baker */
                                alg_gen.Select ();

                                /* Crossover */
                                alg_gen.Cruce_Multipunto ();

                                /* Mutation */
                                alg_gen.Mutacion_Uniforme ();

                                /* Elitist selection */
                                alg_gen.Elitist ();

                                /* Evaluation of the current population */
                                alg_gen.Evaluate ();

                                /* we increment the counter */
                                Gen++;

                                fun_adap.Decodifica (alg_gen.solucion());
                                fun_adap.Error_tra ();
                                ec = fun_adap.EC;
                                System.out.println (" Iteration=" + (Gen-1) + " MSE=" + ec + " " + " #R=" + base_reglas.n_reglas);
                        } while (Gen<=n_generaciones);

                        /* we store the RB in the Tabu list */
                        if (Aceptar (alg_gen.solucion())==1) {
                                fun_adap.guardar_solucion(alg_gen.solucion());

                                /* we increment the number of solutions */
                                cont_soluciones++;

                                fun_adap.Decodifica (alg_gen.solucion());
                                fun_adap.Cubrimientos_Base();

                        /* we calcule the MSEs */
                                fun_adap.Error_tra ();
                                ec = fun_adap.EC;
                                el = fun_adap.EL;

                                tabla_tst = new MiDataset(fich_datos_tst, false);
                                fun_adap.Error_tst (tabla_tst);
                                ectst = fun_adap.EC;
                                eltst = fun_adap.EL;


                        /* we calculate the minimum and maximum matching */
                                min_CR = 1.0;
                                for (i=0; i < tabla.long_tabla; i++)
                                        min_CR = Adap.Minimo (min_CR, tabla.datos[i].maximo_cubrimiento);


                        /* we write the RB */
                                cadenaReglas = base_reglas.BRtoString();
                                cadenaReglas += "\n\nMinimum of C_R: " + min_CR + " Minimum covering degree: " + fun_adap.mincb + "\nAverage covering degree: " + fun_adap.medcb + " MLE: " + el + "\nMSEtra: " + ec + " , MSEtst: " + ectst + "\n";

                                Fichero.escribeFichero(fichero_reglas, cadenaReglas);

                        /* we write the obligatory output files*/
                                String salida_tra = tabla.getCabecera();
                                salida_tra += fun_adap.getSalidaObli(tabla);
                                Fichero.escribeFichero(fich_tra_obli, salida_tra);

                                String salida_tst = tabla_tst.getCabecera();
                                salida_tst += fun_adap.getSalidaObli(tabla_tst);
                                Fichero.escribeFichero(fich_tst_obli, salida_tst);

                        /* we write the MSEs in specific files */
                                Fichero.AnadirtoFichero(ruta_salida + "selcomunR.txt", "" + base_reglas.n_reglas + "\n");
                                Fichero.AnadirtoFichero(ruta_salida + "selcomunTRA.txt", "" + ec + "\n");
                                Fichero.AnadirtoFichero(ruta_salida + "selcomunTST.txt", "" + ectst + "\n");
                        }

                        /* the multimodal GA finish when the condition is true */
                } while (Parada ()==0);
       }
        }


        /** Criterion of stop */
        public int Parada () {
                if (cont_soluciones == n_soluciones)  return (1);
                else  return (0);
        }


        /** Criterion to accept the solutions */
        int Aceptar (char [] cromosoma) {
                return (1);
        }
}

