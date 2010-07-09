package keel.Algorithms.RE_SL_Postprocess.TSKLocalTunRules;
import java.io.*; 
import org.core.*;
import java.util.*;
import java.lang.Math;

class Tun_TSK {
	public double semilla;
	public long cont_soluciones;
	public long Gen, n_genes, n_reglas, n_generaciones;
	public int n_soluciones;

	public String fich_datos_chequeo, fich_datos_tst;
	public String fichero_conf, ruta_salida;
	public String fichero_br, fichero_reglas, fich_tra_obli, fich_tst_obli;
	public String datos_inter = "";
	public String cadenaReglas = "";

	public MiDataset tabla, tabla_tst;
    public BaseR base_reglas;
    public Adap fun_adap;
    public AG alg_gen;


	public Tun_TSK (String f_e) {
		fichero_conf = f_e;
	}

	private String Quita_blancos(String cadena) {
		StringTokenizer sT = new StringTokenizer(cadena, "\t ", false);
		return (sT.nextToken());
	}


	/** Reads the data of the configuration file */
	public void leer_conf (){
		int i, j;
		String cadenaEntrada, valor;
		double cruce, mutacion, a, b, porc_ind_ee;
		int long_poblacion, gen_ee;


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
		fichero_br = ((ficheros.nextToken()).replace('\"',' ')).trim();

		// we read the name of the output files
		sT.nextToken();
		valor = sT.nextToken();

		ficheros = new StringTokenizer(valor, "\t ", false);
		fich_tra_obli = ((ficheros.nextToken()).replace('\"',' ')).trim();
		fich_tst_obli = ((ficheros.nextToken()).replace('\"',' ')).trim();
		fichero_reglas = ((ficheros.nextToken()).replace('\"',' ')).trim();
		ruta_salida = fich_tst_obli.substring(0, fich_tst_obli.lastIndexOf('/') + 1);

		// we read the seed of the random generator
		sT.nextToken();
		valor = sT.nextToken();
		semilla = Double.parseDouble(valor.trim());
		Randomize.setSeed((long) semilla);;

		// we read the Number of Iterations
		sT.nextToken();
		valor = sT.nextToken();
		n_generaciones = Long.parseLong(valor.trim());

		// we read the Population Size
		sT.nextToken();
		valor = sT.nextToken();
		long_poblacion = Integer.parseInt(valor.trim());

		// we read the Parameter a
		sT.nextToken();
		valor = sT.nextToken();
		a = Double.parseDouble(valor.trim());

		// we read the Parameter b
		sT.nextToken();
		valor = sT.nextToken();
		b = Double.parseDouble(valor.trim());

		// we read the Cross Probability
		sT.nextToken();
		valor = sT.nextToken();
		cruce = Double.parseDouble(valor.trim());

		// we read the Mutation Probability
		sT.nextToken();
		valor = sT.nextToken();
		mutacion = Double.parseDouble(valor.trim());

		// we read the Rate of the Population to which the ES is applied
		sT.nextToken();
		valor = sT.nextToken();
		porc_ind_ee = Double.parseDouble(valor.trim());

		// we read the Evolutionary Strategy Iterations
		sT.nextToken();
		valor = sT.nextToken();
		gen_ee = Integer.parseInt(valor.trim());

		// we create all the objects
		tabla = new MiDataset(fich_datos_chequeo, true);
		if (tabla.salir==false) {
		base_reglas = new BaseR(fichero_br, tabla);
		fun_adap = new Adap(tabla, base_reglas);
		alg_gen = new AG(long_poblacion, cruce, mutacion, a, b, porc_ind_ee, gen_ee, fun_adap, base_reglas);
	    }
	}

	public void run () {
		int i, j;
		double ec, el, ec_tst, el_tst;


		/* We read the configutate file and we initialize the structures and variables */
		leer_conf();

		if (tabla.salir==false) {
		/* Generation of the initial population */
		Gen = 0;
		alg_gen.Initialize();

		/* Evaluation  of the initial population */
		alg_gen.Evaluate ();
		Gen++;

		/* Main of the genetic algorithm */
		do {
			/* Interchange of the new and old population */
			alg_gen.Intercambio();

			/* Selection by means of Baker */
			alg_gen.Select ();

			/* Crossover */
			alg_gen.Max_Min_Crossover();

			/* Mutation */
			alg_gen.Mutacion_No_Uniforme (Gen, n_generaciones);

			/* Estrategia de evolucion */
			alg_gen.Estrategia_Evolucion ();

			/* Elitist selection */
			alg_gen.Elitist ();

			/* Evaluation of the current population */
			alg_gen.Evaluate ();

			/* we increment the counter */
			Gen++;
		} while (Gen <= n_generaciones);

		/* we calcule the MSEs */
		fun_adap.Decodifica (alg_gen.solucion());
		fun_adap.Error_tra ();
		ec = fun_adap.EC;
		el = fun_adap.EL;

		tabla_tst = new MiDataset(fich_datos_tst, false);
		fun_adap.Error_tst (tabla_tst);
		ec_tst  = fun_adap.EC;
		el_tst = fun_adap.EL;


		/* we write the RB */
		cadenaReglas = base_reglas.BRtoString();
		cadenaReglas += "\nMSEtra: " + ec + "  MLEtra: " + el;
		cadenaReglas += "\nMSEtst: " + ec_tst + "  MLEtst: " + el_tst;

		Fichero.escribeFichero(fichero_reglas, cadenaReglas);

		/* we write the obligatory output files*/
		String salida_tra = tabla.getCabecera();
		salida_tra += fun_adap.getSalidaObli(tabla);
		Fichero.escribeFichero(fich_tra_obli, salida_tra);

		String salida_tst = tabla_tst.getCabecera();
		salida_tst += fun_adap.getSalidaObli(tabla_tst);
		Fichero.escribeFichero(fich_tst_obli, salida_tst);

		/* we write the MSEs in specific files */
		Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunR.txt", "" + base_reglas.n_reglas + "\n");
		Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunTRA.txt", "" + ec + "\n");
		Fichero.AnadirtoFichero(ruta_salida + "tun_tskcomunTST.txt", "" + ec_tst + "\n");
       }
	}

}
