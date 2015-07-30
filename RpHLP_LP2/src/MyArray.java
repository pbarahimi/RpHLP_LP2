

import java.util.Scanner;
import java.io.*;

public class MyArray {

	public static double[][] read(String file) {
		int col;
		int row = 0;
		int el = 0; // Number of elements

		try {
			Scanner in = new Scanner(new File(file));
			while (in.hasNextLine()) {
				row++;
				in.nextLine();
			}

			in = new Scanner(new File(file));
			while (in.hasNextDouble()) {
				el++;
				in.nextDouble();
			}
			// System.out.println("the number of elements: "+el);

			// to get the # of columns we devide number of elements by # of rows
			try {
				col = el / row;
				double[][] y = new double[row][col];
				in = new Scanner(new File(file));

				for (int i = 0; i < row; i++) {
					for (int j = 0; j < col; j++) {
						y[i][j] = in.nextFloat();
					}
				}
				in.close();
				return y;
			} catch (ArithmeticException e) {
				System.out.println("Devided by zero!");
				return null;
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			return null;
		}

	}

	public static double[] read2(String file) {
		int row = 0;

		try {
			Scanner in = new Scanner(new File(file));
			while (in.hasNextLine()) {
				row++;
				in.nextLine();
			}

			try {
				double[] y = new double[row];
				in = new Scanner(new File(file));

				for (int i = 0; i < row; i++) {
					y[i] = in.nextFloat();
				}
				in.close();
				return y;
			} catch (ArithmeticException e) {
				System.out.println("Devided by zero!");
				return null;
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			return null;
		}

	}

	public static void print(double[][] A) {
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[0].length; j++) {
				System.out.printf("%-8.2f ", A[i][j]);
			}
			System.out.println();
		}
	}
}