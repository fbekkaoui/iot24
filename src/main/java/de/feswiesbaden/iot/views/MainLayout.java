package de.feswiesbaden.iot.views;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import de.feswiesbaden.iot.views.dashboard.DashboardView;
import de.feswiesbaden.iot.views.documentation.DocumentationView;
import de.feswiesbaden.iot.views.mqttvalue.MqttValueView;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final Tabs navigationTabs = new Tabs();
    private final Map<Class<? extends Component>, Tab> tabsByView = new LinkedHashMap<>();

    public MainLayout() {
        addToNavbar(createHeaderContent());
        setDrawerOpened(false);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Tab selectedTab = tabsByView.get(getContent().getClass());
        if (selectedTab != null) {
            navigationTabs.setSelectedTab(selectedTab);
        }
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.getStyle().set("padding", "1rem 1.5rem");
        header.setWidthFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setSpacing(true);

        H1 appName = new H1("Internet of Things Beispielanwendung");
        appName.getStyle().set("margin", "0");
        appName.getStyle().set("font-size", "1.75rem");
        appName.getStyle().set("font-weight", "600");

        navigationTabs.add(createTab("Dashboard", LineAwesomeIcon.CHART_BAR_SOLID.create(), DashboardView.class));
        navigationTabs.add(createTab("MQTT", LineAwesomeIcon.GLOBE_SOLID.create(), MqttValueView.class));
        navigationTabs.add(createTab("Readme.md", LineAwesomeIcon.FILE_ALT_SOLID.create(), DocumentationView.class));
        navigationTabs.getStyle().set("margin", "0 auto");

        Div leftArea = createBalancedArea();
        Div centerArea = createBalancedArea();
        Div rightArea = createBalancedArea();

        leftArea.add(appName);
        centerArea.add(navigationTabs);
        leftArea.getStyle().set("display", "flex");
        leftArea.getStyle().set("align-items", "center");
        centerArea.getStyle().set("display", "flex");
        centerArea.getStyle().set("align-items", "center");
        centerArea.getStyle().set("justify-content", "center");
        rightArea.getStyle().set("display", "flex");
        rightArea.getStyle().set("align-items", "center");

        layout.add(leftArea, centerArea, rightArea);
        layout.expand(leftArea, centerArea, rightArea);
        header.add(layout);
        return header;
    }

    private Tab createTab(String label, Component icon, Class<? extends Component> navigationTarget) {
        RouterLink link = new RouterLink();
        link.setRoute(navigationTarget);
        link.add(icon, new Span(label));
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("gap", "0.45rem");

        Tab tab = new Tab(link);
        tabsByView.put(navigationTarget, tab);
        return tab;
    }

    private Div createBalancedArea() {
        Div area = new Div();
        area.getStyle().set("flex", "1");
        return area;
    }
}
