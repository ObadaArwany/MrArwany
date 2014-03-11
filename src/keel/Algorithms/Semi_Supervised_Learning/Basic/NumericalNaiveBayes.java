/**
 * 
 * ISAAK: Busca el tag <aqui> para saber donde estan las probabilidades ;)
 * 
 */

package keel.Algorithms.Semi_Supervised_Learning.Basic;

import keel.Algorithms.Lazy_Learning.LazyAlgorithm;
import keel.Dataset.Attributes;
import keel.Dataset.InstanceSet;

import java.util.Arrays;


public class NumericalNaiveBayes extends LazyAlgorithm{

	
	//Additional structures
	private boolean isNominal[];
	private double stdDev [][];
	private double mean [][];
	private double classDistribution[];
	private double classProb[];
	private double attrProb[][][]; //ACV
	private double likehood[];
	private double likehood2[];
	
	/** 
	 * The main method of the class
	 * 
	 * @param script Name of the configuration script  
	 * 
	 */
	public NumericalNaiveBayes (double [][] train, int [] clasesTrain, double [][] test, int [] clasesTest, int clases) {
	  
		readDataFiles(train, clasesTrain, test, clasesTest, clases);
		
		/*
		for(int i=0; i< test.length; i++){
			for(int j=0; j<test[i].length; j++){
				System.out.print(test[i][j]+", ");
			}
			
			System.out.println(" ");
		}
		*/
		outFile = new String[3];
		this.outFile[0] = new String("salida1");
		this.outFile[1] =new String("salida2");
		
		nClasses = clases;
		//Naming the algorithm
		name="NumericalNaiveBayes";

		//Initialization of auxiliary structures
		stdDev=new double [inputAtt][nClasses];
		mean=new double [inputAtt][nClasses];
		classDistribution=new double [nClasses];
		classProb=new double [nClasses];
		likehood=new double [nClasses];
		isNominal=new boolean [inputAtt];
		
		for(int i=0;i<inputAtt;i++){
			Arrays.fill(stdDev[i], 0.0);
			Arrays.fill(mean[i], 0.0);
			
			if(Attributes.getAttribute(i).getNumNominalValues()!=-1){
				isNominal[i]=true;
			}
			else{
				isNominal[i]=false;
			}
		}
		Arrays.fill(classDistribution, 0.0);
		
		
		
		generateNormalModel();
		computeNominalAttributesProbabilities();
		//Initialization stuff ends here. So, we can start time-counting
		
		setInitialTime();
		
		
		
	} //end-method 
	
	
	
	public NumericalNaiveBayes (String script, InstanceSet train, InstanceSet test, InstanceSet refer) {
		  
		readDataFiles(script, train, test, refer);
		//System.out.println("EOO");
		//Naming the algorithm
		name="NumericalNaiveBayes";

		//Initialization of auxiliary structures
		stdDev=new double [inputAtt][nClasses];
		mean=new double [inputAtt][nClasses];
		classDistribution=new double [nClasses];
		classProb=new double [nClasses];
		likehood=new double [nClasses];
		isNominal=new boolean [inputAtt];
		
		for(int i=0;i<inputAtt;i++){
			Arrays.fill(stdDev[i], 0.0);
			Arrays.fill(mean[i], 0.0);
			if(train.getAttributeDefinitions().getInputAttribute(i).getNumNominalValues()!=-1){
				isNominal[i]=true;
			}
			else{
				isNominal[i]=false;
			}
		}
		Arrays.fill(classDistribution, 0.0);
		
		
		generateNormalModel();
		computeNominalAttributesProbabilities();
		//Initialization stuff ends here. So, we can start time-counting
		
		setInitialTime();
		
	} //end-method 
	
	
	
