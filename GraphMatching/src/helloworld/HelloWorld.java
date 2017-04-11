package helloworld;

import java.io.IOException;

import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellLibrary;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class HelloWorld {

	public static void main(String[] args) throws IOException {
		System.out.println("Hello World");
		
		TincrCheckpoint tcp = VivadoInterface.loadTCP("/home/adam/vivado_workspace/ooc_counter/counter0_gs.tcp");
		CellDesign design = tcp.getDesign();
		Device device = tcp.getDevice();
		CellLibrary libCells = tcp.getLibCells();
				
		VivadoInterface.writeTCP("/home/adam/test/", design, device, libCells);
	}

}
