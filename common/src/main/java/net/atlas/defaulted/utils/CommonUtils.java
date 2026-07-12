package net.atlas.defaulted.utils;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtils {
    private static final DiffRowGenerator GENERATOR = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .oldTag(left -> left ? "§c" : "§r§4")
            .newTag(left -> left ? "§a" : "§r§2")
            .build();
    public static <A> A parse(StringReader reader, RegistryAccess registryAccess, Codec<A> codec) throws CommandSyntaxException {
        DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registryAccess);
        return codec.parse(ops, TagParser.create(ops).parseAsArgument(reader))
                .getOrThrow(s -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, s));
    }
    public static <A> String readValueOf(A value, Codec<A> codec, DynamicOps<Tag> ops) {
        return codec.encodeStart(ops, value).mapOrElse(tag -> {
            PrettyStringTagVisitor tagVisitor = new PrettyStringTagVisitor();
            tag.accept(tagVisitor);
            return tagVisitor.build();
        }, DataResult.Error::message);
    }
    public static List<MutableComponent> processTag(String str) {
        return Arrays.stream(str.split("\n")).map(s -> Component.literal(s).withStyle(ChatFormatting.GRAY)).toList();
    }
    public static List<MutableComponent> unifiedDiff(String oldStr, String newStr) {
        List<String> oldLines = Arrays.stream(oldStr.split("\n")).toList();
        List<String> newLines = Arrays.stream(newStr.split("\n")).toList();
        List<DiffRow> rows = GENERATOR.generateDiffRows(oldLines, newLines);
        List<MutableComponent> diff = new ArrayList<>();
        rows.forEach(row -> addUnifiedDiffLines(diff, row));
        return diff;
    }

    public static void addUnifiedDiffLines(List<MutableComponent> diff, DiffRow diffRow) {
        switch (diffRow.getTag()) {
            case INSERT -> diff.add(Component.literal("§2+")
                    .append(Component.literal("§2" + diffRow.getNewLine())));
            case DELETE -> diff.add(Component.literal("§4-")
                    .append(Component.literal("§4" + diffRow.getOldLine())));
            case CHANGE -> {
                diff.add(Component.literal("§4-")
                        .append(Component.literal("§4" + diffRow.getOldLine())));
                diff.add(Component.literal("§2+")
                        .append(Component.literal("§2" + diffRow.getNewLine())));
            }
            case EQUAL -> diff.add(Component.literal("§7" + diffRow.getNewLine()));
        }
    }
}
