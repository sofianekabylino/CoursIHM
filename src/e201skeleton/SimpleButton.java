package e201skeleton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fr.lri.swingstates.canvas.CExtensionalTag;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.EnterOnShape;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.debug.StateMachineEvent;
import fr.lri.swingstates.debug.StateMachineEventAdapter;
import fr.lri.swingstates.debug.StateMachineVisualization;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Release;
import fr.lri.swingstates.sm.transitions.TimeOut;

/**
 * 
 * @author Sofiane YOUSFI the Kabylino
 * 
 */
public class SimpleButton extends MouseAdapter {

	private static final String CLIQUE_DROIT = "Clique droit";
	private static final String ERROR_DROIT_NE_FONCTIONNE_PAS = "Le Clique droit ne fonctionne pas!";
	private static final String CLIQUE_GAUCHE = "Clique gauche";
	private static final String BUTTON_NAME = "simple";
	private static CText label;
	private static CRectangle rectangle;

	SimpleButton(final Canvas canvas, final String text) {
		label = canvas.newText(0, 0, text, new Font("verdana", Font.PLAIN, 12));

		double x = label.getMinX();
		double y = label.getMinY();
		double w = label.getHeight();
		double h = label.getWidth();
		rectangle = canvas.newRectangle(x - 5, y - 5, h + 10, w + 10);
		rectangle.setFillPaint(Color.white);
		label.above(rectangle);
		label.addChild(rectangle);
		rectangle.getFillPaint();
		canvas.addMouseListener(this);
	}

