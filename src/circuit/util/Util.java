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

    public static boolean isPointWithinBox(int x, int y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= (bx + bw) && y >= by && y <= (by + bh);
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
}