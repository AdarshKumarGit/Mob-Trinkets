package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.trinket.TrinketData;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MobTrinkets.MOD_ID);

    public static final Supplier<AttachmentType<TrinketData>> TRINKETS = ATTACHMENTS.register("trinkets",
            () -> AttachmentType.builder(() -> TrinketData.EMPTY)
                    .serialize(TrinketData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
