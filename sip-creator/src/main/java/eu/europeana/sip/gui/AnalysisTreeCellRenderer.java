/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.gui;

import eu.europeana.sip.mapping.MappingTree;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.awt.Font;

/**
 * Render the cells of the analysis tree
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AnalysisTreeCellRenderer extends DefaultTreeCellRenderer {
    private Font normalFont, thickFont;

    @Override
    public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
        MappingTree.Node node = (MappingTree.Node) o;
        JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, o, b, b1, b2, i, b3);
        label.setFont(node.getStatistics() != null ? getThickFont() : getNormalFont());
        return label;
    }

    private Font getNormalFont() {
        if (normalFont == null) {
            normalFont = super.getFont();
        }
        return normalFont;
    }

    private Font getThickFont() {
        if (thickFont == null) {
            thickFont = new Font(getNormalFont().getFontName(), Font.BOLD, getNormalFont().getSize());
        }
        return thickFont;
    }
}
