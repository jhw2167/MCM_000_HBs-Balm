package net.blay09.mods.balm.forge;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;

public class DeferredRegisters {
    private static final Table<ResourceKey<?>, String, DeferredRegister<?>> deferredRegisters = Tables.synchronizedTable(HashBasedTable.create());

    public static <T> DeferredRegister<T> get(IForgeRegistry<T> registry, String modId) {
        return get(registry.getRegistryKey(), modId);
    }

    @SuppressWarnings("unchecked")
    public static <T> DeferredRegister<T> get(ResourceKey<Registry<T>> registry, String modId) {
        DeferredRegister<?> register = deferredRegisters.get(registry, modId);
        if (register == null) {
            register = DeferredRegister.create(registry, modId);
            deferredRegisters.put(registry, modId, register);
        }

        return (DeferredRegister<T>) register;
    }

    public static Collection<DeferredRegister<?>> getByModId(String modId) {
        return deferredRegisters.column(modId).values();
    }

    public static void register(String modId, IEventBus modEventBus) {
        synchronized (deferredRegisters) {
            for (DeferredRegister<?> deferredRegister : DeferredRegisters.getByModId(modId)) {
                deferredRegister.register(modEventBus);
            }
        }
    }
}
