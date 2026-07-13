package net.atlas.defaulted.utils;

import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PrettyStringTagVisitor implements TagVisitor {
    private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
    private final StringBuilder builder = new StringBuilder();
    private int indentLevel = 0;

    public String build() {
        return this.builder.toString();
    }

    @Override
    public void visitString(final StringTag tag) {
        this.builder.append(StringTag.quoteAndEscape(tag.value()));
    }

    @Override
    public void visitByte(final ByteTag tag) {
        this.builder.append(tag.value()).append('b');
    }

    @Override
    public void visitShort(final ShortTag tag) {
        this.builder.append(tag.value()).append('s');
    }

    @Override
    public void visitInt(final IntTag tag) {
        this.builder.append(tag.value());
    }

    @Override
    public void visitLong(final LongTag tag) {
        this.builder.append(tag.value()).append('L');
    }

    @Override
    public void visitFloat(final FloatTag tag) {
        this.builder.append(tag.value()).append('f');
    }

    @Override
    public void visitDouble(final DoubleTag tag) {
        this.builder.append(tag.value()).append('d');
    }

    @Override
    public void visitByteArray(final ByteArrayTag tag) {
        this.builder.append("[B;");
        this.indentLevel++;
        byte[] data = tag.getAsByteArray();

        for (int i = 0; i < data.length; i++) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append('\n');
            this.appendIndent();

            this.builder.append(data[i]).append('B');
        }

        this.indentLevel--;
        if (!tag.isEmpty()) {
            this.builder.append('\n');
            this.appendIndent();
        }
        this.builder.append(']');
    }

    @Override
    public void visitIntArray(final IntArrayTag tag) {
        this.builder.append("[I;");
        this.indentLevel++;
        int[] data = tag.getAsIntArray();

        for (int i = 0; i < data.length; i++) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append('\n');
            this.appendIndent();

            this.builder.append(data[i]);
        }

        this.indentLevel--;
        if (!tag.isEmpty()) {
            this.builder.append('\n');
            this.appendIndent();
        }
        this.builder.append(']');
    }

    @Override
    public void visitLongArray(final LongArrayTag tag) {
        this.builder.append("[L;");
        this.indentLevel++;
        long[] data = tag.getAsLongArray();

        for (int i = 0; i < data.length; i++) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append('\n');
            this.appendIndent();

            this.builder.append(data[i]).append('L');
        }

        this.indentLevel--;
        if (!tag.isEmpty()) {
            this.builder.append('\n');
            this.appendIndent();
        }
        this.builder.append(']');
    }

    @Override
    public void visitList(final ListTag tag) {
        this.builder.append('[');
        this.indentLevel++;

        for (int i = 0; i < tag.size(); i++) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append('\n');
            this.appendIndent();

            tag.get(i).accept(this);
        }

        this.indentLevel--;
        if (!tag.isEmpty()) {
            this.builder.append('\n');
            this.appendIndent();
        }
        this.builder.append(']');
    }

    @Override
    public void visitCompound(final CompoundTag tag) {
        this.builder.append('{');
        this.indentLevel++;
        List<Map.Entry<String, Tag>> entries = new ArrayList<>(tag.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Tag> entry = entries.get(i);
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append('\n');
            this.appendIndent();

            this.handleKeyEscape(entry.getKey());
            this.builder.append(':');
            entry.getValue().accept(this);
        }

        this.indentLevel--;
        if (!tag.isEmpty()) {
            this.builder.append('\n');
            this.appendIndent();
        }
        this.builder.append('}');
    }

    private void handleKeyEscape(final String input) {
        if (!input.equalsIgnoreCase("true") && !input.equalsIgnoreCase("false") && UNQUOTED_KEY_MATCH.matcher(input).matches()) {
            this.builder.append(input);
        } else {
            StringTag.quoteAndEscape(input, this.builder);
        }
    }

    @Override
    public void visitEnd(final @NotNull EndTag tag) {
        this.builder.append("END");
    }

    public void appendIndent() {
        this.builder.append("  ".repeat(Math.max(0, this.indentLevel)));
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }
}
