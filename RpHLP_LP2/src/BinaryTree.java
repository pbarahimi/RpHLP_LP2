import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


public class BinaryTree {

	public static int leftChild(int index, int depth) {
		if (Math.floor(Math.log(index+1)/Math.log(2))>depth-1){
			throw new NoSuchElementException("Node " + index + " with depth " + depth + " has no children");
		}
		return 2*index + 1;
	}
	
	public static int rightChild(int index, int depth) {
		if (Math.floor(Math.log(index+1)/Math.log(2))>depth-1){
			throw new NoSuchElementException("Node " + index + " with depth " + depth + " has no children");
		}
		return 2*index + 2;
	}
	
	public static List<Integer> leftChildren(int index, int depth) {
		if (Math.floor(Math.log(index+1)/Math.log(2))>depth-1){
			throw new NoSuchElementException("Node " + index + " with depth " + depth + " has no children");
		}
		int leftchild = (2*index)+1;
		
		List<Integer> result = new ArrayList<Integer>();
		List<Integer> unvisitedList = new ArrayList<Integer>();
		result.add(leftchild);
		unvisitedList.add(leftchild);
		
		int currentNode = unvisitedList.get(0);
		unvisitedList.remove(0);
		
		while (currentNode<Math.pow(2, depth) ){
			
			int newLeftChild = (2*currentNode)+1;
			int newRightChild = (2*currentNode)+2;
			
			result.add(newLeftChild);
			result.add(newRightChild);
			unvisitedList.add(newLeftChild);
			unvisitedList.add(newRightChild);
			currentNode = unvisitedList.get(0);
			unvisitedList.remove(0);
		}
		return result;				
	}
	
	public static List<Integer> rightChildren(int index, int depth) {
		if (Math.floor(Math.log(index+1)/Math.log(2))>depth-1){
			throw new NoSuchElementException("Node " + index + " with depth " + depth + " has no children");
		}
		int rightchild = (2*index)+2;
		
		List<Integer> result = new ArrayList<Integer>();
		List<Integer> unvisitedList = new ArrayList<Integer>();
		result.add(rightchild);
		unvisitedList.add(rightchild);
		
		int currentNode = unvisitedList.get(0);
		unvisitedList.remove(0);
		
		while (currentNode<Math.pow(2, depth) ){
			
			int newLeftChild = (2*currentNode)+1;
			int newRightChild = (2*currentNode)+2;
			
			result.add(newLeftChild);
			result.add(newRightChild);
			unvisitedList.add(newLeftChild);
			unvisitedList.add(newRightChild);
			currentNode = unvisitedList.get(0);
			unvisitedList.remove(0);
		}
		return result;				
	}
}
