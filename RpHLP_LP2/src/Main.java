import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;



public class Main {
	private static double alpha = 0.2;
	private static double[][] tmpFlows = MyArray.read("w.txt");
	private static int nVar = tmpFlows.length;
	private static double[][] flows = new double[nVar][nVar];
	// private static double[][] fixedCharge = MyArray.read("fixedcharge.txt");
	private static double[][] coordinates = MyArray.read("coordinates.txt");
	private static double[][] distances = Distance.get(coordinates);	
	private static int P = 4; // number of hubs to be located
	private static double q = 0.05; // probability of a node being functional
	private static int D = 1; 	// max number of simultaneous disruptions
	private static int R = (int) (Math.pow(2, D+1)-2);  // Index of the last node in the full binary tree
	private static int M = nVar * R; // the big M

	/**
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param m
	 * @return operating probability of a route
	 */
	public static double Q(int i, int k, int m, int j) {
		double result = q;
		if (k!=i && j!=m)
			result = q+q(k,m);
		else if (m!=j)
			result = q(i,m);
		else if (k!=i)
			result = q(j,k);
		else if (i==k && j==m)
			result = 0;
		else 
			System.out.println("Not include in the Q(i,k,m,j)!");
		/*if (i != j) {
			if (j != k && j != m && i != k && i != m && k != m) // Xijkm
				result = 2*q;
			else if (j == k && j == m) // Xijjj
				result = 1;
			else if (i == k && i == m) // Xijii
				result = 1;
			else if (i == k && j == m) // Xijij
				result = 1;
			else if (i != k && j != m && k == m) // Xijkk
				result = q;
			else if (i == k && m != k && j != m) // Xijik
				result = q;
			else if (i != k && m != k && j == m) // Xijkj
				result = q;
			else if (j == k && k != m && i != m) // Xijjk Shouldn't be selected
													// in the solution
				result = q;
			else if (i == m && m != k && j != k) // Xijki Shouldn't be selected
													// in the solution
				result = q;
			else if (j == k && i == m && k != m) // Xijji Shouldn't be selected
													// in the solution
				result = 1;
		}*/
		return result;
	}
	
	/** Cikmj */
	private static double Cijkm(int i, int k, int m, int j) {
		double cost = distances[i][k] + (1 - alpha) * distances[k][m]
				+ distances[m][j];
		return cost;
	}
	
	public static int beta(int i, int j, int k, int m, int r){
		int output=1;
		if ((i==k && j==m) || (i==k && i==m) || (j==k && j==m))
			output=P-r;
		return output;
	}

	private static double q(int k, int m){
		if (k==m)
			return 0;
		else
			return q;		
	}

