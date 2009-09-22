package eu.europeana.dashboard.client.widgets;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import eu.europeana.dashboard.client.DashboardWidget;
import eu.europeana.dashboard.client.Reply;
import eu.europeana.dashboard.client.dto.EuropeanaIdX;
import eu.europeana.dashboard.client.dto.SavedItemX;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow dashboarders to manage a list of carousel items
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class RecordEditWidget extends DashboardWidget {
    private List<Float> boostList = new ArrayList<Float>();
    private EuropeanaIdX europeanaId;
    private Label uriLabel = new Label();
    private Label stateLabel = new Label();
    private TextArea solrRecords = new TextArea();
    private ListBox boostBox = new ListBox();
    private Widget formWidget;

    public RecordEditWidget(World world) {
        super(world);
        fillBoostBox();
    }

    protected Widget createWidget() {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setSpacing(15);
        panel.add(new SavedItemChooser(world, new SavedItemChooserOwner()).createWidget());
        formWidget = createFormWidget();
        formWidget.setVisible(false);
        panel.add(formWidget);
        return panel;
    }

    private Widget createFormWidget() {
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(10);
        panel.add(uriLabel);
        panel.add(stateLabel);
        panel.add(new Label(world.messages().solrRecordXML()));
        panel.add(wrapSolrRecords());
        panel.add(boostBox);
        Button submit = new Button(world.messages().submit());
        submit.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                if (europeanaId != null) {
                    europeanaId.setBoostFactor(getBoost());
                    europeanaId.setSolrRecords(solrRecords.getText());
                    world.service().updateEuropeanaId(europeanaId, new Reply<EuropeanaIdX>() {
                        public void onSuccess(EuropeanaIdX result) {
                            setEuropeanaId(result);
                        }
                    });
                    setEuropeanaId(null);
                }
            }
        });
        panel.add(submit);
        return panel;
    }

    private Widget wrapSolrRecords() {
        solrRecords.setSize("100%", "100%");
        ScrollPanel scrollPanel = new ScrollPanel(solrRecords);
        scrollPanel.setSize("700px", "300px");
        DecoratorPanel dp = new DecoratorPanel();
        dp.setWidget(scrollPanel);
        return dp;
    }

    private void setBoost(Float boost) {
        if (boost == null) {
            boost = 1.0f;
        }
        int index = 0;
        int which = 0;
        float minDistance = Float.MAX_VALUE;
        for (Float value : boostList) {
            float distance = Math.abs(value-boost);
            if (distance < minDistance) {
                minDistance = distance;
                which = index;
            }
            index++;
        }
        boostBox.setSelectedIndex(which);
    }

    private float getBoost() {
        return boostList.get(boostBox.getSelectedIndex());
    }

    private void fillBoostBox() {
        for (int value = 1; value < 20; value++) {
            boostList.add(value/10f);
            if (value < 10) {
                boostBox.addItem("Boost 0."+value);
            }
            else {
                boostBox.addItem("Boost 1."+(value-10));
            }
        }
    }

    private void setEuropeanaId(EuropeanaIdX europeanaId) {
        this.europeanaId = europeanaId;
        if (europeanaId != null) {
            uriLabel.setText(europeanaId.getEuropeanaUri());
            String stateString = world.messages().europeanaIdCreated(
                    europeanaId.getCreated()
            );
            stateLabel.setText(stateString);
            solrRecords.setText(europeanaId.getSolrRecords());
            setBoost(europeanaId.getBoostFactor());
        }
        formWidget.setVisible(europeanaId != null);
    }

    private class SavedItemChooserOwner implements SavedItemChooser.Owner {
        public boolean avoidItem(SavedItemX savedItem) {
            return europeanaId != null && europeanaId.getEuropeanaUri().equals(savedItem.getUri());
        }

        public void selectItem(final SavedItemX savedItem) {
            world.service().fetchEuropeanaId(savedItem.getUri(), new Reply<EuropeanaIdX>() {
                public void onSuccess(EuropeanaIdX result) {
                    setEuropeanaId(result);
                }
            });
        }
    }

}