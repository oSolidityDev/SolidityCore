package io.github.abdullahcxd.soliditycore.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class DeprecationException extends RuntimeException {
    public DeprecationException(String message) {
        super(message);
    }
    public DeprecationException(String name, @NotNull DeprecatedType type, String message) { super("The " + type.getName() + " named " + name + " has been deprecated: " + message); }

    @Getter
    public enum DeprecatedType {
        Method("method"),
        Class("class"),
        Constructor("constructor"),
        Field("field"),
        LocalVariable("local variable"),
        Package("package"),
        Module("module"),
        Parameter("parameter"),
        Type("type");

        private final String name;

        DeprecatedType(String name) {
            this.name = name;
        }
    }

}
