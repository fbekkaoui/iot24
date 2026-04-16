package de.feswiesbaden.iot.views.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import de.feswiesbaden.iot.views.MainLayout;

@PageTitle("Dokumentation")
@Route(value = "documentation", layout = MainLayout.class)
public class DocumentationView extends VerticalLayout {

    private static final Path README_PATH = Path.of("README.md");
    private static final String CLASSPATH_README = "project-docs/README.md";
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[(.*?)]\\((.*?)\\)");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.*?)]\\((.*?)\\)");
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^\\d+\\.\\s+(.*)$");

    public DocumentationView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("overflow", "auto");
        getStyle().set("padding", "1.25rem 1.5rem 1.75rem");

        Div content = new Div();
        content.setWidthFull();
        content.getStyle().set("max-width", "920px");
        content.getStyle().set("display", "flex");
        content.getStyle().set("flex-direction", "column");
        content.getStyle().set("gap", "0.85rem");

        add(content);
        for (Component component : loadReadmeComponents()) {
            content.add(component);
        }
    }

    private List<Component> loadReadmeComponents() {
        try {
            ReadmeSource readmeSource = loadReadmeSource();
            if (readmeSource == null) {
                return List.of(createFallbackText("README.md wurde weder im Projekt noch im Build gefunden."));
            }
            return parseMarkdown(readmeSource);
        } catch (IOException exception) {
            return List.of(createFallbackText("README.md konnte nicht gelesen werden: " + exception.getMessage()));
        }
    }

    private ReadmeSource loadReadmeSource() throws IOException {
        if (Files.exists(README_PATH)) {
            return new ReadmeSource(
                    Files.readAllLines(README_PATH, StandardCharsets.UTF_8),
                    README_PATH.toAbsolutePath().getParent(),
                    null);
        }

        InputStream readmeStream = getClass().getClassLoader().getResourceAsStream(CLASSPATH_README);
        if (readmeStream == null) {
            return null;
        }

        try (InputStream inputStream = readmeStream) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new ReadmeSource(List.of(content.split("\\R", -1)), null, "project-docs");
        }
    }

    private List<Component> parseMarkdown(ReadmeSource readmeSource) {
        List<Component> components = new ArrayList<>();
        List<String> paragraphLines = new ArrayList<>();
        List<String> unorderedListItems = new ArrayList<>();
        List<String> orderedListItems = new ArrayList<>();
        StringBuilder codeBlock = null;

        for (String line : readmeSource.lines()) {
            if (line.startsWith("```")) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);

                if (codeBlock == null) {
                    codeBlock = new StringBuilder();
                } else {
                    components.add(createCodeBlock(codeBlock.toString().stripTrailing()));
                    codeBlock = null;
                }
                continue;
            }

            if (codeBlock != null) {
                codeBlock.append(line).append(System.lineSeparator());
                continue;
            }

            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);
                continue;
            }

            if (trimmedLine.startsWith("# ")) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);
                components.add(createHeading(trimmedLine.substring(2), 1));
                continue;
            }

            if (trimmedLine.startsWith("## ")) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);
                components.add(createHeading(trimmedLine.substring(3), 2));
                continue;
            }

            if (trimmedLine.startsWith("### ")) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);
                components.add(createHeading(trimmedLine.substring(4), 3));
                continue;
            }

            if (trimmedLine.startsWith("- ")) {
                flushParagraph(components, paragraphLines);
                flushOrderedList(components, orderedListItems);
                unorderedListItems.add(trimmedLine.substring(2).trim());
                continue;
            }

            Matcher orderedListMatcher = ORDERED_LIST_PATTERN.matcher(trimmedLine);
            if (orderedListMatcher.matches()) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                orderedListItems.add(orderedListMatcher.group(1).trim());
                continue;
            }

            Matcher imageMatcher = IMAGE_PATTERN.matcher(trimmedLine);
            if (imageMatcher.matches()) {
                flushParagraph(components, paragraphLines);
                flushUnorderedList(components, unorderedListItems);
                flushOrderedList(components, orderedListItems);
                components.add(createImage(imageMatcher.group(1), imageMatcher.group(2), readmeSource));
                continue;
            }

            paragraphLines.add(trimmedLine);
        }

        flushParagraph(components, paragraphLines);
        flushUnorderedList(components, unorderedListItems);
        flushOrderedList(components, orderedListItems);

        if (codeBlock != null) {
            components.add(createCodeBlock(codeBlock.toString().stripTrailing()));
        }

        return components;
    }

    private void flushParagraph(List<Component> components, List<String> paragraphLines) {
        if (paragraphLines.isEmpty()) {
            return;
        }

        String text = String.join(" ", paragraphLines);
        components.add(createParagraph(text));
        paragraphLines.clear();
    }

    private void flushUnorderedList(List<Component> components, List<String> listItems) {
        if (listItems.isEmpty()) {
            return;
        }

        UnorderedList list = new UnorderedList();
        list.getStyle().set("margin", "0");
        list.getStyle().set("padding-left", "1.35rem");

        for (String itemText : listItems) {
            ListItem item = new ListItem();
            item.getStyle().set("margin", "0.2rem 0");
            item.add(createInlineText(itemText));
            list.add(item);
        }

        components.add(list);
        listItems.clear();
    }

    private void flushOrderedList(List<Component> components, List<String> listItems) {
        if (listItems.isEmpty()) {
            return;
        }

        OrderedList list = new OrderedList();
        list.getStyle().set("margin", "0");
        list.getStyle().set("padding-left", "1.35rem");

        for (String itemText : listItems) {
            ListItem item = new ListItem();
            item.getStyle().set("margin", "0.2rem 0");
            item.add(createInlineText(itemText));
            list.add(item);
        }

        components.add(list);
        listItems.clear();
    }

    private Component createHeading(String text, int level) {
        Component heading = switch (level) {
            case 1 -> new H1(text);
            case 2 -> new H2(text);
            default -> new H3(text);
        };
        heading.getElement().getStyle().set("margin", "0");
        return heading;
    }

    private Paragraph createParagraph(String text) {
        Paragraph paragraph = new Paragraph();
        paragraph.getStyle().set("margin", "0");
        paragraph.getStyle().set("line-height", "1.6");
        paragraph.add(createInlineText(text));
        return paragraph;
    }

    private Pre createCodeBlock(String code) {
        Pre pre = new Pre(code);
        pre.getStyle().set("margin", "0");
        pre.getStyle().set("padding", "0.25rem 0");
        pre.getStyle().set("background", "transparent");
        pre.getStyle().set("overflow", "auto");
        return pre;
    }

    private Component createImage(String altText, String source, ReadmeSource readmeSource) {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            return styleImage(new Image(source, altText));
        }

        if (readmeSource.fileSystemBase() != null) {
            Path imagePath = readmeSource.fileSystemBase().resolve(source).normalize();
            if (Files.exists(imagePath)) {
                StreamResource resource = new StreamResource(imagePath.getFileName().toString(), () -> {
                    try {
                        return Files.newInputStream(imagePath);
                    } catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                });
                return styleImage(new Image(resource, altText));
            }
        }

        if (readmeSource.classpathBase() != null) {
            String classpathLocation = readmeSource.classpathBase() + "/" + source;
            if (getClass().getClassLoader().getResource(classpathLocation) != null) {
                StreamResource resource = new StreamResource(Path.of(source).getFileName().toString(), () -> {
                    InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathLocation);
                    if (stream == null) {
                        throw new UncheckedIOException(new IOException("Bildressource nicht gefunden: " + classpathLocation));
                    }
                    return stream;
                });
                return styleImage(new Image(resource, altText));
            }
        }

        return createFallbackText("Bild konnte nicht geladen werden: " + source);
    }

    private Image styleImage(Image image) {
        image.setMaxWidth("100%");
        image.getStyle().set("height", "auto");
        image.getStyle().set("margin", "0.35rem 0");
        return image;
    }

    private Div createInlineText(String text) {
        Div wrapper = new Div();
        wrapper.getStyle().set("display", "inline");

        int index = 0;
        Matcher matcher = LINK_PATTERN.matcher(text);
        while (matcher.find()) {
            if (matcher.start() > index) {
                wrapper.add(new Span(text.substring(index, matcher.start())));
            }

            Anchor link = new Anchor(matcher.group(2), matcher.group(1));
            link.setTarget("_blank");
            wrapper.add(link);
            index = matcher.end();
        }

        if (index < text.length()) {
            wrapper.add(new Span(text.substring(index)));
        }

        return wrapper;
    }

    private Paragraph createFallbackText(String text) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.getStyle().set("margin", "0");
        paragraph.getStyle().set("color", "var(--lumo-secondary-text-color)");
        return paragraph;
    }

    private record ReadmeSource(List<String> lines, Path fileSystemBase, String classpathBase) {
    }
}