	/** 
	 * The main method of the class
	 * 
	 * @param script Name of the configuration script  
	 * 
	 */
	public NumericalNaiveBayes (String script) {
	  
		readDataFiles(script);
		
		//Naming the algorithm
		name="NumericalNaiveBayes";

		//Initialization of auxiliary structures
		stdDev=new double [inputAtt][nClasses];
		mean=new double [inputAtt][nClasses];
		classDistribution=new double [nClasses];
		classProb=new double [nClasses];
		likehood=new double [nClasses];
		isNominal=new boolean [inputAtt];
		
		for(int i=0;i<inputAtt;i++){
			Arrays.fill(stdDev[i], 0.0);
			Arrays.fill(mean[i], 0.0);
			if(train.getAttributeDefinitions().getInputAttribute(i).getNumNominalValues()!=-1){
				isNominal[i]=true;
			}
			else{
				isNominal[i]=false;
			}
		}
		Arrays.fill(classDistribution, 0.0);
		
		
		generateNormalModel();
		computeNominalAttributesProbabilities();
		//Initialization stuff ends here. So, we can start time-counting
		
		setInitialTime();
		
	} //end-method 
	
	/** 
	 * Reads configuration script, to extract the parameter's values.
	 * 
	 * @param script Name of the configuration script  
	 * 
	 */	
	protected void readParameters (String script) {

	}//end-method

	/** 
	 * Evaluates a instance to predict its class.
	 * 
	 * @param example Instance evaluated 
	 * @return Class predicted
	 * 
	 */
	protected int evaluate (double example[]) {
	
		int result=-1;
		
		Arrays.fill(likehood, 1.0);
		
		for(int j=0;j<inputAtt;j++){
			for(int c=0;c<nClasses;c++){
				if(classDistribution[c]>0){
					likehood[c]*=computeLikehood(j,c,example[j]);
				}
			}
		}
		

		for(int c=0;c<nClasses;c++){
			likehood[c]*=classProb[c];
		}
		

		/*System.out.println("*******1******");
		
		for(int i=0;i<likehood.length;i++){
			System.out.println(likehood[i]);

		}
		System.out.println("******2*******");
		
		*/
		/*
		
		<aqui>

		 esta ya listo el array con las probabilidades (ordenadas por clases). Por ejemplo, si esto fuera iris, un posible array seria:
		 
		 likehood[0]= 0.2332
		 likehood[1]= 0.0056
		 likehood[2]= 0.0031
		 
		 Mucho ojo, no estan normalizadas. El NB no lo necesita, pero puede que para otros usos s� que haga falta...
		 
		*/
		double max=-1;
		//System.out.println("max ="+ max);
		
		for(int i=0;i<likehood.length;i++){
			//System.out.println(likehood[i]);
			if(max<likehood[i]){
				max=likehood[i];
				result=i;
			}
		}

		
		return result;
	
	} //end-method	
	
	
	
	/** 
	 * Evaluates a instance to predict its class.
	 * 
	 * @param example Instance evaluated 
	 * @return Class predicted
	 * 
	 */
	protected double[] evaluate2 (double example[]) {
	
			
		Arrays.fill(likehood, 1.0);
		
		for(int j=0;j<inputAtt;j++){
			for(int c=0;c<nClasses;c++){
				if(classDistribution[c]>0){
					likehood[c]*=computeLikehood(j,c,example[j]);
				}
			}
		}
		
		for(int c=0;c<nClasses;c++){
			likehood[c]*=classProb[c];
		}
		
		
		// Normalize 
		
		double sumatoria=0;

		for(int i=0;i<likehood.length;i++){
			sumatoria+= likehood[i];
		}

		for(int i=0;i<likehood.length;i++){
			likehood[i]/=sumatoria;

		}
		
	/*	for(int i=0;i<likehood.length;i++){
			System.out.println(likehood[i]);

		}
		System.out.println("*************");
		*/
		
		double result[] = new double[nClasses];

		result = likehood.clone();
		return result;
	
	} //end-method	
	
	
	public double[][] getProbabilities(){
		
	 //System.out.println("OBTENIENDO PROBABILIDADES, "+ testData.length);
		double [][] probabilities = new double[this.testData.length][this.nClasses];
		
		
		for(int q = 0 ; q < this.testData.length ;q++){
			
			 probabilities[q] = this.evaluate2(this.testData[q]);
		}
		
		
		return probabilities;
	}
	
