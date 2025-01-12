package net.blay09.mods.balm.api.config;

public interface BalmConfigProperty<T> {
    Class<T> getType();
    Class<T> getInnerType();
    void setValue(T value);
    T getValue();
    T getDefaultValue();
}
