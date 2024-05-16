package circuit.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {
    // Calculates the square of a number
    public static double sqr(double x) { return x * x; }
    
    // Calculates the distance between two 2D points
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    // Calculates the distance squared between two points
    public static double distanceSquared(double x1, double y1, double x2, double y2) { return sqr(x2 - x1) + sqr(y2 - y1); }

    // Calculates the distance from a point to a given line
    public static double distanceFromLine(double x1, double y1, double x2, double y2, double p, double q) {
        double l2 = distanceSquared(x1, y1, x2, y2);
        if (l2 == 0) return Math.sqrt(distanceSquared(p, q, x1, y1));
        double t = ((p - x1) * (x2 - x1) + (q - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        return Math.sqrt(distanceSquared(p, q, x1 + t * (x2 - x1), y1 + t * (y2 - y1)));
    }

    // Calculates whether a point overlaps the bounds of a box
    public static boolean isPointWithinBox(int x, int y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= (bx + bw) && y >= by && y <= (by + bh);
    }
    
    // Loads a text file and returns a list of the lines
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
}