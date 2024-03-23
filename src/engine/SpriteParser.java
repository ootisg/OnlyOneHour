package engine;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A class for parsing an image into multiple subimages.
 * 
 * TODO DESCRIBE PARAMETER FORMAT
 * 
 * @author nathan
 *
 */
public class SpriteParser {
	
	/**
	 * The list of parameters for this SpriteParser
	 */
	private ArrayList<String> parameters;
	
	/**
	 * The list of variables for this SpriteParser
	 */
	private HashMap<String, String> vars;
	
	/**
	 * Constructs a new SpriteParser object from the given filepath.
	 * @param filepath The filepath to the list of parameters
	 */
	public SpriteParser (String filepath) {
		parameters = new ArrayList<String> ();
		File workingFile = new File (filepath);
		Scanner fileScanner;
		try {
			fileScanner = new Scanner (workingFile);
		} catch (FileNotFoundException e) {
			return;
		}
		while (fileScanner.hasNextLine ()) {
			parameters.add (fileScanner.nextLine ());
		}
		fileScanner.close ();
	}
	
	/**
	 * Constructs a new SpriteParser object with the given list of parameters.
	 * @param parameters The list of parameters for this parser
	 */
	public SpriteParser (ArrayList<String> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Attempts to parse without a source image, instead using any value found for the $src variable.
	 * @return A list of BufferedImage as the parsing result
	 */
	public BufferedImage[] parse () {
		return parse (null);
	}
	
	/**
	 * Parses the given source image according to this SpriteParser's parameters.
	 * @param source The source image to parse
	 * @return A list of BufferedImages as the result of parsing the image
	 */
	public BufferedImage[] parse (BufferedImage source) {
		vars = new HashMap<String, String> ();
		LinkedList<BufferedImage> frames = parseToList (source);
		return frames.toArray (new BufferedImage[0]);
	}
	
	//Private method to enable recursive calling
	private LinkedList<BufferedImage> parseToList (BufferedImage source) throws IllegalStateException {
		LinkedList<BufferedImage> frames = new LinkedList<BufferedImage> ();
		if (parameters == null || parameters.size () == 0) {
			frames.add (source);
			return frames;
		}
		String totalParam = "";
		boolean varMode = true;
		for (int i = 0; i < parameters.size (); i ++) {
			String workingParam = parameters.get (i);
			if (workingParam.equals ("")) {
				//Do nothing
			} else if (workingParam.charAt (0) == '$') {
				if (!varMode) {
					throw new IllegalStateException ("Unexpected variable declaration " + workingParam);
				}
				parseVar (workingParam);
			} else {
				if (source == null) {
					source = Sprite.getImage (vars.get ("$src"));
				}
				varMode = false;
				int depth = 0;
				do {
					char lastChar = workingParam.charAt (workingParam.length () - 1);
					if (i < parameters.size () - 1 || lastChar == ';') {
						if (lastChar == ':' || depth > 0) {
							i ++;
							if (lastChar == ':') {
								depth ++;
							} else if (lastChar == ';') {
								depth --;
							} else {
								workingParam += ",";
							}
							if (i < parameters.size ()) {
								workingParam += parameters.get (i);
							}
						}
					} else {
						if (depth != 0) {
							throw new IllegalStateException ("Unexpected end of input");
						}
					}
				} while (depth > 0);
				frames.addAll (parseParameter (source, workingParam));
			}
		}
		return frames;
	}
	
	private LinkedList<BufferedImage> parseParameter (BufferedImage source, String parameter) throws IllegalStateException {
		BufferedImage[] images;
		LinkedList<BufferedImage> frames = new LinkedList<BufferedImage> ();
		if (parameter.equals ("")) {
			return frames;
		}
		String paramText = "";
		for (int i = 0; i < parameter.length (); i ++) {
			char c = parameter.charAt (i);
			if (c == ':' || c == ';' || c == ',' || i == parameter.length () - 1) {
				String remaining = "";
				if (i < parameter.length () - 1) {
					remaining = parameter.substring (i + 1);
				}
				if (c != ':' && c != ';' && c != ',') {
					paramText += c;
				}
				Scanner paramScanner = new Scanner (paramText);
				if (paramScanner.hasNext ()) {
					int[] fields;
					switch (paramScanner.next ()) {
						case "rectangle":
							fields = getFields (paramText, 4);
							switch (c) {
								case ':':
									BufferedImage temp = source.getSubimage (fields [0], fields [1], fields [2], fields [3]);
									frames.addAll (parseParameter (temp, remaining));
									frames.addAll (parseParameter (source, skipCurrentIndent (remaining)));
									return frames;
								case ';':
									frames.add (source.getSubimage (fields [0], fields [1], fields [2], fields [3]));
									return frames;
								case ',':
									frames.add (source.getSubimage (fields [0], fields [1], fields [2], fields [3]));
									frames.addAll (parseParameter (source, remaining));
									return frames;
								default:
									frames.add (source.getSubimage (fields [0], fields [1], fields [2], fields [3]));
									return frames;
							}
							//No break here
						case "grid":
							fields = getFields (paramText, 2);
							switch (c) {
								case ':':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 0; j < images.length; j ++) {
										frames.addAll (parseParameter (images [j], remaining));
									}
									frames.addAll (parseParameter (source, skipCurrentIndent (remaining)));
									return frames;
								case ';':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 0; j < images.length; j ++) {
										frames.add (images [j]);
									}
									return frames;
								case ',':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 0; j < images.length; j ++) {
										frames.add (images [j]);
									}
									frames.addAll (parseParameter (source, remaining));
									return frames;
								default:
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 0; j < images.length; j ++) {
										frames.add (images [j]);
									}
									return frames;
							}
							//No break here
						case "indexedGrid":
							fields = getFields (paramText, -1);
							if (fields.length < 2) {
								throw new IllegalStateException ("The minimum required number of fields (2) was not found");
							}
							switch (c) {
								case ':':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 2; j < fields.length; j ++) {
										frames.addAll (parseParameter (images [fields [j]], remaining));
									}
									frames.addAll (parseParameter (source, skipCurrentIndent (remaining)));
									return frames;
								case ';':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 2; j < fields.length; j ++) {
										frames.add (images [fields[j]]);
									}
									return frames;
								case ',':
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 2; j < fields.length; j ++) {
										frames.add (images [fields[j]]);
									}
									frames.addAll (parseParameter (source, remaining));
									return frames;
								default:
									images = splitGrid (source, fields [0], fields [1]);
									for (int j = 2; j < fields.length; j ++) {
										frames.add (images [fields[j]]);
									}
									return frames;
							}
							//No break here
						default:
							break;
					}
				}
			} else {
				paramText += c;
			}
		}
		throw new IllegalStateException ();
	}
	
	/**
	 * Splits the BufferedImage src, relative to a grid, into an array of BufferedImage(s).
	 * @param src The source image
	 * @param cellWidth The width of the cells in the grid
	 * @param cellHeight The height of the cells in the grid
	 * @return The list of BufferedImages taken from the grid
	 */
	private static BufferedImage[] splitGrid (BufferedImage src, int cellWidth, int cellHeight) {
		int imgWidth = src.getWidth ();
		int imgHeight = src.getHeight ();
		int cellsHoriz = imgWidth / cellWidth;
		int cellsVert = imgHeight / cellHeight;
		BufferedImage[] images = new BufferedImage [cellsVert * cellsHoriz];
		for (int i = 0; i < cellsVert; i ++) {
			for (int j = 0; j < cellsHoriz; j ++) {
				images [i * cellsHoriz + j] = src.getSubimage (cellWidth * j, cellHeight * i, cellWidth, cellHeight);
			}
		}
		return images;
	}
	
	private static String skipCurrentIndent (String input) {
		int depth = 1;
		for (int i = 0; i < input.length (); i ++) {
			if (input.charAt (i) == ':') {
				depth ++;
			} else if (input.charAt (i) == ';') {
				depth --;
			}
			if (depth == 0) {
				if (i < input.length () - 1) {
					return input.substring (i + 1);
				} else {
					return "";
				}
			}
		}
		return "";
	}
	
	private boolean parseVar (String declaration) {
		if (declaration.charAt (0) != '$' || declaration.length () <= 1) {
			return false;
		}
		Scanner varScanner = new Scanner (declaration);
		String varName = varScanner.next ();
		String varValue = "";
		if (varScanner.hasNext ()) {
			while (varScanner.hasNext ()) {
				varValue += varScanner.next ();
				if (varScanner.hasNext ()) {
					varValue += " ";
				}
			}
			vars.put (varName, varValue);
		} else {
			vars.put (varName, "");
		}
		return true;
	}
	
	private int[] getFields (String parseString, int numFields) throws IllegalStateException {
		ArrayList<Integer> fields = new ArrayList<Integer> ();
		Scanner s = new Scanner (parseString);
		s.next ();
		int currentField = 0;
		while (s.hasNext ()) {
			String working = s.next ();
			if (working.charAt (0) == '$') {
				Scanner varScanner = new Scanner (vars.get (working));
				while (varScanner.hasNext ()) {
					if (currentField >= numFields && numFields != -1) {
						throw new IllegalStateException ("Variable " + working + " contains more fields than allowed");
					}
					fields.add (Integer.parseInt (varScanner.next ()));
					currentField ++;
				}
			} else {
				if (currentField >= numFields && numFields != -1) {
					throw new IllegalStateException ("Unexpected field");
				}
				fields.add (Integer.parseInt (working));
				currentField ++;
			}
		}
		if (fields.size () < numFields) {
			throw new IllegalStateException ("The required amount of fields (" + numFields + ") was not found");
		}
		int[] fieldsArray = new int[fields.size ()];
		for (int i = 0; i < fieldsArray.length; i ++) {
			fieldsArray [i] = fields.get (i);
		}
		return fieldsArray;
	}
}