	public static void main(String[] args) throws FileNotFoundException {
		/*
		 * Filling in the flows matrix assymetrically
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = 0; j < nVar; j++) {
				flows[i][j] = tmpFlows[i][j] + tmpFlows[j][i];
			}
		}
		
//		File file = new File("C:/Users/PB/git/RpHLP_LP/RpHLP_LP/ModelAndResults/model.lp");
		File file = new File("D:/model.lp");
		PrintWriter out = new PrintWriter(file);

		/**
		 *  Objective function
		 */
		out.println("Minimize");
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				for (int k = 0; k < nVar; k++) {
					for (int m = 0; m < nVar; m++) {
						double coef = flows[i][j] * Cijkm(i, k, m, j) * (1 - Q(i,k,m,j));
						out.append(" + " + coef + " x" + i + "_" + k
										+ "_" + m + "_" + j + "_0\n");
					}
				}
			}
		}
		
		for (int r = 1; r <= R; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = i+1; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
							double CoEf = flows[i][j] * Cijkm(i, k, m, j) * Math.pow(q, Math.floor(Math.log(r+1)/Math.log(2)));
							out.println(" + " + CoEf + " x" + i + "_" + k
									+ "_" + m + "_" + j + "_" + r);
						}
					}
				}
			}
		}
		
		/* Fixed charged variables */
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[0].length; j++) {
				System.out.printf("%-8.2f ", distances[i][j]);
			}
			System.out.println();
		}
	
		/*for (int k = 0; k < nVar; k++) {
			out.print(" + " + fixedCharge[k][0] + " y" + k);
		}*/
		out.println();
		out.println("Subject to");

		/**
		 *  Constraint 2
		 */
		for (int i = 0; i < nVar; i++) {
			out.print(" + y" + i);
		}
		out.println(" = " + P);

		/**
		 *  Constraint 3
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				
				for (int k = 0; k < nVar; k++) {
					for (int m = 0; m < nVar; m++) {

						out.print(" + x" + i + "_" + k + "_" + m + "_" + j
								+ "_0");
					}
				}
				out.println(" = 1");
			}
		}
		out.println();

		/**
		 *  Constraint 4
		 */
		for (int r = 0; r <= R; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = i+1; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						
						for (int m = 0; m < nVar; m++) {
							out.print(" + x" + i + "_" + k + "_" + m + "_" + j
									+ "_" + r);
						}
						out.println(" - y" + k + " <= 0");
					}
				}
			}
		}
		out.println();

		/**
		 *  Constraint 5
		 */
		for (int r = 0; r <= R; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = i+1; j < nVar; j++) {
					for (int m = 0; m < nVar; m++) {
						
						for (int k = 0; k < nVar; k++) {
							out.print(" + x" + i + "_" + k + "_" + m + "_" + j
									+ "_" + r);
						}
						out.println(" - y" + m + " <= 0");
					}
				}
			}
		}
		out.println();


		/** 
		 * Constraint 6
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				for (int r=0;r<=R;r++){
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							if (k!=i && m!=i){
								out.append(" + x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + r);
							}
						}
					}
					out.append(" + " + M + " y" + i);
					out.append(" <= " + M + "\n");
				}
			}
		}
		
		/** 
		 * Constraint 7
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				for (int r=0;r<=R;r++){
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							if (k!=j && m!=j){
								out.append(" + x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + r);
							}
						}
					}
					out.append(" + " + M + " y" + j);
					out.append(" <= " + M + "\n");
				}
			}
		}
		
		/**
		 *  Constraint 8
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				for (int r=0;r<=R-Math.pow(2, D);r++){	// The leaf-nodes are not to be considered in this constraint.
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							if (k!=i && k!=j){
								out.append(" + x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + r);
							}
						}
					}
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							
								out.append(" - x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + (2*r+1) );		// left child node
							
						}
					}
					out.append(" <= 0\n");
				}
			}
		}
		
		/**
		 *  Constraint 9
		 */
		for (int i = 0; i < nVar; i++) {
			for (int j = i+1; j < nVar; j++) {
				for (int r=0;r<=R-Math.pow(2, D);r++){	// The leaf-nodes are not to be considered in this constraint.
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							if (m!=i && m!=j){
								out.append(" + x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + r);
							}
						}
					}
					
					for (int k=0;k<nVar;k++){
						for (int m=0;m<nVar;m++){
							
								out.append(" - x" + i + "_" + k + "_" + m + "_" + j
										+ "_" + (2*r+2) );		// right child node
							
						}
					}
					out.append(" <= 0\n");
				}
			}
		}
		
		/**
		 * Constraint 10
		 */
		for (int i=0;i<nVar;i++){
			for (int j=i+1;j<nVar;j++){
				for (int k=0;k<nVar;k++){
					for (int r=0;r<=R-Math.pow(2, D);r++){
						
						for (int m=0; m<nVar; m++){
							out.append(" + x" + i + "_" + k + "_" + m + "_"
									+ j + "_" + (2*r+1));
							out.append(" + x" + i + "_" + m + "_" + k + "_"
									+ j + "_" + (2*r+1));
						}
						for (int m=0; m<nVar; m++){
							out.append(" + " + M + " x" + i + "_" + k + "_" + m + "_"
									+ j + "_" + r);
						}
						out.println(" <= "+ M);
					}
				}
			}
		}
		
		/**
		 * Constraint 11
		 */
		for (int i=0;i<nVar;i++){
			for (int j=i+1;j<nVar;j++){
				for (int m=0;m<nVar;m++){
					for (int r=0;r<=R-Math.pow(2, D);r++){
						
						for (int k=0; k<nVar; k++){
							out.append(" + x" + i + "_" + k + "_" + m + "_"
									+ j + "_" + (2*r+2));
							out.append(" + x" + i + "_" + m + "_" + k + "_"
									+ j + "_" + (2*r+2));
						}
						for (int k=0; k<nVar; k++){
							out.append(" + " + M + " x" + i + "_" + k + "_" + m + "_"
									+ j + "_" + r);
						}
						out.println(" <= "+ M);
					}
				}
			}
		}
		
		/**
		 *  Constraint 12
		 */
		/* Modify the counter for r to count child nodes only. 
		 * Hint: refer to the formulation in #38 assignment.
		 *
		 */		 
/*		for (int i=0;i<nVar;i++){
			for (int j=i+1;j<nVar;j++){
				for (int k=0;k<nVar;k++){
					for (int m=0;m<nVar;m++){
						
						for (int r=0;r<=R;r++){
							out.print(" + x" + i + "_" + m + "_" + k + "_"
									+ j + "_" + r);
						}
						out.println(" <= 1");
				
					}
				}
			}
		}*/
		out.println("Binaries");

		/**
		 *  Binaries
		 */
		for (int k = 0; k < nVar; k++) {
			out.println("y" + k);
		}
		
		for (int r = 0; r <= R; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = i+1; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
								out.println("x" + i + "_" + k
										+ "_" + m + "_" + j + "_" + r);
						}
					}
				}
			}
		}
		out.close();

		// Test
/*		PrintWriter out1 = new PrintWriter(new File("ModelAndResults/Test.csv"));
		out1.append("Variable,Pr,Wij,Cijkm,Coefficient\n");
		for (int r = 0; r < P; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
							double Pr = q(i, j, k, m) * Math.pow((1 - q), r);
							double Wij = flows[i][j];
							double Cijkm = distances[i][k] + (1 - alpha)
									* distances[k][m] + distances[m][j];
							double CoEf = Wij * Cijkm;
							out1.append("x" + i + "_" + j + "_" + k + "_" + m
									+ "_" + r + ",");
							out1.append(Pr + "," + Wij + "," + Cijkm + ","
									+ CoEf);
							out1.append("\n");
						}
					}
				}
			}
		}
		out1.close();*/
		
		// Solve the model
		/*ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start",
				 "C:/gurobi603/win64/bin/gurobi_cl",
				 "ResultFile=C:/Users/PB/git/RpHLP_LP/RpHLP_LP/ModelAndResults/Results.sol"
				 ,"C:/Users/PB/git/RpHLP_LP/RpHLP_LP/ModelAndResults/model.lp");*/
		
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start",
				 "C:/gurobi603/win64/bin/gurobi_cl",
				 "ResultFile=D:/Results.sol"
				 ,"D:/model.lp");
		 try {
			 pb.start();
			 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Done!");

	}
}