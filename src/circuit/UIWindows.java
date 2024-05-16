package circuit;

import circuit.components.Circuit;
import circuit.components.Pin;
import circuit.graphics.Editor;

import javax.swing.*;
import java.awt.*;

public class UIWindows {
	// Generates the UI to retrieve details about the circuit to save to a file
	public static void saveCircuitUI(ProgramManager manager) {
		String name = JOptionPane.showInputDialog("Circuit Name:");
		String strClockId = JOptionPane.showInputDialog("Enter index of clock pin (or -1 if none)");
		int clockId = -1;
		if (strClockId.matches("-?\\d+")) clockId = Integer.parseInt(strClockId);
		Color compCol = JColorChooser.showDialog(manager.frame, "Choose Circuit Colour", Color.WHITE);
		Color textCol = JColorChooser.showDialog(manager.frame, "Choose Circuit Text Colour", Color.WHITE);

		for (Pin p : manager.editor.currentCircuit.getInputPins()) {
			p.setState(false);
		}
		manager.editor.currentCircuit.tick(0.001);

		manager.editor.currentCircuit.setName(name);
		manager.editor.currentCircuit.setWidth(Math.max(manager.renderer.g.getFontMetrics().stringWidth(name) * 2, 40));
		manager.editor.currentCircuit.setHeight(Editor.IOPIN_GAP * Math.max(manager.editor.currentCircuit.getInputPins().size(), manager.editor.currentCircuit.getOutputPins().size()));
		manager.editor.currentCircuit.setCircuitColor(compCol.getRGB() & 0xffffff);
		manager.editor.currentCircuit.setTextColor(textCol.getRGB() & 0xffffff);

		manager.editor.currentCircuit.save(clockId);
		Circuit.CIRCUITS.put(name, manager.editor.currentCircuit);

		manager.editor.newEditor();
	}
}