	public void mouseClicked(final MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			System.err.println(CLIQUE_GAUCHE);
		} else {
			System.err.println(ERROR_DROIT_NE_FONCTIONNE_PAS);
		}
	}

	/**
	 * mettre et retirer la couleur jaune
	 */
	final static CExtensionalTag yellow = new CExtensionalTag() {
		public void added(CShape s) {
			s.setOutlined(true).setFillPaint(Color.yellow);
		};

		public void removed(CShape s) {
			s.setOutlined(true).setFillPaint(Color.white);

		};
	};

	/**
	 * mettre et retirer la couleur rouge du double click
	 */
	final static CExtensionalTag red = new CExtensionalTag() {
		public void added(CShape s) {
			s.setOutlined(true).setFillPaint(Color.red);
		};

		public void removed(CShape s) {
			s.setOutlined(true).setFillPaint(Color.white);
		};
	};

	/**
	 * La méthode qui rassemble les transactions
	 */
	public static void statesFactory() {
		JFrame frame = new JFrame();
		final Canvas canvas = new Canvas(400, 400);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);

		final SimpleButton myButton = new SimpleButton(canvas, BUTTON_NAME);
		SimpleButton.getShape().translateBy(100, 100);

		/*
		 * La machine a etat
		 */
		CStateMachine sm = new CStateMachine() {

			/*
			 * etat initial
			 */
			public State idle = new State() {
				final Transition enterOnShape = new EnterOnShape(">> on") {
					public void action() {
						rectangle.setStroke(new BasicStroke(2));
					}
				};
			};
			// état du bouton lorsque on le survole
			public State on = new State() {
				final Transition leaveOnShape = new LeaveOnShape(">> idle") {
					public void action() {
						rectangle.setStroke(new BasicStroke(1));
					}
				};
				final Transition pressOnShape = new PressOnShape(BUTTON1, ">> demiClk") {
					public void action() {
						rectangle.addTag(yellow);
						rectangle.setStroke(new BasicStroke(1));
						// Timer qui permettra de revenir à l'état 'on' si on
						// maintient le click
						armTimer(1000, false);
					}
				};

			};

			/**
			 * état lors d'un press sur le bouton
			 */
			public State press = new State() {
				// si on maintien le click... au bout d'un moment on revient à
				// l'état 'on'
				final Transition timeOut = new TimeOut(">> on") {
					public void action() {
						rectangle.removeTag(yellow);
						rectangle.setStroke(new BasicStroke(2));
					}
				};
				// Si on press une deuxieme fois... on pass au click et demi
				final Transition pressOnShape = new PressOnShape(BUTTON1, ">> clkETdemi") {
					public void action() {
						rectangle.addTag(red);
						rectangle.setStroke(new BasicStroke(1));
						armTimer(1000, false);// Timer qui permettra de revenir
												// a l'etat 'on' si on maintien
												// le click
					}
				};
			};

			/**
			 * état lors du maintien du press en dehors du bouton
			 */
			public State outBtn = new State() {
				// en rentrant dans le bouton... on revient a demiclk et le
				// timer et réinitialisé
				final Transition enterOnShape = new EnterOnShape(">> demiClk") {
					public void action() {
						rectangle.addTag(yellow);
						rectangle.setStroke(new BasicStroke(1));
						armTimer(1000, false);
					}
				};
				// Si on relache... on revien a l'etat initial
				final Release release = new Release(">> idle") {
					public void action() {
						rectangle.removeTag(yellow);
						rectangle.setStroke(new BasicStroke(1));
					}
				};

			};

			/**
			 * état du demi clique
			 */
			public State demiClk = new State() {
				// Si on relache... on pass a l'etat press
				final Transition releaseOnShape = new ReleaseOnShape(">> press") {
					public void action() {
						rectangle.addTag(yellow);
						rectangle.setStroke(new BasicStroke(1));
						// Timer
						armTimer(300, false);
					}
				};
				// Si on quitte le bouton en pressant... on passe a outBtn
				final Transition leaveOnShape = new LeaveOnShape(">> outBtn") {
					public void action() {
						// disarmTimer();
						rectangle.removeTag(yellow);
						rectangle.setStroke(new BasicStroke(1));
					}
				};
				// lorsque le temps defini dans le timer s'écoule... on revien à
				// l'etat on
				final Transition timeOut = new TimeOut(">> on") {
					public void action() {
						rectangle.removeTag(yellow);
						rectangle.setStroke(new BasicStroke(2));
					}
				};

			};

			/**
			 * état double click
			 */
			public State dblClk = new State() {
				// si on maintien le click... au bout d'un moment on revient a
				// l'etat 'on'
				final Transition timeOut = new TimeOut(">> on") {
					public void action() {
						rectangle.removeTag(red);
						rectangle.setStroke(new BasicStroke(2));
					}
				};
			};

			/**
			 * état click et demi
			 */
			public State clkETdemi = new State() {

				// si on maintien le click... au bout d'un moment on revient à
				// l'état 'on'
				final Transition timeOut = new TimeOut(">> on") {
					public void action() {
						rectangle.removeTag(red);
						rectangle.setStroke(new BasicStroke(2));
					}
				};
				// Si on relache... on pass a l'etat dblClk
				final Transition releaseOnShape = new ReleaseOnShape(">> dblClk") {
					public void action() {
						rectangle.addTag(red);
						rectangle.setStroke(new BasicStroke(1));
						armTimer(300, false);
					}
				};

			};

		};
		sm.armTimer(500, true);

		// Visualisation de la State Machine
		final JFrame viz = new JFrame();
		viz.getContentPane().add(new StateMachineVisualization(sm));
		viz.pack();
		viz.setVisible(true);

		// Visualisation des états lors des transitions
		sm.attachTo(canvas);
		sm.addStateMachineListener(new StateMachineEventAdapter() {
			public void smStateChanged(StateMachineEvent e) {
				System.out.println("State changed from " + e.getPreviousState().getName() + " to "
						+ e.getCurrentState().getName() + "\n");
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Action
	 */
	public void action() {
		System.out.println("ACTION!");
	}

	/**
	 * @return CShape the label
	 */
	public static CShape getShape() {
		return label;
	}

	/**
	 * Quand on presse sur la souris
	 */
	public void mousePressed(final MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			System.out.println(CLIQUE_GAUCHE);
		} else {
			System.out.println(CLIQUE_DROIT);
		}
	}

	
}
