package sharedbackup;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class GUI {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		DefaultListModel model = new DefaultListModel();
		JList list = new JList(model);
		list.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		list.setBounds(10, 11, 245, 240);
		
		 // Initialize the list with items
	    String[] items = { "A", "B", "C", "D" };
	    for (int i = 0; i < items.length; i++) {
	        model.add(i, items[i]);

	      }
		frame.getContentPane().add(list);
	}
}
