import java.io.*;
import java.util.*;

/**
 * This class has static methods to compress and decompress the file, as well as many helper methods 
 * for compression. This 'zip' file is created using Huffman Encoding. A user can pass in through parameters 
 * a file name and desired compressed 'zip' file name, and vice versa for decompression. 
 * 
 * This program compressed the USConstitution file from 45 KB to 25 KB
 * It also compressed WarAndPeace.txt from 3.2 MB to 1.8 MB!
 * 
 * @author John Weingart, Dartmouth CS 10, Fall 2018
 */
public class HuffmanCompressor {

	/**
	 * This is the compression method. It calls on other methods to:
	 * 1) find frequencies of each character within the file
	 * 2) generate a PriorityQueue containing each character and their frequency (each stored in a tree)
	 * 3) generate one big binary tree from the priority queue, based on frequency
	 * 4) create a code map: characters and their bit codes
	 * 5) generate a compressed file by traversing the code map
	 * 
	 * @param fileName					Name of file to compress
	 * @param compressedFileName		Name of compressed file to be created
	 * @return							Code map tree (for decompression)
	 */
	public static BinaryTree<ValueChar> compressFile(String fileName, String compressedFileName)
	{
		BinaryTree<ValueChar> thisTree = null;
		HashMap<Character, Integer> frequencies = fileFrequencies(fileName);

		if(frequencies != null) //handles wrong file name case
		{
			PriorityQueue<BinaryTree<ValueChar>> originalQueue = generateOriginalQueue(frequencies);
			thisTree = generateTree(originalQueue);
			HashMap<Character, String> codeMap = generateCodeMap(thisTree);
			System.out.println(frequencies);
			System.out.println(thisTree);
			System.out.println(codeMap);
			writeCompressed(codeMap, fileName, compressedFileName);
		}
		return thisTree;
	}

	/**
	 * This is the method that decompresses a file. It opens the file and output file, then reads bits from 
	 * the compressed file. Each bit tells it to check the left ('0') or right ('1') node in the tree. When
	 * it gets to a leaf, it writes the Character at that leaf to the output file.
	 * 
	 * @param codeTree					Binary Tree that stores all characters in file
	 * @param fileName					Name of file to decompress
	 * @param decompressedFileName		Name of decompressed file to generate
	 */
	public static void decompressFile(BinaryTree<ValueChar> codeTree, String fileName, String decompressedFileName)
	{
		BufferedBitReader bitInput = null;
		BufferedWriter output = null;

		//Open the file and output file, throw exception if not
		try
		{
			bitInput = new BufferedBitReader(fileName);
		}
		catch(IOException e)
		{
			System.out.println("Could not open compressed file for decompression");
			return;
		}
		try
		{
			output = new BufferedWriter(new FileWriter(decompressedFileName));
		}
		catch(IOException e)
		{
			System.out.println("Could not open compressed file for decompression");
			return;
		}

		BinaryTree<ValueChar> searchTree = codeTree; //'searchTree' will traverse the tree; codeTree must still keep track of head

		//while loop to read all bits in the file and 
		while(bitInput.hasNext())
		{
			if(searchTree.isLeaf()) //if it is a leaf, we have reached the end of the code for a character
			{
				try
				{
					output.write(searchTree.getData().getChar()); //write that character to the decompressed file
				}
				catch(IOException e)
				{
					System.out.println("Could not write to decompressed file");
					return;
				}
				searchTree = codeTree;
			}
			else //we are at an internal node, keep going down tree
			{
				try
				{
					if(bitInput.readBit()) //if the value is '1' (true)
					{
						searchTree = searchTree.getRight();
					}
					else //if value is '0' (false)
					{
						searchTree = searchTree.getLeft();
					}
				}
				catch(IOException e)
				{
					System.out.println("Error reading compressed file");
					return;
				}
			}
		}//end while loop

		if(searchTree != null)
		{
			//last point is excluded in while loop because hasNext() is null
			try
			{
				output.write(searchTree.getData().getChar());
			}
			catch(IOException e)
			{
				System.out.println("Could not write to decompressed file" + e.getMessage());
				return;
			}
		}

		// Close both files, if possible
		try 
		{
			bitInput.close();
		}
		catch (IOException e) {
			System.err.println("Cannot close input file.\n" + e.getMessage());
		}

		try 
		{
			output.close();
		}
		catch (IOException e) {
			System.err.println("Cannot close output file.\n" + e.getMessage());
		}
	}

	/**
	 * This method writes a compressed version of the file, one bit at a time. It reads every line of the file. For 
	 * each character, it finds the code for that character (in the HashMap) and writes that code in bits to the new 
	 * file. 
	 *  
	 * @param codeMap						HashMap with all characters and their 'codes'
	 * @param fileName						Name of file to compress
	 * @param compressedFileName			Name of compressed file to create
	 */
	public static void writeCompressed(HashMap<Character, String> codeMap, String fileName, String compressedFileName)
	{
		BufferedReader input = null;
		BufferedBitWriter bitOutput = null;
		try
		{
			input = new BufferedReader(new FileReader(fileName));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Could not open input file");
		}

		try
		{
			bitOutput = new BufferedBitWriter(compressedFileName);
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Could not open compressed output file");
		}

		try {
			// Line by line
			String line;
			int lineNum = 0;
			while ((line = input.readLine()) != null) {
				System.out.println("read @"+lineNum+"`"+line+"'");

				for(int i = 0; i < line.length(); i ++) //find each character in the line
				{
					Character thisChar = line.charAt(i);
					String code = codeMap.get(thisChar); //find code for this character

					for(int j = 0; j < code.length(); j ++) //write code for this character to output 
					{
						bitOutput.writeBit(code.charAt(j)=='1');
					}
				}

				String code = codeMap.get('\n'); //write '\n' code after each line finishes!                      
				for(int i = 0; i < code.length(); i ++)
				{
					bitOutput.writeBit(code.charAt(i)=='1');
				}
				lineNum++;
			}
		}
		catch (IOException e) {
			System.err.println("IO error while reading.\n" + e.getMessage());
		}

		// Close both files, if possible
		try {
			input.close();
		}
		catch (IOException e) {
			System.err.println("Cannot close input file.\n" + e.getMessage());
		}

		try {
			bitOutput.close();
		}
		catch (IOException e) {
			System.err.println("Cannot close output file.\n" + e.getMessage());
		}

	}
	
