package com.housetracker.authservice.validator;

import static java.util.Objects.isNull;

public abstract class Validator<T> {

    private Validator next;

    public static <T> Validator<T> link(Validator<T> first, Validator<T>... chain) {
        Validator<T> head = first;
        for (Validator<T> nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void validate(T target);

    protected void validateNext(T target) {
        if (isNull(next)) {
            return;
        }
        next.validate(target);
    }
}
