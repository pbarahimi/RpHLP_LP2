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
	private static int P = 3; // number of hubs to be located
	private static double q = 0.95; // probability of a node being functional
	private static int M = nVar * P; // the big M

	/**
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param m
	 * @return operating probability of a route
	 */
	public static double q(int i, int j, int k, int m) {
		double result = q;
		if (i != j) {
			if (j != k && j != m && i != k && i != m && k != m) // Xijkm
				result = Math.pow(q, 2);
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
		}
		return result;
	}
	
	public static int beta(int i, int j, int k, int m, int r){
		int output=1;
		if ((i==k && j==m) || (i==k && i==m) || (j==k && j==m))
			output=P-r;
		return output;
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

		// Objective function
		out.println("Minimize");
		for (int r = 0; r <= P; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
//							double Pr = q(i, j, k, m) * Math.pow((1 - q), r);
							double Wij = flows[i][j];
							double Cijkm = distances[i][k] + (1 - alpha)
									* distances[k][m] + distances[m][j];
							double CoEf = Wij * Cijkm;
							if (CoEf != 0) {
								out.println("+ " + CoEf + " x" + i + "_" + j
										+ "_" + k + "_" + m + "_" + r);
							}
						}
					}
				}
			}
		}
		
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

		// Constraint 1
		for (int k = 0; k < nVar; k++) {
			out.print(" + y" + k);
		}
		out.println(" = " + P);

		// Constraint 2
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {

							out.print(" + x" + i + "_" + j + "_" + k + "_" + m
									+ "_0");
						}
					}
					out.println(" = 1");
				}
			}
		out.println();

		// Constraint 3
		for (int r = 0; r <= P-1; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						
						for (int m = 0; m < nVar; m++) {
							out.print(" + x" + i + "_" + j + "_" + k + "_" + m
									+ "_" + r);
						}
						out.println(" - y" + k + " <= 0");
					}
				}
			}
		}
		out.println();

		// Constraint 4
		for (int r = 0; r <= P-1; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int m = 0; m < nVar; m++) {
						
						for (int k = 0; k < nVar; k++) {
							out.print(" + x" + i + "_" + j + "_" + k + "_" + m
									+ "_" + r);
						}
						out.println(" - y" + m + " <= 0");
					}
				}
			}
		}
		out.println();

		// Constraint 5
		
		for (int i=0;i<nVar;i++){
			for (int j=0;j<nVar;j++){
				for (int k=0;k<nVar;k++){
					for (int m=0;m<nVar;m++){
						if (m!=k){
							for (int r=0;r<=P-1;r++){
								out.print(" + x" + i + "_" + j + "_" + k + "_"
											+ m + "_" + r);
								out.print(" + x" + i + "_" + j + "_" + m + "_"
										+ k + "_" + r);
							}
							out.println(" <= 1");
						}
					}
				}
			}
		}

		// Constraint 6
		for (int i = 0; i < nVar; i++) {
			for (int j = 0; j < nVar; j++) {
			
				for (int k=0;k<nVar;k++){
					for (int m=0;m<nVar;m++){	
						for (int r=0;r<=P-1;r++){
							int beta=beta(i,j,k,m,r);
							out.print(" + "+beta+" x" + i + "_" + j + "_" + k + "_"
									+ m + "_" + r);
						}
					}
				}
				out.println(" = "+ P);
			}
		}
		
		// Constraint 7
		for (int i = 0; i < nVar; i++) {
			for (int j = 0; j < nVar; j++) {
				for (int r=0;r<=P-2;r++){
					
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
							out.print(" + x" + i + "_" + j + "_" + k + "_"
									+ m + "_" + (r+1));
							out.print(" - x" + i + "_" + j + "_" + k + "_"
									+ m + "_" + r);
						}
					}
					out.println(" <= 0");
				}
			}
		}
		
		
		// Constraint 8
		for (int i = 0; i < nVar; i++) {
			for (int j = 0; j < nVar; j++) {
				for (int k=0;k<nVar;k++){
					for (int r=0;r<=P-2;r++){
						
						for (int m = 0; m < nVar; m++) {
							for (int t = r+1; t <= P; t++) {
								out.print(" + x" + i + "_" + j + "_" + k + "_"
										+ m + "_" + t);
								out.print(" + x" + i + "_" + j + "_" + m + "_"
										+ k + "_" + t);
							}
						}
						out.print(" + "+ M + " x" + i + "_" + j + "_" + k + "_"
								+ k + "_" + r);
						out.println(" <= " + M);
					}
				}
			}
		}
	
		// Constraint 9
		for (int r = 0; r <= P-3; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
								for (int _k=0;_k<nVar;_k++){
									for (int t=r+2;t<=P-1;t++){
										out.print(" + x" + i + "_" + j + "_" + _k + "_"
												+ m + "_" + t);
										out.print(" + x" + i + "_" + j + "_" + m + "_"
												+ _k + "_" + t);
									}
								}
								
								out.print(" + "+ M + " x" + i + "_" + j + "_" + k + "_"
										+ m + "_" + r);
								out.print(" + "+ M + " x" + i + "_" + j + "_" + m + "_"
										+ k + "_" + r);
								for (int n=0;n<nVar;n++){
									if (n!=k){
										out.print(" + "+ M + " x" + i + "_" + j + "_" + k + "_"
												+ n + "_" + (r+1));
										out.print(" + "+ M + " x" + i + "_" + j + "_" + n + "_"
												+ k + "_" + (r+1));
									}
								}
							out.println(" <= " + (2*M));
						}
					}
				}
			}
		}

		out.println("Binaries");

		// Binaries
		for (int k = 0; k < nVar; k++) {
			out.println("y" + k);
		}
		
		for (int r = 0; r < P+1; r++) {
			for (int i = 0; i < nVar; i++) {
				for (int j = 0; j < nVar; j++) {
					for (int k = 0; k < nVar; k++) {
						for (int m = 0; m < nVar; m++) {
								out.println("x" + i + "_" + j
										+ "_" + k + "_" + m + "_" + r);
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

	}
}