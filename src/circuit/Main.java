package circuit;

import javax.swing.JFrame;
import circuit.util.ThreadPool;
import circuit.util.Util;

public class Main {
	public static void main(String[] args) {
		ThreadPool pool = new ThreadPool(2);
		System.out.println("Running on OS: " + System.getProperty("os.name"));

		Game game = Game.getInstance();
		game.frame = new JFrame(Game.TITLE);
		game.frame.setUndecorated(true);
		game.frame.add(game);
		game.frame.pack();
		game.frame.setSize(Game.WIDTH, Game.HEIGHT);
		game.frame.setResizable(false);
		game.frame.setLocationRelativeTo(null);
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setVisible(true);

		pool.runTask(game);

		pool.join();
	}
}