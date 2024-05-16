package circuit;

import javax.swing.JFrame;

public class Main {
	public static void main(String[] args) {
		ProgramManager programManager = new ProgramManager();
		programManager.frame = new JFrame(ProgramManager.TITLE);
		programManager.frame.setUndecorated(true);
		programManager.frame.add(programManager);
		programManager.frame.pack();
		programManager.frame.setSize(ProgramManager.WIDTH, ProgramManager.HEIGHT);
		programManager.frame.setResizable(false);
		programManager.frame.setLocation(850, 375);
		programManager.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		programManager.frame.setVisible(true);
		
		programManager.start();
	}
}