	public int[] getPredictions(){
		
			int [] probabilities = new int[this.testData.length];
			
			
			for(int q = 0 ; q < this.testData.length ;q++){
				
				 probabilities[q] = this.evaluate(this.testData[q]);
			}
			
			
			return probabilities;
		}
		
	
	
	
	private double computeLikehood(int att,int out,double value){
		
		double likehood=0.0;
		double m,sigma,exponent;
		
		if(!isNominal[att]){
			m=mean[att][out];
			sigma=stdDev[att][out];
			
			if(sigma==0){
				if(value==m){
					return 1.0;				
				}else{
					return 0.0;
				}
			}else{
				exponent=-1.0*(value-m)*(value-m)/(2.0*sigma*sigma);
				
				likehood= 1.0/Math.sqrt(2.0*Math.PI*sigma*sigma);
				
				likehood*=Math.exp(exponent);
			}
		}
		else{
			likehood=attrProb[att][out][real2Nom(value,att)];
		}
		
		return likehood;
	}
	
	private void generateNormalModel(){
	
		for(int i=0;i<trainData.length;i++){
			for(int j=0;j<inputAtt;j++){
				if(!isNominal[j]){
					
					mean[j][trainOutput[i]]+=trainData[i][j];
				
					stdDev[j][trainOutput[i]]+=(trainData[i][j]*trainData[i][j]);
				}
			}
			
   		classDistribution[trainOutput[i]]+=1.0;
		}
		
		for(int j=0;j<inputAtt;j++){
			if(!isNominal[j]){
				for(int c=0;c<nClasses;c++){
					if(classDistribution[c]>0){
						mean[j][c]/=classDistribution[c];
						stdDev[j][c]/=classDistribution[c];
						stdDev[j][c]-=(mean[j][c]*mean[j][c]);
						stdDev[j][c]=Math.sqrt(stdDev[j][c]);
					}
				}
			}
		}
		
		for(int c=0;c<nClasses;c++){
			classProb[c]=classDistribution[c]/trainData.length;
		}

	}
	
	private int real2Nom(double real,int att){
		
		int result;
		
		result=(int)(real*((Attributes.getInputAttribute(att).getNominalValuesList().size())-1));

		return result;  
		
	}//end-method	

	private void computeNominalAttributesProbabilities() {
        
        attrProb = new double[inputAtt][nClasses][1];
        
        for (int j = 0; j < inputAtt; j++) {
        	if(isNominal[j]){
	        	for (int c = 0; c < nClasses; c++) {
	                attrProb[j][c] = new double[Attributes.getInputAttribute(j).getNumNominalValues()];
	        	}
        	}
        }
        
        for (int i = 0; i < trainData.length; i++) {;
            for (int j = 0; j < inputAtt; j++) {
            	if(isNominal[j]){
            	//	System.out.println(real2Nom(trainData[i][j],j));
            		attrProb[j][trainOutput[i]][real2Nom(trainData[i][j],j)]+=1.0; 
            	}
            }
        }
        
        
        int contador[][] = new int[inputAtt][nClasses];

        for (int i = 0; i < attrProb.length; i++) {
        	if(isNominal[i]){
	            for (int j = 0; j < attrProb[i].length; j++) {
	                for (int k = 0; k < attrProb[i][j].length; k++) {
	                    attrProb[i][j][k]++; //Laplace correction
	                    contador[i][j] += attrProb[i][j][k];
	                }
	            }
        	}
        }
        for (int i = 0; i < attrProb.length; i++) {
        	if(isNominal[i]){
	            for (int j = 0; j < attrProb[i].length; j++) {
	                for (int k = 0; k < attrProb[i][j].length; k++) {
	                    attrProb[i][j][k] /= contador[i][j];
	                }
	            }
        	}
        }
    }
	
} //end-class 