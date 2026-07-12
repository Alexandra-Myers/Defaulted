package net.atlas.defaulted.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.base.BasePatches;
import net.atlas.defaulted.base.BasePatchesBuilder;
import net.atlas.defaulted.base.PatchType;
import net.atlas.defaulted.command.argument.PatchTypeArgument;
import net.atlas.defaulted.mixin.PatchedDataComponentMapAccessor;
import net.atlas.defaulted.utils.CommonUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DefaultedCommand {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext context) {
        commandDispatcher.register(Commands.literal("defaulted")
                .requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("item")
                        .then(Commands.argument("item", ResourceArgument.resource(context, Registries.ITEM))
                                .then(Commands.argument("to_read", StringArgumentType.word())
                                        .suggests((context1, builder) -> PatchTypeArgument.suggestionsReadCodec(context1, builder, PatchType.ITEM))
                                        .then(Commands.literal("get-prototype").executes(DefaultedCommand::readItemPrototype))
                                        .then(Commands.literal("get-patched").executes(DefaultedCommand::readItemPatch))
                                        .then(Commands.literal("diff").executes(DefaultedCommand::readItemDiff)))))
                .then(Commands.literal("enchantment")
                        .then(Commands.argument("enchantment", ResourceArgument.resource(context, Registries.ENCHANTMENT))
                                .then(Commands.argument("to_read", StringArgumentType.word())
                                        .suggests((context1, builder) -> PatchTypeArgument.suggestionsReadCodec(context1, builder, PatchType.ENCHANTMENT))
                                        .then(Commands.literal("get-prototype").executes(DefaultedCommand::readEnchantmentPrototype))
                                        .then(Commands.literal("get-patched").executes(DefaultedCommand::readEnchantmentPatch))
                                        .then(Commands.literal("diff").executes(DefaultedCommand::readEnchantmentDiff)))))
                .then(Commands.literal("generate")
                        .then(Commands.literal("build")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .executes(DefaultedCommand::buildPatch)))
                        .then(Commands.literal("start")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(PatchTypeArgument::suggestions)
                                        .then(Commands.argument("id", IdentifierArgument.id())
                                                .executes(DefaultedCommand::createUniversal)
                                                .then(Commands.argument("elements", StringArgumentType.greedyString())
                                                        .executes(DefaultedCommand::create)))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .then(Commands.argument("to_write", StringArgumentType.word())
                                                .suggests(((context1, builder) -> PatchTypeArgument.suggestionsEditCodec(context1, builder, "id")))
                                                .then(Commands.argument("value", StringArgumentType.greedyString()).executes(DefaultedCommand::setValue)))))
                        .then(Commands.literal("append-patch-generator")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .then(Commands.argument("generator", StringArgumentType.greedyString())
                                                .executes(DefaultedCommand::appendPatchGenerator))))
                        .then(Commands.literal("priority")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .suggests(PatchTypeArgument::suggestionsExistingBuilder)
                                        .then(Commands.argument("priority", IntegerArgumentType.integer())
                                                .executes(DefaultedCommand::priority))))));
    }

    private static int createUniversal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PatchType<?, ?, ?, ?> patchType = PatchTypeArgument.getPatchType(context, "type");
        Identifier id = IdentifierArgument.getId(context, "id");
        patchType.addBuilder(id);
        return 0;
    }

    private static int create(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PatchType<?, ?, ?, ?> patchType = PatchTypeArgument.getPatchType(context, "type");
        Identifier id = IdentifierArgument.getId(context, "id");
        String elements = StringArgumentType.getString(context, "elements");
        patchType.addBuilder(id, new StringReader(elements), context.getSource().registryAccess());
        return 0;
    }

    private static int buildPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        BasePatches<?, ?> patches = patchType.get(id).build();
        JsonElement root = patches.save(context.getSource().registryAccess());
        File outputFile = context.getSource().getLevel().getStructureManager().worldTemplates().createAndValidatePathToStructure(id, FileToIdConverter.registry(patches.key())).toFile();
        try {
            outputFile.createNewFile();
            GSON.toJson(root, new PrintWriter(outputFile));
        } catch (IOException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(new StringReader(""), e.getMessage());
        }
        return 0;
    }

    private static int appendPatchGenerator(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        BasePatchesBuilder<?, ?, ?, ?> patches = patchType.get(id);
        String generator = StringArgumentType.getString(context, "generator");
        patches.addGeneratorRaw(CommonUtils.parse(new StringReader(generator), context.getSource().registryAccess(), patchType.generatorCodec()));
        return 0;
    }

    private static int setValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        PatchTypeArgument.writeValue(context, id, "to_write", StringArgumentType.getString(context, "value"), patchType);
        return 0;
    }

    private static int priority(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "id");
        PatchType<?, ?, ?, ?> patchType = PatchType.forId(id);
        if (patchType == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        BasePatchesBuilder<?, ?, ?, ?> patches = patchType.get(id);
        int priority = IntegerArgumentType.getInteger(context, "priority");
        patches.setPriority(priority);
        return 0;
    }

    private static DataComponentMap prototype(Holder.Reference<Item> reference) {
        return (reference.components() instanceof PatchedDataComponentMap patchedDataComponentMap ?
                PatchedDataComponentMapAccessor.class.cast(patchedDataComponentMap).getPrototype() :
                reference.components());
    }

    private static int readItemPrototype(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, prototype(reference))));
        return 0;
    }

    private static int readItemPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, reference.components())));
        return 0;
    }

    private static int readItemDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Item> reference = ResourceArgument.getResource(context, "item", Registries.ITEM);
        String prototype = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, prototype(reference));
        String patched = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ITEM, reference.components());
        emit(context, CommonUtils.unifiedDiff(prototype, patched));
        return 0;
    }

    private static int readEnchantmentPrototype(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, Defaulted.getOriginalEnchantment(reference))));
        return 0;
    }

    private static int readEnchantmentPatch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        emit(context, CommonUtils.processTag(PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, reference.value())));
        return 0;
    }

    private static int readEnchantmentDiff(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Enchantment> reference = ResourceArgument.getResource(context, "enchantment", Registries.ENCHANTMENT);
        String prototype = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, Defaulted.getOriginalEnchantment(reference));
        String patched = PatchTypeArgument.getReadValue(context, "to_read", PatchType.ENCHANTMENT, reference.value());
        emit(context, CommonUtils.unifiedDiff(prototype, patched));
        return 0;
    }

    private static void emit(CommandContext<CommandSourceStack> context, List<? extends Component> output) {
        output.forEach(component -> context.getSource().sendSuccess(() -> component, false));
    }
}
