package munit.internal.junitinterface;

import java.lang.annotation.Annotation;

public class Indent implements Annotation {
    final int value;

    public Indent(int value) {
        this.value = value;
    }

    public int value() { return value; }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Indent.class;
    }
}
