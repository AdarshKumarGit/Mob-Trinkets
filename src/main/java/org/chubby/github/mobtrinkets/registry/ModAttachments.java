package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.chubby.github.mobtrinkets.MobTrinkets;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MobTrinkets.MOD_ID);

    public static final Supplier<AttachmentType<ItemStack>> EQUIPPED = ATTACHMENTS.register("equipped_trinket",
            () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                    .serialize(ItemStack.OPTIONAL_CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
