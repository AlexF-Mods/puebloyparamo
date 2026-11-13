package io.github.alexf6.puebloyparamo.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Ítem del mod con descripción traducible y efecto opcional al terminar de consumirse.
 */
public class DescribedItem extends Item {

    private final String tooltipKey;
    private final Consumer<LivingEntity> onConsume;

    public DescribedItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, null);
    }

    public DescribedItem(Properties properties, String tooltipKey, Consumer<LivingEntity> onConsume) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.onConsume = onConsume;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && onConsume != null) {
            onConsume.accept(entity);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> consumer,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, consumer, flag);
        if (tooltipKey != null && !tooltipKey.isEmpty()) {
            consumer.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
