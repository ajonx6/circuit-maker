package circuit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static int colourLerp(int c1, int c2, float value) {
        return (0xff << 24) | ((int) ((float) ((c1 >> 16) & 0xff) * value) + (int) ((float) ((c2 >> 16) & 0xff) * (1f - value))) << 16 | ((int) ((float) ((c1 >> 8) & 0xff) * value) + (int) ((float) ((c2 >> 8) & 0xff) * (1f - value))) << 8 | ((int) ((float) (c1 & 0xff) * value) + (int) ((float) (c2 & 0xff) * (1f - value)));
    }

    public static double distanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double sqr(double x) { return x * x; }

    public static double dist2(double x1, double y1, double x2, double y2) { return sqr(x2 - x1) + sqr(y2 - y1); }

    public static double distanceFromLine(double x1, double y1, double x2, double y2, double p, double q) {
        double l2 = dist2(x1, y1, x2, y2);
        if (l2 == 0) return Math.sqrt(dist2(p, q, x1, y1));
        double t = ((p - x1) * (x2 - x1) + (q - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        return Math.sqrt(dist2(p, q, x1 + t * (x2 - x1), y1 + t * (y2 - y1)));
    }

    public static List<String> load(String name) {
        List<String> data = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader("res/" + name))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                data.add(line);
            }
            return data;
        } catch (IOException e) {
            System.err.println("Could not load: res/" + name);
            System.exit(1);
        }
        return null;
    }

    public static boolean isPointWithinBox(int x, int y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= (bx + bw) && y >= by && y <= (by + bh);
    }

    // public static void loadComponents() {
    //     List<String> order = load("backup_order.txt");
    //     // File folder = new File("res/components/");
    //     // File[] files = folder.listFiles();
    //     // File f = null;
    //     // for (File file : files) {
    //     //     if (file.getName().equals(s)) f = file;
    //     // }
    //     // System.out.println(s);
    //     for (String s : order) {
    //         List<String> lines = load("components/" + s + ".txt");
    //         Circuit c = null;
    //         for (String line : lines) {
    //             String[] tokens = line.split(" ");
    //             if (tokens[0].equals("n")) {
    //                 c = new Circuit();
    //                 c.setName(line.substring(2));
    //             } else if (tokens[0].equals("d")) {
    //                 c.setDims(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
    //             } else if (tokens[0].equals("c")) {
    //                 c.setCompColor(Integer.parseInt(tokens[1], 16));
    //             } else if (tokens[0].equals("t")) {
    //                 c.setTextColor(Integer.parseInt(tokens[1], 16));
    //             } else if (tokens[0].equals("p")) {
    //                 c.addPin(new Pin(), Circuit.PinType.getByLetter(tokens[2]));
    //             } else if (tokens[0].equals("w")) {
    //                 c.addWire(new Wire(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4])));
    //             } else if (tokens[0].equals("b")) {
    //                 BaseCircuit bc = null;
    //                 if (tokens[1].equals("O")) bc = new OrGate();
    //                 if (tokens[1].equals("N")) bc = new NotGate();
    //                 if (tokens[1].equals("A")) bc = new AndGate();
    //                 bc.setPids(Integer.parseInt(tokens[2]), 0, Integer.parseInt(tokens[3]), 0, Integer.parseInt(tokens[4]), 0);
    //                 c.addBaseCircuit(bc);
    //             } else if (tokens[0].equals("s")) {
    //                 // c.addSegmentDisplay(new SevenSegmentDisplay());
    //             } else if (tokens[0].equals("END")) {
    //                 CircuitList.circuitList.add(c);
    //             }
    //         }
    //     }
    // }
    //
    // public static void loadComponentss() {
    //     List<String> lines = load("circuit_data.txt");
    //     Circuit c = null;
    //     for (String line : lines) {
    //         String[] tokens = line.split(" ");
    //         if (tokens[0].equals("n")) {
    //             c = new Circuit();
    //             c.setName(line.substring(2));
    //         } else if (tokens[0].equals("d")) {
    //             c.setDims(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
    //         } else if (tokens[0].equals("c")) {
    //             c.setCompColor(Integer.parseInt(tokens[1], 16));
    //         } else if (tokens[0].equals("t")) {
    //             c.setTextColor(Integer.parseInt(tokens[1], 16));
    //         } else if (tokens[0].equals("p")) {
    //             c.addPin(new Pin(), Circuit.PinType.getByLetter(tokens[2]));
    //         } else if (tokens[0].equals("w")) {
    //             c.addWire(new Wire(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4])));
    //         } else if (tokens[0].equals("b")) {
    //             BaseCircuit bc = null;
    //             if (tokens[1].equals("O")) bc = new OrGate();
    //             if (tokens[1].equals("N")) bc = new NotGate();
    //             if (tokens[1].equals("A")) bc = new AndGate();
    //             bc.setPids(Integer.parseInt(tokens[2]), 0, Integer.parseInt(tokens[3]), 0, Integer.parseInt(tokens[4]), 0);
    //             c.addBaseCircuit(bc);
    //         } else if (tokens[0].equals("s")) {
    //             // c.addSegmentDisplay(new SevenSegmentDisplay());
    //         } else if (tokens[0].equals("END")) {
    //             CircuitList.circuitList.add(c);
    //         }
    //     }
    // }
    //
    // public static void saveComponent(Circuit c) {
    //     c.calculateHeight();
    //     StringBuilder sb = new StringBuilder("");
    //     sb.append("n " + c.name + "\n");
    //     sb.append("d " + c.width + " " + c.height + "\n");
    //     sb.append("c " + Integer.toHexString(c.compColor) + "\n");
    //     sb.append("t " + Integer.toHexString(c.textColor) + "\n");
    //     for (int p : c.pins.keySet()) {
    //         sb.append("p " + p + " ");
    //         if (c.inputPins.contains(p)) sb.append("I\n");
    //         else if (c.outputPins.contains(p)) sb.append("O\n");
    //         else sb.append("N\n");
    //     }
    //     for (int w : c.wires.keySet()) {
    //         sb.append("w " + c.wires.get(w).getPin1ID() + " " + c.wires.get(w).getPin1CID() + " " + c.wires.get(w).getPin2ID() + " " + c.wires.get(w).getPin2CID() + "\n");
    //     }
    //     for (int b : c.baseCircuits.keySet()) {
    //         sb.append("b ");
    //         if (c.baseCircuits.get(b) instanceof NotGate) sb.append("N ");
    //         else if (c.baseCircuits.get(b) instanceof OrGate) sb.append("O ");
    //         else if (c.baseCircuits.get(b) instanceof AndGate) sb.append("A ");
    //         sb.append(c.baseCircuits.get(b).toString() + "\n");
    //     }
    //     // for (int ignored : c.segments.keySet()) {
    //     //     sb.append("s\n");
    //     // }
    //     sb.append("END\n");
    //     try (BufferedWriter bw = new BufferedWriter(new FileWriter("res/components/" + c.name + ".txt"))) {
    //         bw.append(sb.toString());
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
    //
    // public static void saveComponents(List<Circuit> circuits) {
    //     StringBuilder sb = new StringBuilder("");
    //     for (Circuit c : circuits) {
    //         saveComponent(c);
    //         sb.append(c.name + "\n");
    //     }
    //     try (BufferedWriter bw = new BufferedWriter(new FileWriter("res/backup_order.txt"))) {
    //         bw.append(sb.toString());
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}