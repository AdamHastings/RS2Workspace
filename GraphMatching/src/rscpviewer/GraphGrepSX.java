package rscpviewer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.device.Bel;
import edu.byu.ece.rapidSmith.device.BelPin;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class GraphGrepSX {
	public static void createFile(String input_filename) {
		TincrCheckpoint goldStandard;

		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			goldStandard = VivadoInterface.loadTCP(input_filename);
		} catch (IOException e) {
			System.out.println("File not found");
			e.printStackTrace();
			return;
		}

		CellDesign goldDesign = goldStandard.getDesign();
		Collection<Cell> cells_c_gs = goldDesign.getCells();
		ArrayList<Cell> goldCells = new ArrayList<Cell>();
		ArrayList<CellNet> goldNets = new ArrayList<CellNet>(goldDesign.getNets());
		HashSet<String> goldCellPins_set = new HashSet<String>();

		for (Cell c : cells_c_gs) {
			goldCells.add(c);
			Collection<CellPin> cellpins = c.getPins();
			
			for (CellPin pin : cellpins) {		
				goldCellPins_set.add(pin.getFullName());
			}
		}

		ArrayList<String> goldCellPins = new ArrayList<String>(goldCellPins_set);
		Collections.sort(goldCellPins, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.compareTo(b);
			}
		});

		try {
			String filename = input_filename + "_ggsx";
			fw = new FileWriter(filename);
			bw = new BufferedWriter(fw);
			bw.write("#" + filename + "\n");
			bw.write(goldCellPins.size() + "\n");
			for (String pin : goldCellPins) {
				bw.write(pin + "\n");
			}
			
			
			// TODO QUESTION: Is is possible for a net to have two sources?
			// Answer: This shouldn't be a problem, i.e. it shouldn't be something that we encounter...
			int edge_count = 0;
			String edge_buffer = "";
			for (CellNet n : goldNets) {
				List<CellPin> sources = n.getAllSourcePins();
				if (sources.size() == 0) {
					System.out.println("WARNING: 0 sources");
				} else if (sources.size() == 1) {
					Collection<CellPin> sinks = n.getSinkPins();
					CellPin source = sources.get(0);
					int source_index = goldCellPins.indexOf(source.getFullName());
					for (CellPin sink : sinks) {
						int sink_index = goldCellPins.indexOf(sink.getFullName());
						edge_buffer += source_index + " " + sink_index + " " + n.getName() + "\n";
						edge_count = edge_count + 1;
					}
				} else {
					System.out.println("WARNING: 2+ sources");
					return; // Is this return necessary? How should multiple sources be handled?
				}
			}
			// TODO: I'm guessing we'll want to label the edges to help with speedup?
			// According the GraphGrepSX_v3.3/exs/db.geu file, it looks like the format for edges is
			// <source> <sink> <name>
			// Let's test this out though and test it just to be sure?
			bw.write(edge_count + "\n");
			bw.write(edge_buffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/** Create a database or query file for GraphGrepSX3.3
	 * 	The function creates a file using the -ged format
	 *	(-ged => directed graph with labeled edges and vertices)
	 * 
	 * @param nets
	 * @param filename
	 */
	public static void createFile(Collection<CellNet> nets, String filename) {
		
		String moduleName = "aes_128_0" + "/";
		
		// Objects for file writing
		BufferedWriter bw = null;
		FileWriter fw = null;

		// A set to contain all pins connected to the nets
		HashSet<String> CellPins_set = new HashSet<String>();

		// Get all pins connected to all nets
		for (CellNet n : nets) {
			// Get all pins connected to this net
			if (n == null) {
				System.out.println("Null CellNet?");
			}
			Collection<CellPin> cellpins = n.getPins();
			
			// Add each of the pins to the goldcellPins_set
			for (CellPin pin : cellpins) {		
				CellPins_set.add(pin.getFullName());
			}
		}

		// Place the goldCellPins_set into a list
		ArrayList<String> goldCellPins = new ArrayList<String>(CellPins_set);
		// Sort the list in lexicographical order
		Collections.sort(goldCellPins, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.compareTo(b);
			}
		});

		
		// Begin writing the output file
		try {
			// Write file header
			filename = filename + "_ggsx";
			fw = new FileWriter(filename);
			bw = new BufferedWriter(fw);
			bw.write("#" + filename + "\n");
			
			// Write the number of CellPins found
			bw.write(goldCellPins.size() + "\n");
			// Write each CellPin, in lexicographical order
			for (String pin : goldCellPins) {
				// Is this the best way to do this? Do you see any potential bugs from this approach?
				if (pin.startsWith(moduleName)) {
					pin = pin.replaceFirst("^" + moduleName, "");				
				}
				bw.write(pin + "\n");
			}
			
			int edge_count = 0;
			String edge_buffer = "";
			for (CellNet n : nets) {
				CellPin sources = n.getSourcePin();
				if (sources == null) {
					System.out.println("WARNING: 0 sources");
					System.out.println(n.toString());
					Collection<CellPin> sinks = n.getSinkPins();
					System.out.println(sinks.toString());
					System.out.println(n.getPins());
				} else {  // if (sources.size() == 1) {
					Collection<CellPin> sinks = n.getSinkPins();
					CellPin source = sources;
					int source_index = goldCellPins.indexOf(source.getFullName());
					for (CellPin sink : sinks) {
						
						// Again...are there any potential problems with doing this?
						String name = n.getName();
						if (name.startsWith(moduleName)) {
							name = name.replaceFirst("^" + moduleName, "");				
						}
						
						
						int sink_index = goldCellPins.indexOf(sink.getFullName());
						edge_buffer += source_index + " " + sink_index + " " + name + "\n";
						edge_count = edge_count + 1;
					}
				} /*else {
					System.out.println("WARNING: 2+ sources");
					return; // Is this return necessary? How should multiple sources be handled?
				}*/
			}
			
			bw.write(edge_count + "\n");
			bw.write(edge_buffer);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close the file
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//TODO should this be available across calls to this class? Should one graph see the map of another?
		private static Map<String, Integer> pinToOffset;
		
		/** This function checks the pinToOffset map for possible discrepancies between the offset
		 * seen for the current BelPin and what has typically happened in the past. 
		 * 
		 * @param String belAndPinName 
		 * @param int offset 
		 */
		private static void verifyPinMapping(String belAndPinName, int offset)
		{
			if(pinToOffset.containsKey(belAndPinName))
			{
				if(pinToOffset.get(belAndPinName) != offset)
					System.out.println("ERROR: Bels have different pin offsets");
			}
			else
			{
				pinToOffset.put(belAndPinName, offset);
			}
		}
		
		/** This function determines what vertices correspond to a Flip Flop
		 * 
		 * A FF Graph is of the following form: FF_{NumInputs}_{NumOutputs} {Input}* {Output}*
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @param ArrayList<String> vertices - This is where created vertices should be passed back
		 * @param ArrayList<String> edges - This is where created edges should be passed back
		 * @return int ret - This represents the number of vertices (or the number of edges + 1)
		 */
		private static int createFFGraph(int index, Cell cell, ArrayList<String> vertices, ArrayList<String> edges)
		{
			int offset = 0;
			Bel bel = cell.getBel();
			Collection<BelPin> sources = bel.getSources();
			Collection<BelPin> sinks = bel.getSinks();
			
			//FF vertex
			String vertex = "FF_" + sinks.size() + '_' + sources.size();
			vertices.add(vertex);
			offset++;
			
			String edge;
			//sinks (inputs)
			for(BelPin belPin : sinks)
			{
				//vertex
				vertex = "FF_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "";
				edge += index+offset;					//From
				edge += " " + index + " FF_INTERNAL";	//To
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			//sources (outputs)
			for(BelPin belPin : sources)
			{
				//vertex
				vertex = "FF_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "" + index + " ";			//From
				edge += index+offset;				//To
				edge += " FF_INTERNAL";
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			return offset;
		}
		
		/** This function determines what vertices correspond to a CARRY
		 * 
		 * A CARRY Graph is of the following form: CARRY_{NumInputs}_{NumOutputs} {Input}* {Output}*
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @param ArrayList<String> vertices - This is where created vertices should be passed back
		 * @param ArrayList<String> edges - This is where created edges should be passed back
		 * @return int ret - This represents the number of vertices (or the number of edges + 1)
		 */
		private static int createCARRYGraph(int index, Cell cell, ArrayList<String> vertices, ArrayList<String> edges)
		{
			int offset = 0;
			Bel bel = cell.getBel();
			Collection<BelPin> sources = bel.getSources();
			Collection<BelPin> sinks = bel.getSinks();
			
			//CARRY vertex
			String vertex = "CARRY_" + sinks.size() + '_' + sources.size();
			vertices.add(vertex);
			offset++;
			
			String edge;
			//sinks (inputs)
			for(BelPin belPin : sinks)
			{
				//vertex
				vertex = "CARRY_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "";
				edge += index+offset;						//From
				edge += " " + index + " CARRY_INTERNAL";	//To
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			//sources (outputs)
			for(BelPin belPin : sources)
			{
				//vertex
				vertex = "CARRY_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "" + index + " ";			//From
				edge += index+offset;				//To
				edge += " CARRY_INTERNAL";
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			return offset;
		}
		
		/** This function determines what vertices correspond to a LUT. This appends the LUT Configuration
		 * to the main vertex.
		 * 
		 * A LUT Graph is of the following form: LUT_{NumInputs}_{NumOutputs}_{INIT} {Input}* {Output}*
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @param ArrayList<String> vertices - This is where created vertices should be passed back
		 * @param ArrayList<String> edges - This is where created edges should be passed back
		 * @return int ret - This represents the number of vertices (or the number of edges + 1)
		 */
		private static int createLUTGraph(int index, Cell cell, ArrayList<String> vertices, ArrayList<String> edges)
		{
			int offset = 0;
			Bel bel = cell.getBel();
			Collection<BelPin> sources = bel.getSources();
			Collection<BelPin> sinks = bel.getSinks();
			
			//LUT vertex
			String vertex = "LUT_" + sinks.size() + '_' + sources.size() + '_' + cell.getProperties().get("INIT").getStringValue();
			vertices.add(vertex);
			offset++;
			
			String edge;
			//sinks (inputs)
			for(BelPin belPin : sinks)
			{
				//vertex
				vertex = "LUT_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "";
				edge += index+offset;					//From
				edge += " " + index + " LUT_INTERNAL";	//To
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			//sources (outputs)
			for(BelPin belPin : sources)
			{
				//vertex
				vertex = "LUT_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "" + index + " ";			//From
				edge += index+offset;				//To
				edge += " LUT_INTERNAL";
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			return offset;
		}
		
		/**
		 * This function gets all of the properties that are important for the initialization 
		 * of a RAM cell
		 * @param cell
		 * @return A _ delimited string of the initialization properties
		 */
		private static String getRAMINIT(Cell cell)
		{
			//There are other properties that we might want to use as well:
			//	SRVAL A, SRVAL B, WRITE MODE A, WRITE MODE B, WRITE WIDTH A, WRITE WIDTH B
			//	bram addr begin, bram addr end, bram slice begin, bram slice end,
			//  READ WIDTH A, READ WIDTH B
			String ramINIT[] = {"INITP_00", "INITP_01", "INITP_02", "INITP_03", 
								"INITP_04", "INITP_05", "INITP_06", "INITP_07", 
								"INIT_00", "INIT_01", "INIT_02", "INIT_03", 
								"INIT_04", "INIT_05", "INIT_06", "INIT_07", 
								"INIT_08", "INIT_09", "INIT_0A", "INIT_0B", 
								"INIT_0C", "INIT_0D", "INIT_0E", "INIT_0F", 
								"INIT_10", "INIT_11", "INIT_12", "INIT_13", 
								"INIT_14", "INIT_15", "INIT_16", "INIT_17", 
								"INIT_18", "INIT_19", "INIT_1A", "INIT_1B", 
								"INIT_1C", "INIT_1D", "INIT_1E", "INIT_1F", 
								"INIT_20", "INIT_21", "INIT_22", "INIT_23", 
								"INIT_24", "INIT_25", "INIT_26", "INIT_27", 
								"INIT_28", "INIT_29", "INIT_2A", "INIT_2B", 
								"INIT_2C", "INIT_2D", "INIT_2E", "INIT_2F", 
								"INIT_30", "INIT_31", "INIT_32", "INIT_33", 
								"INIT_34", "INIT_35", "INIT_36", "INIT_37", 
								"INIT_38", "INIT_39", "INIT_3A", "INIT_3B", 
								"INIT_3C", "INIT_3D", "INIT_3E", "INIT_3F",
								"INIT_A", "INIT_B"
			};
			StringBuilder sb = new StringBuilder();
			
			for(String property : ramINIT)
			{
				sb.append(cell.getProperties().get(property).getStringValue());
				sb.append('_');
			}
			sb.setLength(sb.length() - 1);
			
			return sb.toString();
		}
		
		/** This function determines what vertices correspond to a RAMB18E1. This appends the RAM 
		 * Configuration to the main vertex.
		 * 
		 * A RAM Graph is of the following form: RAM_{NumInputs}_{NumOutputs}_{INIT} {Input}* {Output}*
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @param ArrayList<String> vertices - This is where created vertices should be passed back
		 * @param ArrayList<String> edges - This is where created edges should be passed back
		 * @return int ret - This represents the number of vertices (or the number of edges + 1)
		 */
		private static int createRAMGraph(int index, Cell cell, ArrayList<String> vertices, ArrayList<String> edges)
		{
			int offset = 0;
			Bel bel = cell.getBel();
			Collection<BelPin> sources = bel.getSources();
			Collection<BelPin> sinks = bel.getSinks();
			
			//LUT vertex
			String vertex = "RAM_" + sinks.size() + '_' + sources.size() + '_' + getRAMINIT(cell);
			vertices.add(vertex);
			offset++;
			
			String edge;
			//sinks (inputs)
			for(BelPin belPin : sinks)
			{
				//vertex
				vertex = "RAM_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "";
				edge += index+offset;					//From
				edge += " " + index + " RAM_INTERNAL";	//To
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			//sources (outputs)
			for(BelPin belPin : sources)
			{
				//vertex
				vertex = "RAM_" + belPin.getName();
				vertices.add(vertex);
				
				//edge
				edge = "" + index + " ";			//From
				edge += index+offset;				//To
				edge += " RAM_INTERNAL";
				edges.add(edge);
				
				//BelPin offset
				//This should be the same as how the map keys are queried (when connecting up the various logic elements)
				String belAndPinName = bel.getTemplate().getId() + "_" + belPin.getName();
				verifyPinMapping(belAndPinName, offset);
				
				offset++;
			}
			
			return offset;
		}
		
		/** This function determines what vertices correspond to a bel
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @return int ret - This represents the number of vertices (or the number of edges + 1)
		 */
		private static int createCellSubGraph(int index, Cell cell, ArrayList<String> vertices, ArrayList<String> edges)
		{
			String belType = cell.getBel().getType();
//			System.out.println(belType);
			
			//Flip Flops
			Pattern p = Pattern.compile("[ABCD]5*FF");
			if(p.matcher(belType).matches())
				return createFFGraph(index, cell, vertices, edges);
			
			//CARRY4
			p = Pattern.compile("CARRY4");
			if(p.matcher(belType).matches())
				return createCARRYGraph(index, cell, vertices, edges);
			
			//LUT
			p = Pattern.compile("[ABCD][56]LUT");
			if(p.matcher(belType).matches())
				return createLUTGraph(index, cell, vertices, edges);
			
			//RAMB18E1
			p = Pattern.compile("RAMB18E1");
			if(p.matcher(belType).matches())
				return createRAMGraph(index, cell, vertices, edges);
			
			System.out.println("ERROR: Program does not account for BelType " + belType);
			return 0;
		}
		
		/** Create a database or query file for GraphGrepSX3.3
		 * 	The function creates a file using the -ged format
		 *	(-ged => directed graph with labeled edges and vertices)
		 * 
		 * @param filename - The file to which the output will be written
		 * @param cells_c - A collection of Cells - each cell should be adjacent to at least one other cell
		 * @param nets_c - A collection of Nets - Should source and sink only at cells in cells_c
		 */
		public static void createExpandedGraphFile(String filename, Collection<Cell> cells_c, Collection<CellNet> nets_c) 
		{
			// File and output variables
			String filenameWithPostfix = "IO/" + filename + "_ggsx";
			StringBuilder sb0 = new StringBuilder();
			sb0.append("#" + filenameWithPostfix + "\n");
			
			//Graph variables
			//This is a global because it should not be specific to a design
			pinToOffset = new HashMap<String, Integer>();
			ArrayList<String> vertices = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();
			Map<Cell, Integer> cellToVertex = new HashMap<Cell, Integer>();
			ArrayList<Cell> processedCells = new ArrayList<Cell>();
			//This keeps track of the vertex number of the center vertex representing the cell
			int index = 0;
			
			//Iterate through the cells and find placed cells, constructing subgraphs
			System.out.println("Creating subgraphs for cells in " + filename + ".");
			for(Cell cell : cells_c)
			{
				if(cell.getBel() == null)
					System.out.println("WARNING: " + cell.toString() + " has no bel.");
				else
				{
					//The cell is placed. Make a subgraph for it and add it to the overall graph
					int edgesCreated  = createCellSubGraph(index, cell, vertices, edges);
					//Keep track of where in the graph the cell is placed
					cellToVertex.put(cell, index);
					processedCells.add(cell);
					index += edgesCreated;
				}
			}
			//At this point we know exactly how many vertices there are in the graph
			sb0.append(vertices.size());
			sb0.append('\n');
			for(String vertex : vertices)
				sb0.append(vertex + '\n');

			System.out.println("Linking up subgraphs in " + filename + ".");
			Set<Cell> placedCells = cellToVertex.keySet();
			//Connect up the various logic elements
			for(CellNet net : nets_c)
			{
				for(CellPin sourcePin : net.getAllSourcePins())
				{
					//Check to make sure the pin is mapped to only one belPin
					Set<BelPin> sourceBelPins = sourcePin.getMappedBelPins();
					if(sourceBelPins.size() != 1)
					{
						System.out.println("WARNING: " + sourcePin + " is mapped to " + sourceBelPins.size() + " BelPins.");
						continue;
					}
					
					//If the source pin belongs to a placed cell
					if(placedCells.contains(sourcePin.getCell()))
					{					
						//Make sure the graph has the edge connected to the right sourceBelPin
						int sourceIndex = cellToVertex.get(sourcePin.getCell());
						BelPin sourceBelPin = sourcePin.getMappedBelPin();
						//This should be the same as how the map keys are created (in each of the create*Graph functions)
						int sourceOffset = pinToOffset.get(sourceBelPin.getBel().getTemplate().getId() + "_" + sourceBelPin.getName());
						
						for(CellPin sinkPin : net.getSinkPins())
						{
							//Check to make sure the pin is mapped to only one belPin
							Set<BelPin> sinkBelPins = sinkPin.getMappedBelPins();
							if(sinkBelPins.size() != 1)
							{
								System.out.println("WARNING: " + sinkPin + " is mapped to " + sinkBelPins.size() + " BelPins.");
								continue;
							}
							
							//If the sink pin belongs to a placed cell
							if(placedCells.contains(sinkPin.getCell()))
							{
								//Make sure the graph has the edge connected to the right sinkBelPin
								int sinkIndex = cellToVertex.get(sinkPin.getCell());
								BelPin sinkBelPin = sinkPin.getMappedBelPin();
								//This should be the same as how the map keys are created (in each of the create*Graph() functions)
								int sinkOffset = pinToOffset.get(sinkBelPin.getBel().getTemplate().getId() + "_" + sinkBelPin.getName());
								
								String edge = (sourceIndex+sourceOffset) + " " + (sinkIndex+sinkOffset) + " WIRE";
								//Edges should not be included twice
								if(!edges.contains(edge))
									edges.add(edge);
							}
						}
					}
				}
			}
			
			//At this point we know exactly how many edges there are in the graph
			sb0.append(edges.size());
			sb0.append('\n');
			for(String edge : edges)
				sb0.append(edge + '\n');
			
			//Perform the writing
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filenameWithPostfix), "utf-8"))) {
				System.out.println("Writing ggsx file to " + filenameWithPostfix);
				
				writer.write(sb0.toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//This functionality relies heavily on the fact that first all bels are transformed 
			//into subgraphs, and that within those subgraphs the all of the input pins are
			//processed before the output pins.
			boolean printDOTGraph = false;
			if(printDOTGraph)
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("IO/" + filename + ".dot"), "utf-8"))) {
					System.out.println("Writing dot file to IO/" + filename + ".dot");
					writer.write("digraph " + filename + " {\n");
					writer.write("\tcompound=true;\n");

					//Coming into this for loop it should always be processing the first edge in a subgraph
					int edgesProcessed = 0;
					int belsProcessed = 0;
					while(belsProcessed < cellToVertex.keySet().size())
					{
						writer.write("\tsubgraph cluster" + belsProcessed + " {\n");
						writer.write("\t\tlabel=\""+processedCells.get(belsProcessed)+"\";\n");
						
						int thisSourceIndex = 0, thisSinkIndex = 0, nextSourceIndex = 0, nextSinkIndex = 0;
						do
						{
							//Find the indices of vertices
							String thisEdge = edges.get(edgesProcessed);
							String[] theseTokens = thisEdge.split(" ");
							assert(theseTokens.length == 3);
							thisSourceIndex = Integer.parseInt(theseTokens[0]);
							thisSinkIndex = Integer.parseInt(theseTokens[1]);
						
							//Create the edge 
							String source = vertices.get(thisSourceIndex);
							String sink = vertices.get(thisSinkIndex);
							
							//Write edge
							writer.write("\t\t\"" + theseTokens[0]+"_"+source + "\" -> \"" +
									theseTokens[1]+"_"+sink + 
									"\" [label=\""+theseTokens[2]+"\"];\n");
							
							edgesProcessed++;

							//Find the indices of vertices (this is solely for the 
							//check in the while loop)
							if(edgesProcessed < edges.size())
							{
								String nextEdge = edges.get(edgesProcessed);
								String[] nextTokens = nextEdge.split(" ");
								nextSourceIndex = Integer.parseInt(nextTokens[0]);
								nextSinkIndex = Integer.parseInt(nextTokens[1]);
							}
							else
							{
								//For graphs only composed of unconnected cells (i.e.
								//a graph of one Cell, or two Cells that are not connected)
								break;
							}
						}
						while(!(thisSourceIndex < thisSinkIndex && nextSourceIndex > nextSinkIndex) && edges.get(edgesProcessed).contains("INTERNAL"));
						
						writer.write("\t}\n");
						belsProcessed++;
					}
					
					for(; edgesProcessed < edges.size(); edgesProcessed++)
					{
						String edge = edges.get(edgesProcessed);
						//Find the nodes in vertex list
						String[] tokens = edge.split(" ");
						assert(tokens.length == 3);
						int sourceIndex = Integer.parseInt(tokens[0]);
						int sinkIndex = Integer.parseInt(tokens[1]);

						//Create the edge 
						String source = vertices.get(sourceIndex);
						String sink = vertices.get(sinkIndex);
						
						writer.write("\t\"" + tokens[0]+"_"+source + "\" -> \"" +
											tokens[1]+"_"+sink + 
											"\" [label=\""+tokens[2]+"\"];\n");
					}
					
					writer.write("}");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		/** This function determines what vertices correspond to a bel
		 * 
		 * @param Cell cell - The cell that the graph will be created from
		 * @return String ret - This represents the vertex
		 */
		private static String createCellVertex(Cell cell)
		{
			String belType = cell.getBel().getType();
//			System.out.println(belType);
			
			//Flip Flops
			Pattern p = Pattern.compile("[ABCD]5*FF");
			if(p.matcher(belType).matches())
				return "FF";
			
			//CARRY4
			p = Pattern.compile("CARRY4");
			if(p.matcher(belType).matches())
				return "CARRY4";
			
			//LUT
			p = Pattern.compile("[ABCD][56]LUT");
			if(p.matcher(belType).matches())
				return "LUT";
			
			//RAMB18E1
			p = Pattern.compile("RAMB18E1");
			if(p.matcher(belType).matches())
				return "RAMB18E1";
			
			System.out.println("ERROR: Program does not account for BelType " + belType);
			return null;
		}
		
		/** Create a database or query file for GraphGrepSX3.3
		 * 	The function creates a file using the -ged format
		 *	(-ged => directed graph with labeled edges and vertices)
		 * 
		 * @param filename - The file to which the output will be written
		 * @param cells_c - A collection of Cells - each cell should be adjacent to at least one other cell
		 * @param nets_c - A collection of Nets - Should source and sink only at cells in cells_c
		 */
		public static void createGraphFile(String filename, Collection<Cell> cells_c, Collection<CellNet> nets_c) 
		{
			// File and output variables
			String filenameWithPostfix = "IO/" + filename + "_ggsx";
			StringBuilder sb0 = new StringBuilder();
			sb0.append("#" + filenameWithPostfix + "\n");
			
			//Graph variables
			//This is a global because it should not be specific to a design
			pinToOffset = new HashMap<String, Integer>();
			ArrayList<String> vertices = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();
			Map<Cell, Integer> cellToVertex = new HashMap<Cell, Integer>();
			ArrayList<Cell> processedCells = new ArrayList<Cell>();
			//This keeps track of the vertex number of the center vertex representing the cell
			int index = 0;
			
			//Iterate through the cells and find placed cells
			System.out.println("Creating vertices for cells in " + filename + ".");
			for(Cell cell : cells_c)
			{
				if(cell.getBel() == null)
					System.out.println("WARNING: " + cell.toString() + " has no bel.");
				else
				{
					//The cell is placed. Make a vertex for it and add it to the overall graph
					vertices.add(createCellVertex(cell));
					
					//Keep track of where in the graph the cell is placed
					cellToVertex.put(cell, index);
					processedCells.add(cell);
					index++;
				}
			}
			//At this point we know exactly how many vertices there are in the graph
			sb0.append(vertices.size());
			sb0.append('\n');
			for(String vertex : vertices)
				sb0.append(vertex + '\n');

			System.out.println("Linking up vertices in " + filename + ".");
			Set<Cell> placedCells = cellToVertex.keySet();
			//Connect up the various logic elements
			for(CellNet net : nets_c)
			{
				for(CellPin sourcePin : net.getAllSourcePins())
				{
					//Check to make sure the pin is mapped to only one belPin
					Set<BelPin> sourceBelPins = sourcePin.getMappedBelPins();
					if(sourceBelPins.size() != 1)
					{
						System.out.println("WARNING: " + sourcePin + " is mapped to " + sourceBelPins.size() + " BelPins.");
						continue;
					}
					
					//If the source pin belongs to a placed cell
					if(placedCells.contains(sourcePin.getCell()))
					{					
						//Make sure the graph has the edge connected to the right sourceBelPin
						int sourceIndex = cellToVertex.get(sourcePin.getCell());
						
						for(CellPin sinkPin : net.getSinkPins())
						{
							//Check to make sure the pin is mapped to only one belPin
							Set<BelPin> sinkBelPins = sinkPin.getMappedBelPins();
							if(sinkBelPins.size() != 1)
							{
								System.out.println("WARNING: " + sinkPin + " is mapped to " + sinkBelPins.size() + " BelPins.");
								continue;
							}
							
							//If the sink pin belongs to a placed cell
							if(placedCells.contains(sinkPin.getCell()))
							{
								//Make sure the graph has the edge connected to the right sinkBelPin
								int sinkIndex = cellToVertex.get(sinkPin.getCell());
								
								String edge = sourceIndex + " " + sinkIndex + " WIRE";
								//Edges should not be included twice
								if(!edges.contains(edge))
									edges.add(edge);
							}
						}
					}
				}
			}
			
			//At this point we know exactly how many edges there are in the graph
			sb0.append(edges.size());
			sb0.append('\n');
			for(String edge : edges)
				sb0.append(edge + '\n');
			
			//Perform the writing
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filenameWithPostfix), "utf-8"))) {
				System.out.println("Writing ggsx file to " + filenameWithPostfix);
				
				writer.write(sb0.toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//This functionality relies heavily on the fact that first all bels are transformed 
			//into subgraphs, and that within those subgraphs the all of the input pins are
			//processed before the output pins.
			boolean printDOTGraph = false;
			if(printDOTGraph)
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("IO/" + filename + ".dot"), "utf-8"))) {
					System.out.println("Writing dot file to IO/" + filename + ".dot");
					writer.write("digraph " + filename + " {\n");
					
					for(String edge : edges)
					{
						//Find the nodes in vertex list
						String[] tokens = edge.split(" ");
						assert(tokens.length == 3);
						int sourceIndex = Integer.parseInt(tokens[0]);
						int sinkIndex = Integer.parseInt(tokens[1]);

						//Create the edge 
						String source = vertices.get(sourceIndex);
						String sink = vertices.get(sinkIndex);
						
						writer.write("\t\"" + tokens[0]+"_"+source + "\" -> \"" +
											tokens[1]+"_"+sink + 
											"\" [label=\""+tokens[2]+"\"];\n");
					}
					
					writer.write("}");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
}