	/**
	 * Generates the code map; Uses a helper to do the real work.
	 * @param tree
	 * @return
	 */
	public static HashMap<Character, String> generateCodeMap(BinaryTree<ValueChar> tree) 
	{
		HashMap<Character, String> code = new HashMap<Character, String>();
		generateCodeMap(tree, code, ""); 
		return code;
	}

	/**
	 * Helper method for generateCodeMap(). Starts at head, and recursively works its way down to each 
	 * leaf, adding '0' if it goes to the left node, and '1' if it goes to the right node.
	 * 
	 * @param tree
	 * @param code
	 * @param codeString
	 */
	public static void generateCodeMap(BinaryTree<ValueChar> tree, HashMap<Character, String> code, String codeString)
	{
		if(tree == null)
		{
			return;
		}
		if(tree.isLeaf()) //if leaf, add to HashMap
		{
			code.put(tree.getData().getChar(), codeString); 
		}
		else //internal node: recursively call left and right node, adding '0' or '1'
		{
			if(tree.hasLeft())
			{
				generateCodeMap(tree.getLeft(), code, codeString + "0");
			}
			if(tree.hasRight())
			{
				generateCodeMap(tree.getRight(), code, codeString + "1");
			}
		}
	}
	
	/**
	 * Generates a binary tree from a queue of binary trees. Removes the trees of highest priority, 
	 * @param queueTree				PriorityQueue of all characters/value pairs, stored in their own BinaryTree.
	 * @return						Tree in order as indicated by the Huffman method
	 */
	public static BinaryTree<ValueChar> generateTree(PriorityQueue<BinaryTree<ValueChar>> queueTree)
	{
		BinaryTree<ValueChar> finalTree = null;
		while(queueTree.size()>1)
		{
			BinaryTree<ValueChar> t1 = queueTree.remove();
			BinaryTree<ValueChar> t2 = queueTree.remove();
			queueTree.add(new BinaryTree<ValueChar>(new ValueChar('*', t1.getData().getInt()+t2.getData().getInt()), t1, t2));
		}

		if(queueTree.size()==0)
		{
			return null;
		}
		return queueTree.remove();
	}
	public static PriorityQueue<BinaryTree<ValueChar>> generateOriginalQueue(HashMap<Character, Integer> frequencies)
	{
		PriorityQueue<BinaryTree<ValueChar>> charTreeList = new PriorityQueue<BinaryTree<ValueChar>>((BinaryTree<ValueChar> v1, BinaryTree<ValueChar> v2) -> v1.getData().getInt()-v2.getData().getInt());

		for(Character thisChar: frequencies.keySet())
		{
			ValueChar thisVal = new ValueChar(thisChar, frequencies.get(thisChar));
			charTreeList.add(new BinaryTree<ValueChar>(thisVal));
		}
		return charTreeList;
	}

	/**
	 * Creates a HashMap, with keys as all of the characters in the file, and 
	 * 
	 * @param fileName			Name of the file to open
	 * @return					HashMap, with Characters as keys and Integers as values
	 */
	public static HashMap<Character,Integer> fileFrequencies(String fileName) {
		HashMap<Character, Integer> frequencies = new HashMap<Character, Integer>();
		BufferedReader input = null;

		// Open the file, if possible
		try {
			input = new BufferedReader(new FileReader(fileName));
		} 
		catch (FileNotFoundException e) {
			System.err.println("Cannot open file.\n" + e.getMessage());
			return null;
		} 

		// Read the file
		try {
			// Line by line
			String line;
			int lineNum = 0;
			while ((line = input.readLine()) != null) {
				System.out.println("read @"+lineNum+"`"+line+"'");

				//Loops through each char in this line, adds it to frequency count for that chart
				for(int i = 0; i < line.length(); i ++)
				{
					Character thisChar = line.charAt(i);

					if(!frequencies.containsKey(thisChar)) //add new char if it is first occurrence
					{
						frequencies.put(thisChar, new Integer(1));
					}
					else
					{
						frequencies.replace(thisChar, frequencies.get(thisChar).intValue()+1); //add 1 to value of that char 
					}
				}

				Character newLine = '\n';

				if(lineNum == 0) //first line: first '\n' key
				{
					frequencies.put(newLine, new Integer(1));
				}
				else
				{
					frequencies.replace(newLine, frequencies.get(newLine).intValue()+1); //every line, add to the count of '\n'
				}
				lineNum++;
			}
		}
		catch (IOException e) {
			System.err.println("IO error while reading.\n" + e.getMessage());
		}

		// Close the file, if possible
		try {
			input.close();
		}
		catch (IOException e) {
			System.err.println("Cannot close file.\n" + e.getMessage());
		}
		return frequencies;
	}


	/**
	 * Main method: calls upon static methods in this class to compress and decompress files. Must store code map 
	 * after compression and pass to decompress method in order to know the codes of each character and decompress.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BinaryTree<ValueChar> treeChars = compressFile("files/USConstitution.txt", "files/ConstitutionCompressed.txt");
		decompressFile(treeChars, "files/ConstitutionCompressed.txt", "files/ConstitutionDecoded.txt");
	}
}
