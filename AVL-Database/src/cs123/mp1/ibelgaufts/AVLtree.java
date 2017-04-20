package cs123.mp1.ibelgaufts;

import java.util.ArrayList;
import java.util.List;

public class AVLtree<T extends Comparable<T>> {
	private Node root;
	
	private class Node {
		private T data;
		private int balance;
		private Node left, right, parent;
		
		Node(T d, Node p) {
			data = d;
			parent = p;
		}
	}
	
	public boolean insert(T input) {
		if(root == null) {
			root = new Node(input, null);
		} else {
			Node n = root;
			Node parent;
			while(true) {
				if(n.data.equals(input)) {
					return false;
				}
				
				parent = n;
				boolean goleft = input.compareTo(n.data) < 0;
				n = goleft ? n.left : n.right;
				
				if(n == null) {
					if(goleft) {
						parent.left = new Node(input, parent);
					} else {
						parent.right = new Node(input, parent);
					}
					
					rebalance(parent);
					break;
				}
			}
		}
		
		return true;
	}

	public void delete(T input) {
		if(root == null) {
			return;
		}
		Node n = root;
		Node parent = root;
		Node delNode = null;
		Node child = root;
		
		while(child != null) {
			parent = n;
			n = child;
			child = input.compareTo(n.data) > 0 ? n.right : n.left;
			if(input.equals(n.data)) {
				delNode = n;
			}
		}
		
		if(delNode != null) {
			delNode.data = n.data;
			
			child = n.left != null ? n.left : n.right;
			
			if(root.data.equals(input)) {
				root = child;
			} else {
				if(parent.left == n) {
					parent.left = child;
				} else {
					parent.right = child;
				}
				
				rebalance(parent);
			}
		}
	}

	public T search(T input) {
		Node n = root;
		
		while(n != null) {
			if(input.compareTo(n.data) == 0) {
				return n.data;
			} else if(input.compareTo(n.data) < 0) {
				n = n.left;
			} else {
				n = n.right;
			}
		}
		
		return null;
	}

	public List<T> searchLT(T input, boolean includeEqual) {
		return searchLT_rec(root, input, includeEqual);
	}
	
	private List<T> searchLT_rec(Node n, T input, boolean includeEqual) {
		List<T> result = new ArrayList<T>();
		
		if(n != null && n.data.compareTo(input) < 0) {
			result.addAll(searchLT_rec(n.left, input, true));
			if(includeEqual) {
				result.add(n.data);
			}
			result.addAll(searchLT_rec(n.right, input, true));
		}
		
		return result;
	}

	public List<T> searchGT(T input, boolean includeEqual) {
		return searchGT_rec(root, input, includeEqual);
	}
	
	private List<T> searchGT_rec(Node n, T input, boolean includeEqual) {
		List<T> result = new ArrayList<T>();
		
		if(n != null && n.data.compareTo(input) > 0) {
			result.addAll(searchGT_rec(n.left, input, true));
			if(includeEqual) {
				result.add(n.data);
			}
			result.addAll(searchGT_rec(n.right, input, true));
		}
		
		return result;
	}

	private void rebalance(Node n) {
		setBalance(n);
		
		// Rebalance child
		if(n.balance == 2) {
			if(getHeight(n.left.left) >= getHeight(n.left.right)) {
				n = rotateRight(n);
			} else {
				n.left = rotateLeft(n.left);
				n = rotateRight(n);
			}
		} else if(n.balance == -2) {
			if(getHeight(n.right.right) >= getHeight(n.right.left)) {
				n = rotateLeft(n);
			} else {
				n.right = rotateRight(n.right);
				n = rotateLeft(n);
			}
		}
		
		// Rebalancing child might unbalance parent
		if(n.parent != null) {
			rebalance(n.parent);
		} else {
			root = n;
		}
	}

	private Node rotateLeft(Node a) {
		Node b = a.right;
		b.parent = a.parent;
		a.right = b.left;

		if(a.right != null) {
			a.right.parent = a;
		}
		
		b.left = a;
		a.parent = b;
		
		if(b.parent != null) {
			if(b.parent.left == a) {
				b.parent.left = b;
			} else {
				b.parent.right = b;
			}
		}
		
		setBalance(a);
		setBalance(b);
		
		return b;
	}

	private Node rotateRight(Node a) {
		Node b = a.left;
		b.parent = a.parent;
		a.left = b.right;

		if(a.left != null) {
			a.left.parent = a;
		}
		
		b.right = a;
		a.parent = b;
		
		if(b.parent != null) {
			if(b.parent.left == a) {
				b.parent.left = b;
			} else {
				b.parent.right = b;
			}
		}
		
		setBalance(a);
		setBalance(b);
		
		return b;
	}

	private void setBalance(Node n) {
		n.balance = getHeight(n.left) - getHeight(n.right);
	}
	
	private int getHeight(Node n) {
		if(n == null) {
			return 0;
		}
		return 1 + Math.max(getHeight(n.left), getHeight(n.right));
	}
	
	public void printValues() {
		printValues(root);
	}

	private void printValues(Node n) {
		if (n != null) {
			printValues(n.left);
			System.out.printf("%s\n", n.data);
			printValues(n.right);
		}
	}

}