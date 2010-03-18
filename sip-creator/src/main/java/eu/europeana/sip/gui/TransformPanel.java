package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.EuropeanaField;
import eu.europeana.sip.mapping.MappingTree;
import eu.europeana.sip.reference.Transform;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle the choices for converter, and allow for filling in parameters
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class TransformPanel extends JPanel {
    private final Logger LOG = Logger.getLogger(TransformPanel.class);
    private MappingTree.Node node;
    private EuropeanaField europeanaField;
    private Listener listener;
    private JComboBox transformBox = new JComboBox(Transform.values());
    private JTextArea groovyArea = new JTextArea();

    public TransformPanel(MappingTree.Node node, EuropeanaField europeanaField, Listener listener) {
        super(new BorderLayout());
        this.node = node;
        this.europeanaField = europeanaField;
        this.listener = listener;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Choose transform:", JLabel.RIGHT), BorderLayout.WEST);
        transformBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                setTransform();
            }
        });
        p.add(transformBox, BorderLayout.CENTER);
        add(p, BorderLayout.NORTH);
        groovyArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(groovyArea, BorderLayout.CENTER);
        JButton compileButton = new JButton("Compile");
        add(compileButton, BorderLayout.SOUTH);
        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                Binding binding = new Binding(retrieveVariables());
                try {
                    new GroovyShell(binding).evaluate(groovyArea.getText());
                }
                catch (CompilationFailedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        for (Transform transform : Transform.values()) {
            //JPanel panel = form[transform.ordinal()] = new FormPanel(transform);
            //formCards.add(panel, transform.toString());
        }
        setTransform();
    }

    public Map<String, String> retrieveVariables() {
        Map<String, String> map = new HashMap<String, String>();
        // todo: retrieve variables
        return map;
    }

    public interface Listener {
        void transformSelected(Transform transform, String[] fieldValues);
    }

    private void setTransform() {
        Transform transform = (Transform) transformBox.getModel().getSelectedItem();
        setTransform(transform);
        //listener.transformSelected(transform, form[transform.ordinal()].getValues());
    }

    private void setTransform(Transform transform) {
        if (transform == null) {
            return;
        }
        //((CardLayout) formCards.getLayout()).show(formCards, transform.toString());
    }

    private class FormPanel extends JPanel {
        private Transform transform;
        private JTextField[] fields;

        public FormPanel(Transform transform) {
            super(new SpringLayout());
            this.transform = transform;
            String[] paramNames = transform.parameterNames();
            fields = new JTextField[paramNames.length];
            int count = 0;
            for (String paramName : paramNames) {
                JLabel label = new JLabel(paramName, JLabel.RIGHT);
                add(label);
                fields[count] = new JTextField(10);
                label.setLabelFor(fields[count]);
                add(fields[count]);
                fields[count].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent event) {
                        listener.transformSelected(FormPanel.this.transform, getValues());
                    }
                });
                count++;
            }
            makeCompactGrid(this, paramNames.length, 2, 5, 5, 5, 5);
        }


        public String[] getValues() {
            String[] values = new String[fields.length];
            int count = 0;
            for (JTextField field : fields) {
                values[count++] = field.getText();
            }
            return values;
        }
    }

    /* Used by makeCompactGrid. */

    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param parent   component containing us
     * @param rows     number of rows
     * @param cols     number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad     x padding between cells
     * @param yPad     y padding between cells
     */
    public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

}
