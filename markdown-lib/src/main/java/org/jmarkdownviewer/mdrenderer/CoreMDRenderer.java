package org.jmarkdownviewer.mdrenderer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonmark.internal.renderer.text.BulletListHolder;
import org.commonmark.internal.renderer.text.ListHolder;
import org.commonmark.internal.renderer.text.OrderedListHolder;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;

public class CoreMDRenderer extends AbstractVisitor implements NodeRenderer {

    protected final MDNodeRendererContext context;
    private final MDWriter textContent;

    private ListHolder listHolder;

    public CoreMDRenderer(MDNodeRendererContext context) {
        this.context = context;
        this.textContent = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                Document.class,
                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class
        ));
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Document document) {
        // No rendering itself
        visitChildren(document);
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        textContent.write('«');
        visitChildren(blockQuote);
        textContent.write('»');

        writeEndOfLineIfNeeded(blockQuote, null);
    }

    @Override
    public void visit(BulletList bulletList) {
        if (listHolder != null) {
            writeEndOfLine();
        }
        listHolder = new BulletListHolder(listHolder, bulletList);
        visitChildren(bulletList);
        writeEndOfLineIfNeeded(bulletList, null);
        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
    }

    @Override
    public void visit(Code code) {
        textContent.write('\"');
        textContent.write(code.getLiteral());
        textContent.write('\"');
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        if (context.stripNewlines()) {
            textContent.writeStripped(fencedCodeBlock.getLiteral());
            writeEndOfLineIfNeeded(fencedCodeBlock, null);
        } else {
            textContent.write(fencedCodeBlock.getLiteral());
        }
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        writeEndOfLineIfNeeded(hardLineBreak, null);
    }

    @Override
    public void visit(Heading heading) {
        visitChildren(heading);
        writeEndOfLineIfNeeded(heading, ':');
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        if (!context.stripNewlines()) {
            textContent.write("***");
        }
        writeEndOfLineIfNeeded(thematicBreak, null);
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        writeText(htmlInline.getLiteral());
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        writeText(htmlBlock.getLiteral());
    }

    @Override
    public void visit(Image image) {
        writeLink(image, image.getTitle(), image.getDestination());
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        if (context.stripNewlines()) {
            textContent.writeStripped(indentedCodeBlock.getLiteral());
            writeEndOfLineIfNeeded(indentedCodeBlock, null);
        } else {
            textContent.write(indentedCodeBlock.getLiteral());
        }
    }

    @Override
    public void visit(Link link) {
        writeLink(link, link.getTitle(), link.getDestination());
    }

    @Override
    public void visit(ListItem listItem) {
        if (listHolder != null && listHolder instanceof OrderedListHolder) {
            OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
            String indent = context.stripNewlines() ? "" : orderedListHolder.getIndent();
            textContent.write(indent + orderedListHolder.getCounter() + orderedListHolder.getDelimiter() + " ");
            visitChildren(listItem);
            writeEndOfLineIfNeeded(listItem, null);
            orderedListHolder.increaseCounter();
        } else if (listHolder != null && listHolder instanceof BulletListHolder) {
            BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
            if (!context.stripNewlines()) {
                textContent.write(bulletListHolder.getIndent() + bulletListHolder.getMarker() + " ");
            }
            visitChildren(listItem);
            writeEndOfLineIfNeeded(listItem, null);
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
        if (listHolder != null) {
            writeEndOfLine();
        }
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
        writeEndOfLineIfNeeded(orderedList, null);
        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        // Add "end of line" only if its "root paragraph.
        if (paragraph.getParent() == null || paragraph.getParent() instanceof Document) {
            writeEndOfLineIfNeeded(paragraph, null);
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        writeEndOfLineIfNeeded(softLineBreak, null);
    }

    @Override
    public void visit(Text text) {
        writeText(text.getLiteral());
    }

    @Override
    protected void visitChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private void writeText(String text) {
        if (context.stripNewlines()) {
            textContent.writeStripped(text);
        } else {
            textContent.write(text);
        }
    }

    private void writeLink(Node node, String title, String destination) {
        boolean hasChild = node.getFirstChild() != null;
        boolean hasTitle = title != null && !title.equals(destination);
        boolean hasDestination = destination != null && !destination.equals("");

        if (hasChild) {
            textContent.write('"');
            visitChildren(node);
            textContent.write('"');
            if (hasTitle || hasDestination) {
                textContent.whitespace();
                textContent.write('(');
            }
        }

        if (hasTitle) {
            textContent.write(title);
            if (hasDestination) {
                textContent.colon();
                textContent.whitespace();
            }
        }

        if (hasDestination) {
            textContent.write(destination);
        }

        if (hasChild && (hasTitle || hasDestination)) {
            textContent.write(')');
        }
    }

    private void writeEndOfLineIfNeeded(Node node, Character c) {
        if (context.stripNewlines()) {
            if (c != null) {
                textContent.write(c);
            }
            if (node.getNext() != null) {
                textContent.whitespace();
            }
        } else {
            if (node.getNext() != null) {
                textContent.line();
            }
        }
    }

    private void writeEndOfLine() {
        if (context.stripNewlines()) {
            textContent.whitespace();
        } else {
            textContent.line();
        }
    }

}
