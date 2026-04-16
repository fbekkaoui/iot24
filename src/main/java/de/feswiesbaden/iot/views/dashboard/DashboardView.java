package de.feswiesbaden.iot.views.dashboard;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.GridBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.builder.MarkersBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.PieBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.pie.builder.DonutBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import de.feswiesbaden.iot.views.MainLayout;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter AXIS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] SERIES_COLORS = {"#1e6f5c", "#e07a5f", "#3d5a80", "#c8553d", "#7a9e7e", "#6d597a"};
    private static final String EMPTY_TEXT = "Noch keine Daten vorhanden.";

    public DashboardView(MqttValueService mqttValueService) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        addClassName("dashboard-view");

        List<MqttValue> recentValues = mqttValueService.findRecent(24);
        Map<String, Long> topicCounts = mqttValueService.countByTopic();

        add(
                createMetricRow(mqttValueService.countAll(), mqttValueService.countTopics(), mqttValueService.findLatest()),
                createChartRow(topicCounts),
                createTrendCard(recentValues));
    }

    private HorizontalLayout createMetricRow(long totalMessages, int topicCount, MqttValue latestValue) {
        HorizontalLayout row = createRow("dashboard-metrics");
        row.add(
                createMetricCard("Nachrichten", String.valueOf(totalMessages), "Gesamtzahl gespeicherter Werte"),
                createMetricCard("Topics", String.valueOf(topicCount), "Unterschiedliche MQTT-Topics"),
                createMetricCard("Letzte Aktivitaet", formatLatestValue(latestValue), "Zuletzt gespeicherte Nachricht"));
        return row;
    }

    private HorizontalLayout createChartRow(Map<String, Long> topicCounts) {
        HorizontalLayout row = createRow("dashboard-charts");

        Div barCard = createChartCard(
                "Nachrichten pro Topic",
                "Balkendiagramm der vorhandenen Nachrichten je Topic.",
                createTopicBarChart(topicCounts));

        Div donutCard = createChartCard(
                "Topics im Vergleich",
                "Donut-Chart zur Verteilung der Nachrichten auf vorhandene Topics.",
                createTopicDonutChart(topicCounts));

        row.add(barCard, donutCard);
        row.setFlexGrow(1, barCard, donutCard);
        return row;
    }

    private Div createTrendCard(List<MqttValue> values) {
        Div card = createCard("dashboard-wide-card");
        card.add(new H3("Topic-Verlauf"));
        card.add(createNote("Numerische MQTT-Nachrichten als gemeinsames Linien- und Punktdiagramm pro Topic."));
        card.add(createTopicTrendChart(values));
        return card;
    }

    private HorizontalLayout createRow(String className) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setPadding(false);
        row.setSpacing(true);
        row.addClassName(className);
        return row;
    }

    private Div createMetricCard(String title, String value, String description) {
        Div card = createCard("dashboard-metric-card");
        card.add(new H3(title), new Paragraph(value), createNote(description));
        return card;
    }

    private Div createChartCard(String title, String description, Component chart) {
        Div card = createCard("dashboard-chart-card");
        card.add(new H3(title), createNote(description), chart);
        return card;
    }

    private Div createCard(String className) {
        Div card = new Div();
        card.addClassName("dashboard-card");
        card.addClassName(className);
        return card;
    }

    private Component createTopicBarChart(Map<String, Long> topicCounts) {
        if (topicCounts.isEmpty()) {
            return createEmptyState(EMPTY_TEXT);
        }

        Double[] values = topicCounts.values().stream()
                .map(Long::doubleValue)
                .toArray(Double[]::new);

        ApexCharts chart = ApexChartsBuilder.get()
                .withChart(createBaseChartConfig(Type.BAR, "270"))
                .withColors(SERIES_COLORS)
                .withSeries(new Series<>("Nachrichten", values))
                .withLegend(LegendBuilder.get().withShow(false).build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withGrid(createGrid())
                .withXaxis(XAxisBuilder.get().withCategories(topicCounts.keySet().toArray(String[]::new)).build())
                .withYaxis(createYAxis(0.0, null))
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withDistributed(true)
                                .withColumnWidth("52%")
                                .build())
                        .build())
                .build();

        return prepareChart(chart, false);
    }

    private Component createTopicDonutChart(Map<String, Long> topicCounts) {
        if (topicCounts.isEmpty()) {
            return createEmptyState(EMPTY_TEXT);
        }

        Double[] values = topicCounts.values().stream()
                .map(Long::doubleValue)
                .toArray(Double[]::new);

        ApexCharts chart = ApexChartsBuilder.get()
                .withChart(createBaseChartConfig(Type.DONUT, "270"))
                .withColors(SERIES_COLORS)
                .withSeries(values)
                .withLabels(topicCounts.keySet().toArray(String[]::new))
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withLegend(createBottomLegend())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withPie(PieBuilder.get()
                                .withDonut(DonutBuilder.get().withSize("68%").build())
                                .build())
                        .build())
                .build();

        return prepareChart(chart, false);
    }

    private Component createTopicTrendChart(List<MqttValue> values) {
        List<MqttValue> numericValues = values.stream()
                .filter(value -> isNumeric(value.getMessage()))
                .sorted(Comparator.comparing(MqttValue::getTimeStamp))
                .toList();

        if (numericValues.isEmpty()) {
            return createEmptyState("Fuer dieses Diagramm werden numerische MQTT-Nachrichten benoetigt.");
        }

        List<String> categories = buildTimeCategories(numericValues);
        Map<String, Double[]> valuesByTopic = buildSeriesDataByTopic(numericValues);
        Double maxValue = findMaxNumericValue(numericValues);

        List<Series<Double>> seriesList = new ArrayList<>();
        valuesByTopic.forEach((topic, topicValues) -> seriesList.add(new Series<>(topic, topicValues)));

        ApexCharts chart = ApexChartsBuilder.get()
                .withChart(createBaseChartConfig(Type.LINE, "320"))
                .withColors(SERIES_COLORS)
                .withSeries(toSeriesArray(seriesList))
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withLegend(createBottomLegend())
                .withGrid(createGrid())
                .withMarkers(MarkersBuilder.get().withSize(4.0, 6.0).build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).withWidth(3.0).build())
                .withXaxis(XAxisBuilder.get().withCategories(categories).build())
                .withYaxis(createYAxis(0.0, maxValue == 0.0 ? 1.0 : maxValue + Math.max(1.0, maxValue * 0.1)))
                .build();

        return prepareChart(chart, true);
    }

    private com.github.appreciated.apexcharts.config.Chart createBaseChartConfig(Type type, String height) {
        return ChartBuilder.get()
                .withType(type)
                .withHeight(height)
                .withToolbar(ToolbarBuilder.get().withShow(false).build())
                .build();
    }

    private com.github.appreciated.apexcharts.config.Grid createGrid() {
        return GridBuilder.get()
                .withBorderColor("#dbe4ea")
                .withStrokeDashArray(4.0)
                .build();
    }

    private com.github.appreciated.apexcharts.config.Legend createBottomLegend() {
        return LegendBuilder.get()
                .withShow(true)
                .withPosition(Position.BOTTOM)
                .withHorizontalAlign(HorizontalAlign.LEFT)
                .build();
    }

    private com.github.appreciated.apexcharts.config.YAxis createYAxis(Double min, Double max) {
        YAxisBuilder builder = YAxisBuilder.get().withMin(min);
        if (max != null) {
            builder.withMax(max);
        }
        return builder.build();
    }

    private ApexCharts prepareChart(ApexCharts chart, boolean wide) {
        chart.addClassName("dashboard-apex-chart");
        if (wide) {
            chart.addClassName("dashboard-apex-chart-wide");
        }
        return chart;
    }

    private List<String> buildTimeCategories(List<MqttValue> numericValues) {
        List<String> categories = new ArrayList<>();
        for (int index = 0; index < numericValues.size(); index++) {
            MqttValue value = numericValues.get(index);
            categories.add(AXIS_TIME_FORMAT.format(value.getTimeStamp()) + " #" + (index + 1));
        }
        return categories;
    }

    private Map<String, Double[]> buildSeriesDataByTopic(List<MqttValue> numericValues) {
        Map<String, Double[]> valuesByTopic = new LinkedHashMap<>();

        for (String topic : numericValues.stream().map(MqttValue::getTopic).distinct().toList()) {
            valuesByTopic.put(topic, new Double[numericValues.size()]);
        }

        for (int index = 0; index < numericValues.size(); index++) {
            MqttValue value = numericValues.get(index);
            valuesByTopic.get(value.getTopic())[index] = Double.parseDouble(value.getMessage());
        }

        return valuesByTopic;
    }

    private Double findMaxNumericValue(List<MqttValue> numericValues) {
        double maxValue = 0.0;
        for (MqttValue value : numericValues) {
            maxValue = Math.max(maxValue, Double.parseDouble(value.getMessage()));
        }
        return maxValue;
    }

    private Component createEmptyState(String text) {
        Div empty = new Div(new Paragraph(text));
        empty.addClassName("dashboard-empty");
        return empty;
    }

    private Span createNote(String text) {
        Span note = new Span(text);
        note.addClassName("dashboard-note");
        return note;
    }

    private String formatLatestValue(MqttValue latestValue) {
        if (latestValue == null) {
            return "Noch keine Nachricht";
        }
        return TIME_FORMAT.format(latestValue.getTimeStamp()) + " / " + latestValue.getTopic();
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Series<Double>[] toSeriesArray(List<Series<Double>> seriesList) {
        return seriesList.toArray(new Series[0]);
    }
}
